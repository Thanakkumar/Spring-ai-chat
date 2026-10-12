package com.example.aichat.dto;

public record StockAnalysis(
    String ticker,
    String trend,
    String recommendation,
    String reason
) {}
