package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- QUAN TRỌNG: Import dòng này
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // <--- Thêm dòng này để Category không bị lỗi khi được gọi từ Court
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 

    // Nếu sau này bạn mở dòng này ra, cũng nhớ thêm @JsonIgnoreProperties hoặc @JsonIgnore để tránh vòng lặp vô hạn
    // @OneToMany(mappedBy = "category")
    // private List<Court> courts;
}