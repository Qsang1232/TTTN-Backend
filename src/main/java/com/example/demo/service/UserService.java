package com.example.demo.service;

import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --- CÁC HÀM CŨ (GIỮ NGUYÊN) ---
    public List<User> getAllUsers() { return userRepository.findAll(); }
    public User getUserById(Long id) { return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    public void deleteUser(Long id) { userRepository.deleteById(id); }
    // --------------------------------

    // --- CÁC HÀM MỚI CHO USER CONTROLLER ---

    // 1. Lấy thông tin user theo username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại: " + username));
    }

    // 2. User tự cập nhật hồ sơ
    public User updateUserProfile(String username, UserUpdateRequest request) {
        User user = getUserByUsername(username);

        // Chỉ cập nhật các trường cho phép
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());

        return userRepository.save(user);
    }
}