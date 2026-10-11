# spring-ai-integration

Spring Boot integration demonstrating Spring AI chat client with **Ollama** (`qwen3.5:4b`), conversational memory, and logging advisors.

## Features
- **Ollama Local LLM**: Integrates with local Ollama running `qwen3.5:4b` — no external API keys or cloud costs required.
- **Provider-Agnostic Design**: Uses generic Spring AI `ChatModel` and `ChatClient` abstractions.
- **Conversational Memory**: Maintains conversation state using `MessageChatMemoryAdvisor` backed by `ChatMemory`.
- **Advisors & Logging**: Request/response logging via `SimpleLoggerAdvisor`.
- **Multiple Endpoints**: Chat responses, memory retention, structured prompts, creative generation, and code generation.

## Requirements
- **JDK 17 or 21**
- **Ollama** installed and running locally
- **Model**: `qwen3.5:4b`
  ```bash
  ollama run qwen3.5:4b
  ```
- **Maven Wrapper**: Included (`mvnw` / `mvnw.cmd`)

## Quick Start

### 1. Start Ollama and verify model
Make sure Ollama is running and `qwen3.5:4b` is available:
```powershell
ollama list
```
If you need to pull it:
```powershell
ollama pull qwen3.5:4b
```

### 2. Configure `application.properties`
The default configuration connects to your local Ollama instance on port 11434:
```properties
spring.application.name=spring-ai-integration
server.port=8085

# Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3.5:4b
spring.ai.ollama.chat.options.temperature=0.4

logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=DEBUG
```

### 3. Run the Application
Using Maven wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```

## API Endpoints

The server runs by default on port `8085`:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/ai/chatResponse` | Basic prompt returning full `ChatResponse` |
| `GET` | `/ai/memory?message=...` | Conversational endpoint maintaining chat history |
| `GET` | `/ai/webResponse` | Query recent AI tech topics |
| `GET` | `/ai/stockTrends` | Financial analyst prompt analyzing market trends |
| `GET` | `/ai/hotels` | Travel assistant prompt returning structured hotel recommendations |
| `GET` | `/ai/creativeResponse` | Creative ideation with frequency penalty options |
| `GET` | `/ai/generateCode` | Recursive Java code generation |

### Example Request

```powershell
# Chat with memory
Invoke-RestMethod -Uri "http://localhost:8085/ai/memory?message=Hello! My name is Alice" -Method Get
Invoke-RestMethod -Uri "http://localhost:8085/ai/memory?message=What is my name?" -Method Get
```

## Testing

Run unit & integration tests:
```powershell
.\mvnw.cmd test
```
