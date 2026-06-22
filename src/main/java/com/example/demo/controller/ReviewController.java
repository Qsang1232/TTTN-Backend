package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. Tạo đánh giá (Cần đăng nhập)
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(@RequestBody ReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Review newReview = reviewService.createReview(request, currentUsername);

        return new ResponseEntity<>(ApiResponse.<Review>builder()
                .success(true)
                .message("Đánh giá thành công")
                .data(newReview)
                .build(), HttpStatus.CREATED);
    }

    // 2. Xem đánh giá của 1 sân (Công khai)
    @GetMapping("/court/{courtId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByCourt(@PathVariable Long courtId) {
        List<Review> reviews = reviewService.getReviewsForCourt(courtId);
        return ResponseEntity.ok(ApiResponse.<List<Review>>builder()
                .success(true)
                .message("Danh sách đánh giá")
                .data(reviews)
                .build());
    }
}