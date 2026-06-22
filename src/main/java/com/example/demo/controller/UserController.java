package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Đã sửa đường dẫn để không trùng với Admin
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. Xem hồ sơ cá nhân (Profile)
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getMyProfile() {
        String currentUsername = getCurrentUsername();
        User user = userService.getUserByUsername(currentUsername);

        // Ẩn mật khẩu trước khi trả về
        user.setPassword(null);

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .success(true)
                .message("Lấy thông tin cá nhân thành công")
                .data(user)
                .build());
    }

    // 2. Cập nhật hồ sơ cá nhân
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateMyProfile(@RequestBody UserUpdateRequest request) {
        String currentUsername = getCurrentUsername();
        User updatedUser = userService.updateUserProfile(currentUsername, request);

        // Ẩn mật khẩu
        updatedUser.setPassword(null);

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .success(true)
                .message("Cập nhật hồ sơ thành công")
                .data(updatedUser)
                .build());
    }

    // Hàm tiện ích để lấy Username từ Token đang đăng nhập
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}