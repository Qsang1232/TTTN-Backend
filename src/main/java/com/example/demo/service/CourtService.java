package com.example.demo.service;

import com.example.demo.dto.CourtRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.Court;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CategoryRepository categoryRepository;

    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    public Court getCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân với ID: " + id));
    }

    // THÊM SÂN (Dùng DTO)
    public Court addCourt(CourtRequest request) {
        // Tìm Category, nếu không có thì lấy mặc định ID=1 (để tránh lỗi)
        Category category = categoryRepository.findById(request.getCategoryId())
               
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sân với ID: " + request.getCategoryId()));

       
        Court court = Court.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .imageUrl(request.getImageUrl())
                .pricePerHour(request.getPricePerHour())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .category(category)
                .build();

        return courtRepository.save(court);
    }

    // CẬP NHẬT SÂN (Dùng DTO)
    public Court updateCourt(Long id, CourtRequest request) {
        Court existingCourt = getCourtById(id);
        
        // Tìm Category (nếu request gửi lên null thì giữ nguyên cái cũ)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(existingCourt.getCategory());
            existingCourt.setCategory(category);
        }

       
        // Chỉ cập nhật nếu trường đó được gửi lên (khác null)
        if (request.getName() != null) existingCourt.setName(request.getName());
        if (request.getDescription() != null) existingCourt.setDescription(request.getDescription());
        if (request.getAddress() != null) existingCourt.setAddress(request.getAddress());
        if (request.getImageUrl() != null) existingCourt.setImageUrl(request.getImageUrl());
        if (request.getPricePerHour() != null) existingCourt.setPricePerHour(request.getPricePerHour());
        if (request.getOpeningTime() != null) existingCourt.setOpeningTime(request.getOpeningTime());
        if (request.getClosingTime() != null) existingCourt.setClosingTime(request.getClosingTime());

        return courtRepository.save(existingCourt);
    }

    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sân không tồn tại");
        }
        courtRepository.deleteById(id);
    }
}