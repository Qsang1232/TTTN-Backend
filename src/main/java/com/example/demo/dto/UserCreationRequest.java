package com.example.demo.dto;

import com.example.demo.model.Role;
import lombok.Data;

@Data
public class UserCreationRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Role role; // ADMIN hoặc USER
}