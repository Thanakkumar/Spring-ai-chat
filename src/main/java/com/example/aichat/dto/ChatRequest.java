package com.example.aichat.dto;

public record ChatRequest(
    String message,
    String conversationId
) {
    public ChatRequest {
        if (message == null || message.isBlank()) {
            message = "Hello!";
        }
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = "default";
        }
    }
}
