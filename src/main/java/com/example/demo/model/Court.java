package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    private String address;

    private String imageUrl;

    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour; 

    private LocalTime openingTime;
    private LocalTime closingTime;

    // 1. Dùng @JsonIgnore để CẤM Jackson đụng vào field này (nguyên nhân gây lỗi 500)
    @ManyToOne(fetch = FetchType.LAZY) // Có thể để LAZY thoải mái
    @JoinColumn(name = "category_id")
    @JsonIgnore 
    private Category category;

    // 2. Tạo hàm thủ công để lấy dữ liệu Category an toàn
    // Hàm này sẽ tự động chạy khi xuất JSON và trả về object sạch
    @JsonProperty("category")
    public Map<String, Object> getCategoryDetail() {
        if (category == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        return map;
    }
}