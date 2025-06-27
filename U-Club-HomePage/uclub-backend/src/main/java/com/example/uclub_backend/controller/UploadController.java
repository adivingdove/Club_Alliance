package com.example.uclub_backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 500);
            result.put("message", "文件为空");
            return result;
        }
        // 绝对路径
        String uploadDir = "C:/Users/lenovo/MySelf/U-Club-HomePage/upload";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(dir, filename);
        file.transferTo(dest);
        result.put("code", 0);
        result.put("url", "/upload/" + filename);
        return result;
    }
} 