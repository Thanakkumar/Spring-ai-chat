package com.example.aichat.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeAndCalculatorTool {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeAndCalculatorTool.class);

    @Tool(description = "Get the current date and time in the system")
    public String getCurrentDateTime() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("Tool executed: getCurrentDateTime -> {}", now);
        return now;
    }

    @Tool(description = "Multiply two numbers and return the result")
    public double multiply(double a, double b) {
        double result = a * b;
        logger.info("Tool executed: multiply({} * {}) -> {}", a, b, result);
        return result;
    }
}
