package com.example.demo.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String phone;
    private String address;
    // Không cho phép update username, password, role ở đây
}