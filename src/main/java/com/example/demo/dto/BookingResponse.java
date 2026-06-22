package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    
    // Thông tin Sân
    private Long courtId;
    private String courtName;
    private String courtImage;
    private String courtAddress; 

    // Thông tin User (An toàn: Không có password)
    private Long userId;
    private String username;
    private String phone;

    // Thông tin Đặt lịch
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private String status; 
}