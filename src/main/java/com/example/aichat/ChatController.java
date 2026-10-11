package com.example.aichat;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final ChatClient chatClientWithMemory;
    private final SimpleLoggerAdvisor simpleLoggerAdvisor;

    @Autowired
    public ChatController(ChatModel chatModel, ChatClient.Builder builder, SimpleLoggerAdvisor simpleLoggerAdvisor, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
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

    @GetMapping("/ai/chatResponse")
    public ChatResponse chat() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .build();
        return chatClient.prompt()
                .options(chatOptions)
                .user("Tell me about new ai tech")
                .call()
                .chatResponse();
    }

    @GetMapping("/ai/memory")
    public String getMessage(@RequestParam(defaultValue = "Tell me about new ai tech") String message) {
        return chatClientWithMemory.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/ai/webResponse")
    public String webSearch() {
        // Note: Ollama is an offline local LLM. For real-time web search, integrate
        // an external search tool (e.g. Tavily, DuckDuckGo) via Spring AI Tools.
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .build();
        return chatClient.prompt()
                .options(chatOptions)
                .user("Tell me about new ai tech that happened recently")
                .call()
                .content();
    }

    @GetMapping("/ai/stockTrends")
    public String getStockTrends() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.3)
                .maxTokens(600)
                .build();

        String systemPrompt = """
            You are a financial analyst AI assistant.
            Provide an insightful analysis of stock market trends for the Indian market.
            Identify:
            - Top 5 stocks that perform well (gainers/leaders)
            - Top 5 stocks that face headwinds
            For each stock, include:
            1. name (stock name or ticker)
            2. howMuchChange (percentage increase or decrease)
            3. canWeBuyNow (yes/no with a brief reason)
            4. Current price now (approximate market price range)
            Format the response in a clean table or JSON-like structure.
            Example:
            [
              {"name": "TCS", "howMuchChange": "+2.5%", "canWeBuyNow": "Yes, stable growth", "Current price now": "₹4100"},
              {"name": "HDFC Bank", "howMuchChange": "-1.8%", "canWeBuyNow": "No, short-term dip", "Current price now": "₹1450"}
            ]
            """;

        String userPrompt = "Fetch the latest stock market update for Indian market only.";

        return chatClient.prompt()
                .options(chatOptions)
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    @GetMapping("/ai/hotels")
    public String getHotelsInTamilNadu() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .maxTokens(600)
                .build();

        String systemPrompt = """
            You are a helpful travel assistant.
            Suggest good hotels in Tamil Nadu for leisure or family stays.
            Include a variety of cities (e.g., Coimbatore, Chennai, Ooty, Madurai, Kodaikanal, Erode).
            For each hotel, provide:
              1. name (hotel name)
              2. city
              3. rating (out of 5 or 10)
              4. priceRange (average per night in INR)
              5. reviewHighlight (a short summary of what guests liked)
            Format the response in a JSON-like array as shown below:
            [
              {"name": "Taj Coromandel", "city": "Chennai", "rating": "9.1/10", "priceRange": "₹9,000–₹12,000", "reviewHighlight": "Excellent luxury stay and food"},
              {"name": "Savoy - IHCL SeleQtions", "city": "Ooty", "rating": "8.9/10", "priceRange": "₹7,500–₹10,000", "reviewHighlight": "Great colonial charm and hillside views"}
            ]
            """;

        String userPrompt = "Find the top-rated hotels in Tamil Nadu for leisure or family stays.";

        return chatClient.prompt()
                .options(chatOptions)
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    @GetMapping("/ai/creativeResponse")
    public String creativeResponse() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .frequencyPenalty(0.5)
                .maxTokens(200)
                .build();
        return chatClient.prompt()
                .options(chatOptions)
                .user("Tell me a creative idea with java fullstack")
                .call()
                .content();
    }

    @GetMapping("/ai/generateCode")
    public String generateCode() {
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.4)
                .maxTokens(200)
                .build();
        return chatClient.prompt()
                .options(chatOptions)
                .user("write a recursive program in java. Start with ---java--- and end with ---")
                .call()
                .content();
    }

    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        logger.info("Got the prompt as: {}", message);
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return Map.of("generation", response != null ? response : "");
    }

    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        logger.info("Got the prompt for stream as: {}", message);
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    @PostMapping(value = """
            /analyze-image""", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(@RequestParam("prompt") String prompt,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("Got prompt for image: {}", prompt);
        logger.info("Got image file: {}, size: {}", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("File must be an image.");
        }

        String aiReply = chatClient.prompt()
                .user(u -> u.text(prompt).media(MimeTypeUtils.parseMimeType(contentType), file.getResource()))
                .call()
                .content();

        return ResponseEntity.ok(aiReply);
    }
}