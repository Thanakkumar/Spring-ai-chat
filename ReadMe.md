# spring-ai-integration

Spring Boot 3.5+ integration showcasing modern **Spring AI (v1.0.3)** capabilities with **Ollama**, featuring conversational memory, tool/function calling, structured outputs, streaming, and vision.

---

## Features

1. **Local & Private LLM Inference**: Powered by local Ollama (`llama3.2:1b` / `qwen3.5:4b`), completely offline with zero API keys or cloud costs.
2. **Provider-Agnostic Core**: Uses Spring AI's generic `ChatModel` and `ChatClient` fluent API.
3. **Multi-User Conversational Memory**: Stateful multi-turn chat with isolated conversation IDs (`chat_memory_conversation_id`) backed by `MessageChatMemoryAdvisor`.
4. **Structured Outputs / Entity Extraction**: Direct mapping from LLM responses into strongly typed Java Records (`List<HotelRecommendation>`, `List<StockAnalysis>`) via `.entity(...)`.
5. **Tool / Function Calling (`@Tool`)**: The LLM autonomously triggers Java backend functions (e.g., system clock, math calculations).
6. **Reactive SSE Streaming**: Real-time Server-Sent Events via `Flux<String>` (`/ai/generateStream`) for instant Time to First Token.
7. **Multimodal Vision**: Upload and inspect images using `MultipartFile` and Spring AI's media APIs (`/analyze-image`).
8. **Parameterized Prompt Templates**: Dynamic variable substitution via `.param("key", value)`.
9. **CPU Multi-Threading Optimized**: Tuned for low-latency CPU inference via `num-thread=4`.

---

## Configuration (`application.properties`)

```properties
spring.application.name=spring-ai-integration
server.port=8088

# Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2:1b
spring.ai.ollama.chat.options.temperature=0.4
spring.ai.ollama.chat.options.num-thread=4

logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=DEBUG
```

> **Tip for Model Selection**:
> - For **ultra-fast CPU performance (1-2s response)**: Use `llama3.2:1b` (`ollama pull llama3.2:1b`)
> - For **vision & thinking capabilities**: Use `qwen3.5:4b` (`ollama pull qwen3.5:4b`)

---

## Endpoints Overview

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/ai/chat` | **Recommended**: Chat with per-user conversational memory (JSON body) |
| `GET` | `/ai/memory?message=...&conversationId=...` | GET-based conversational memory query |
| `GET` | `/ai/generateStream?message=...` | Server-Sent Events (SSE) token streaming |
| `GET` | `/ai/hotels/structured` | Returns strongly-typed `List<HotelRecommendation>` |
| `GET` | `/ai/stocks/structured` | Returns strongly-typed `List<StockAnalysis>` |
| `GET` | `/ai/tools?question=...` | Autonomous Tool / Function Calling |
| `GET` | `/ai/code?task=...&language=...` | Parameterized prompt template code generator |
| `POST` | `/analyze-image` | Multimodal image + prompt analysis |
| `GET` | `/ai/chatResponse` | Full `ChatResponse` with token usage and metadata |
| `GET` | `/ai/generate?message=...` | Simple `Map<String, String>` response |

---

## Example Usage

### 1. Conversational Chat (`POST /ai/chat`)
```powershell
# Message 1
Invoke-RestMethod -Uri "http://localhost:8088/ai/chat" -Method Post `
  -ContentType "application/json" `
  -Body '{"message": "My name is Thanakkumar and I love Spring Boot", "conversationId": "user-42"}'

# Message 2 (remembers previous context)
Invoke-RestMethod -Uri "http://localhost:8088/ai/chat" -Method Post `
  -ContentType "application/json" `
  -Body '{"message": "What is my name and what do I love?", "conversationId": "user-42"}'
```

### 2. Autonomous Tool Calling (`GET /ai/tools`)
The LLM dynamically executes the Java tool `@Tool getCurrentDateTime()` and `@Tool multiply()`:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/ai/tools?question=What is today's date and time and what is 500 multiplied by 25?" -Method Get
```

### 3. Structured Outputs (`GET /ai/hotels/structured`)
Returns typed JSON array matching `HotelRecommendation.java`:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/ai/hotels/structured" -Method Get
```

### 4. Real-time Streaming (`GET /ai/generateStream`)
```powershell
curl -N "http://localhost:8088/ai/generateStream?message=Write a short story about an AI developer"
```

---

## Running the Application & Tests

```powershell
# Run the application
.\mvnw.cmd spring-boot:run

# Run the integration tests
.\mvnw.cmd test
```
