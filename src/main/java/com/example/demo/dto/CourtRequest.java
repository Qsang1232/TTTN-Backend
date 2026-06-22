package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class CourtRequest {
    private String name;
    private String description;
    private String address;
    private String imageUrl;
    private BigDecimal pricePerHour;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Long categoryId; // Chỉ nhận ID, không nhận cả object Category
}