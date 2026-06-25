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

import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthenticationService authService;
    private final UserRepository userRepository;

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

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<User>>> getAllUsers() {
        java.util.List<User> users = userRepository.findAll();
        // Ẩn mật khẩu
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.<java.util.List<User>>builder()
                .success(true)
                .data(users)
                .build());
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Không cho phép khóa chính tài khoản Admin đang login (có thể kiểm tra thêm role nếu cần)
        if (user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Không thể khóa tài khoản Admin!");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
        user.setPassword(null);

        return ResponseEntity.ok(ApiResponse.<User>builder()
                .success(true)
                .message(user.isActive() ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản")
                .data(user)
                .build());
    }
}