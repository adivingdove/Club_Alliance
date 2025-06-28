package com.example.uclub_backend.forum.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin // 允许跨域
public class ForumUploadController {

    // 上传接口（路径为：/api/forum/upload）
    @PostMapping("/forum/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> res = new HashMap<>();

        try {
            // 文件保存路径（项目根目录下的 uploads 文件夹）
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            Files.createDirectories(Paths.get(uploadDir)); // 自动创建目录

            // 生成唯一文件名
            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String suffix = originalName.substring(originalName.lastIndexOf("."));
            String filename = UUID.randomUUID() + suffix;

            // 写入磁盘
            File dest = new File(uploadDir, filename);
            file.transferTo(dest);

            // 访问地址（配合静态资源映射）
            String imageUrl = "/uploads/" + filename;

            res.put("url", imageUrl);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("error", "上传失败：" + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}
