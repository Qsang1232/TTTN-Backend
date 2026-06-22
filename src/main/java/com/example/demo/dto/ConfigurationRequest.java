package com.example.demo.dto; // Đảm bảo đúng package

import lombok.Data;
import java.util.Set;

@Data
public class ConfigurationRequest {
    private Long carModelId;
    private Set<Long> optionIds;
    private String selectedColor;
}