package com.example.demo.repository;

import com.example.demo.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourtRepository extends JpaRepository<Court, Long> {
    // 1. Tìm sân theo ID khu vực
    List<Court> findByCategoryId(Long categoryId);
    
    // 2. (Tùy chọn) Tìm kiếm theo tên sân (VD: tìm chữ "Sân 1")
    List<Court> findByNameContainingIgnoreCase(String name);
}