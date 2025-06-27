package com.example.uclub_backend.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/admin/upload")
@CrossOrigin
public class SystemUploadController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadSystemImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();

        try {
            // 保存路径专属 system 文件夹
            String uploadDir = System.getProperty("user.dir") + "/uploads/system";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            String filename = UUID.randomUUID() + suffix;

            File dest = new File(uploadDir, filename);
            file.transferTo(dest);

            // 对应静态资源映射：/uploads/system/xxx.jpg
            String imageUrl = "/uploads/system/" + filename;

            res.put("url", imageUrl);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("error", "上传失败：" + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}
