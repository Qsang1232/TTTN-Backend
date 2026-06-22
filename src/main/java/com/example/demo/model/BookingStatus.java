package com.example.demo.model;

public enum BookingStatus {
    PENDING_PAYMENT, // Chờ thanh toán
    CONFIRMED,       // Đã xác nhận
    COMPLETED,       // Đã hoàn thành (Sân đã sử dụng)
    CANCELLED        // Đã hủy
}