package com.example.aichat;

import com.example.aichat.dto.ChatRequest;
import com.example.aichat.dto.HotelRecommendation;
import com.example.aichat.dto.StockAnalysis;
import com.example.aichat.tools.DateTimeAndCalculatorTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private static final String CONVERSATION_ID_PARAM = "chat_memory_conversation_id";

    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final ChatClient chatClientWithMemory;
    private final DateTimeAndCalculatorTool tools;

    @Autowired
    public ChatController(ChatModel chatModel,
                          ChatClient.Builder builder,
                          SimpleLoggerAdvisor simpleLoggerAdvisor,
                          ChatMemory chatMemory,
                          DateTimeAndCalculatorTool tools) {
        this.chatModel = chatModel;
        this.tools = tools;
        this.chatClient = builder
                .defaultAdvisors(simpleLoggerAdvisor)
                .build();
        this.chatClientWithMemory = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        simpleLoggerAdvisor
                )
                .build();
    }

    /**
     * 1. Standard POST REST endpoint with Request Body and Per-User/Session Conversation ID
     */
    @PostMapping(value = "/ai/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> chatWithMemoryPost(@RequestBody ChatRequest request) {
        logger.info("POST /ai/chat for conversationId: {}, message: {}", request.conversationId(), request.message());
        String response = chatClientWithMemory.prompt()
                .user(request.message())
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, request.conversationId()))
                .call()
                .content();
        return Map.of(
                "conversationId", request.conversationId(),
                "response", response != null ? response : ""
        );
    }

    /**
     * 2. GET Chat with Memory supporting conversationId parameter
     */
    @GetMapping("/ai/memory")
    public String getMessage(@RequestParam(defaultValue = "Tell me about new ai tech") String message,
                             @RequestParam(defaultValue = "default") String conversationId) {
        return chatClientWithMemory.prompt()
                .user(message)
                .advisors(a -> a.param(CONVERSATION_ID_PARAM, conversationId))
                .call()
                .content();
    }

    /**
     * 3. Real-time Streaming (SSE) - Lowest Time to First Token (TTFT)
     */
    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        logger.info("Streaming requested for prompt: {}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 4. Structured Output / Entity Extraction - Typed List of Records
     */
    @GetMapping("/ai/hotels/structured")
    public List<HotelRecommendation> getHotelsStructured() {
        String prompt = "List 3 top-rated hotels in Tamil Nadu for leisure stays. Be concise.";
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<HotelRecommendation>>() {});
    }

    /**
     * 5. Structured Output - Typed List of Stock Analyses
     */
    @GetMapping("/ai/stocks/structured")
    public List<StockAnalysis> getStocksStructured() {
        List<StockAnalysis> stockAnalysisList = null;
        String prompt = """
                Return exactly 3 brief stock outlooks for these Indian companies: TCS, INFY, and HDFCBANK.
                For each outlook, populate only the StockAnalysis fields:
                ticker, companyName, trend, recommendation, and reason.
                Use the ticker symbols as ticker values. Keep every value a string, use concise text,
                and do not include Markdown, extra fields, or text outside the structured result.
                This is a general AI-generated outlook, not live market data or financial advice.
                """;
        try {
            stockAnalysisList = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<StockAnalysis>>() {
                    });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return stockAnalysisList;
    }

    /**
     * 6. Tool Calling / Function Calling - Model executes backend Java methods dynamically
     */
    @GetMapping("/ai/tools")
    public String executeWithTools(@RequestParam(defaultValue = "What is the current date and time, and what is 123 multiplied by 45?") String question) {
        logger.info("Executing prompt with tools: {}", question);
        return chatClient.prompt()
                .tools(tools)
                .user(question)
                .call()
                .content();
    }

    /**
     * 7. Dynamic Prompt Template with Named Parameter Substitution
     */
    @GetMapping("/ai/code")
    public String generateCodeWithTemplate(@RequestParam(defaultValue = "binary search") String task,
                                           @RequestParam(defaultValue = "Java") String language) {
        return chatClient.prompt()
                .user(u -> u.text("Write a clean, concise implementation of {task} in {language}. Include brief comments.")
                        .param("task", task)
                        .param("language", language))
                .call()
                .content();
    }

    /**
     * 8. Multimodal Vision - Analyze uploaded image with prompt
     */
    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(@RequestParam("prompt") String prompt,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Image analysis requested. File: {}, Prompt: {}", file.getOriginalFilename(), prompt);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded file cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Uploaded file must be a valid image.");
        }

        String aiReply = chatClient.prompt()
                .user(u -> u.text(prompt).media(MimeTypeUtils.parseMimeType(contentType), file.getResource()))
                .call()
                .content();

        return ResponseEntity.ok(aiReply);
    }

    /**
     * 9. Full ChatResponse inspection (Usage metadata, token count, finish reasons)
     */
    @GetMapping("/ai/chatResponse")
    public ChatResponse chat() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .build();
        return chatClient.prompt()
                .options(chatOptions)
                .user("Give a 1-sentence tip for writing good clean code.")
                .call()
                .chatResponse();
    }

    /**
     * 10. Simple Map response
     */
    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "Tell me a fun programming fact") String message) {
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return Map.of("generation", response != null ? response : "");
    }
}