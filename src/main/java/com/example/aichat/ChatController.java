package com.example.aichat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions;

@RestController
public class ChatController {

    Logger logger  = LoggerFactory.getLogger(ChatController.class);

    private OpenAiChatModel chatModel;
    private ChatClient chatClient;
    private SimpleLoggerAdvisor simpleLoggerAdvisor;
    private ChatMemory chatMemory;

    @Autowired
    public ChatController(OpenAiChatModel chatModel, ChatClient.Builder builder, SimpleLoggerAdvisor simpleLoggerAdvisor, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.chatClient = builder.build();
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
        this.chatMemory = chatMemory;
    }

    @GetMapping("/ai/chatResponse")
    public ChatResponse chat() {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.4)
                .build();
       return chatClient.prompt()
               .options(openAiChatOptions)
               .user("Tell me about new ai tech")
               .advisors(simpleLoggerAdvisor)
               .call()
               .chatResponse();
    }

    @GetMapping("/ai/memory")
    public String getMessage(@RequestParam String message) {
        ChatClient chatClientWithMemory = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        return chatClientWithMemory.prompt()
                .user(message)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/webResponse")
    public String webSearch() {
        WebSearchOptions webSearchOptions =
                new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,null);

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.4)
                .webSearchOptions(webSearchOptions)
                .build();
        return chatClient.prompt()
                .options(openAiChatOptions)
                .user("Tell me about new ai tech that happened this week")
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/stockTrends")
    public String getStockTrends() {
        WebSearchOptions webSearchOptions =
                new WebSearchOptions(WebSearchOptions.SearchContextSize.MEDIUM, null);

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.3)
                .webSearchOptions(webSearchOptions)
                .maxTokens(600)
                .build();

        String systemPrompt = """
            You are a financial analyst AI assistant.
            Use real-time web data to find the latest stock market performance till now.
            Identify:
            - Top 5 stocks that performed the best (highest gainers)
            - Top 5 stocks that performed the worst (biggest losers)
            For each stock, include:
            1. name (stock name or ticker)
            2. howMuchChange (percentage increase or decrease)
            3. canWeBuyNow (yes/no with a brief reason)
            4. Current price now (current market price)
            Format the response in a clean table or JSON-like structure.
            Example:
            [
              {"name": "TCS", "howMuchChange": "+2.5%", "canWeBuyNow": "Yes, stable growth", "Current price now": "₹4100"},
              {"name": "HDFC Bank", "howMuchChange": "-1.8%", "canWeBuyNow": "No, short-term dip", "Current price now": "₹1450"}
            ]
            """;

        String userPrompt = "Fetch the latest stock market update for Indian market only.";

        return chatClient.prompt()
                .options(openAiChatOptions)
                .system(systemPrompt)
                .user(userPrompt)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }


    @GetMapping("/ai/hotels")
    public String getHotelsInTamilNadu() {

        WebSearchOptions webSearchOptions = new WebSearchOptions(
                WebSearchOptions.SearchContextSize.MEDIUM,
                new WebSearchOptions.UserLocation(
                        "approximate",
                        new WebSearchOptions.UserLocation.Approximate(
                                "Erode",
                                "India",
                                "Tamil Nadu",
                                "Asia/Kolkata"
                        )
                )
        );

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.4)
                .webSearchOptions(webSearchOptions)
                .maxTokens(600)
                .build();

        String systemPrompt = """
            You are a helpful travel assistant with access to real-time web search data.
            Use the user's approximate location (Erode, Tamil Nadu) to suggest good hotels in Tamil Nadu.
            Find highly-rated hotels based on recent reviews and ratings from trusted sources.
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
                .options(openAiChatOptions)
                .system(systemPrompt)
                .user(userPrompt)
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/creativeResponse")
    public String creativeResponse() {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.4)
                .frequencyPenalty(0.5)
                .maxTokens(200)
                .build();
        return chatClient.prompt()
                .options(openAiChatOptions)
                .user("Tell me a creative idea with java fullstack")
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/generateCode")
    public String generateCode() {
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(chatModel.getDefaultOptions().getModel())
                .temperature(0.4)
                .maxTokens(200)
                .build();
        return chatClient.prompt()
                .options(openAiChatOptions)
                .user("write a recursive program in java. Start with ---java--- and end with ---")
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }
//
//    @GetMapping("/ai/generate")
//    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
//        logger.info("Got the prompt as : {}", message);
//        return Map.of("generation", this.chatModel.call(message));
//    }
//
//    @GetMapping("/ai/generateStream")
//    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
//        Prompt prompt = new Prompt(new UserMessage(message));
//        logger.info("Got the prompt for stream as : {}", message);
//        return this.chatModel.stream(prompt)
//                .map(res -> res.getResult().getOutput().getText());
//    }
//
//    @PostMapping("/analyze-image")
//    public ResponseEntity<String> analyzeImage(@RequestParam("prompt") String prompt,
//                                               @RequestParam("file") MultipartFile file) throws IOException {
//
//        logger.info("Got the prompt for image as : {}", prompt);
//        logger.info("Got the image : {}", file.getName());
//        // Wrap image as a Media object. Detect MIME type (e.g. image/png, image/jpeg)
//        String mimeType = file.getContentType();
//        Media imageMedia = new Media(MimeType.valueOf(mimeType), file.getResource()); // file.getResource() is a Spring Resource
//
//        // Build the prompt: combine text and image
//        UserMessage userMsg = UserMessage.builder()
//                .text(prompt)
//                .media(List.of(imageMedia)) // Can send multiple images
//                .build();
//
//        Prompt aiPrompt = new Prompt(List.of(userMsg));
//
//        // Call the multimodal model
//        ChatResponse response = chatModel.call(aiPrompt);
//        String aiReply = response.getResult().getOutput().getText();
//
//        return ResponseEntity.ok(aiReply);
//    }
//
//    // have to use dall-e model for image generation
//    // Uncomment the following method if you want to generate images based on prompts
//
//    //@GetMapping("/generate/image")
//    //public String generateImageBasedOnPrompt(@RequestParam String message) {
//    // ImagePrompt prompt = new ImagePrompt(message);
//    //return this.imageModel.call(prompt).getResult().getOutput().getUrl();
//    //}

    
}