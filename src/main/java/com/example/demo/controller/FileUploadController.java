package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không tồn tại");
        }
        try {
            // Thư mục lưu trữ thực tế trên server
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 1. Lấy tên file gốc
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) originalFilename = "unknown.jpg";

            // 2. Chuyển tiếng Việt có dấu thành không dấu (sân bóng -> san bong)
            String noAccent = Normalizer.normalize(originalFilename, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            // 3. Thay thế khoảng trắng và ký tự đặc biệt bằng dấu gạch dưới, chuyển thành chữ thường
            // (Chỉ giữ lại chữ cái, số, dấu chấm và dấu gạch ngang)
            String safeFilename = noAccent.replaceAll("[^a-zA-Z0-9\\.\\-]", "_").toLowerCase();

            // 4. Ghép UUID để đảm bảo không bao giờ trùng tên file
            String finalFileName = UUID.randomUUID().toString() + "_" + safeFilename;

            // 5. Lưu file vào ổ cứng
            File dest = new File(uploadDir + finalFileName);
            file.transferTo(dest);

            // 6. Trả về đường dẫn để Frontend lưu vào Database
            String relativePath = "/uploads/" + finalFileName;
            return ResponseEntity.ok(Map.of("url", relativePath));
            
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }
}