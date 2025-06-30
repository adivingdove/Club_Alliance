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
        
        // 使用项目根目录下的 uploads 文件夹
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        
        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成唯一文件名
        String filename = UUID.randomUUID().toString() + extension;
        File dest = new File(dir, filename);
        file.transferTo(dest);
        
        result.put("code", 0);
        result.put("url", "/uploads/" + filename);
        result.put("message", "上传成功");
        
        System.out.println("文件上传成功: " + dest.getAbsolutePath());
        System.out.println("访问URL: " + "/uploads/" + filename);
        
        return result;
    }
} 