# spring-ai-integration

Simple Spring Boot integration demonstrating an AI chat endpoint with memory and logging advisors.

## Features
- Exposes a GET endpoint to send a user message to an AI chat client with memory
- Example uses configurable model and temperature options
- Minimal logging/advisor integration

## Requirements
- JDK 17 or newer
- Maven 3.8+ (used for building/running the project)
- IMPORTANT: You must provide your own AI provider API key. The repository does not include any API keys.
- Recommended: an IDE such as IntelliJ IDEA for development on Windows

## Quick start


Provide your API key

   The application requires an API key (for example, an OpenAI key). Do not commit your key to version control.

   - Set the key in `cmd.exe` (temporary for the session):
     ```cmd
     set OPENAI_API_KEY=your_openai_api_key_here
     ```

   - Or in PowerShell (temporary for the session):
     ```powershell
     $env:OPENAI_API_KEY = "your_openai_api_key_here"
     ```

   - Or add to `src/main/resources/application.properties` (NOT recommended for public repos):
     ```properties
     openai.api.key=your_openai_api_key_here
     ai.chat.model=gpt-4-placeholder
     ai.chat.temperature=0.4
     ```

   Note: The code reads configuration from environment variables and `application.properties`. Choose the method that fits your deployment and keep secrets out of source control.

## API

GET `/ai/memory`

Query parameters:
- `message` (optional) — text prompt for the AI. Defaults to `Tell me about new ai tech` when not provided.

Response:
- Returns a JSON representation of `ChatResponse` (the AI response structure used by the app).

Example (cmd / PowerShell):
```cmd
curl "http://localhost:8080/ai/memory?message=Hello%20AI"
```

PowerShell example using `Invoke-RestMethod`:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/ai/memory?message=Hello%20AI" -Method Get
```

## Implementation details

- Controller behavior:
  - Reads the `message` query param (with a default).
  - Builds a `ChatClient` using the configured chat model and `MessageChatMemoryAdvisor` (backed by `chatMemory`).
  - Builds `OpenAiChatOptions` from configured model and temperature.
  - Sends the prompt via `chatClient.prompt().options(...).user(message).advisors(simpleLoggerAdvisor).call().chatResponse()`.

- Advisors:
  - Memory advisor persists conversational context so follow-up messages can use prior context.
  - Logger advisor records prompts and responses for debugging and observability.

- Configuration:
  - Provide API keys via environment variables or `application.properties`.
  - Example properties: `openai.api.key`, `ai.chat.model`, `ai.chat.temperature`.

## Development & Tests
- Run unit tests:
  ```cmd
  mvn test
  ```
- Use your IDE for debugging controllers and services (set environment variables in Run Configuration).

## Contributing
- Fork the repository, create a feature branch, and submit a PR against `main`.
- Keep secrets out of commits.

## License
- MIT License (or choose an appropriate license for your project).
