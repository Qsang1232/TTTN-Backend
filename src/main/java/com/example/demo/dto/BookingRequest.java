package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}