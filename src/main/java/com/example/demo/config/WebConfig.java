package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL "/uploads/**" tới thư mục "uploads" ở thư mục gốc của project backend
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}