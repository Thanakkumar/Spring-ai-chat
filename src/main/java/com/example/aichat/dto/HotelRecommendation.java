package com.example.aichat.dto;

public record HotelRecommendation(
    String name,
    String city,
    String rating,
    String priceRange,
    String reviewHighlight
) {}
