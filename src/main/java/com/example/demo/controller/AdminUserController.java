package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UserCreationRequest;
import com.example.demo.model.User;
import com.example.demo.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthenticationService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody @Valid UserCreationRequest request) {
        // Map DTO sang Entity
        User newUser = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) 
                .role(request.getRole())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        User createdUser = authService.createAccountByAdmin(newUser);
        
        // Ẩn mật khẩu khi trả về JSON để bảo mật
        createdUser.setPassword(null); 

        return new ResponseEntity<>(ApiResponse.<User>builder()
                .success(true)
                .message("Tạo tài khoản thành công")
                .data(createdUser)
                .build(), HttpStatus.CREATED);
    }
}