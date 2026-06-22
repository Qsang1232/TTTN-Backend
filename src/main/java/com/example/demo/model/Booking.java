package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Dùng String cho đơn giản, tránh lỗi Enum mapping phức tạp
    // Giá trị: PENDING, CONFIRMED, CANCELLED
    private String status; 

    private BigDecimal totalPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;
}