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
        if (file.isEmpty()) throw new RuntimeException("上传文件为空");

        // 文件保存路径
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        Files.createDirectories(Paths.get(uploadDir));

        // 安全处理文件名
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("无效的文件名");
        }
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        String filename = UUID.randomUUID() + suffix;

        File dest = new File(uploadDir, filename);
        file.transferTo(dest);

        String imageUrl = "/uploads/" + filename;
        res.put("url", imageUrl);
        return ResponseEntity.ok(res);
    } catch (Exception e) {
        e.printStackTrace(); // 查看控制台报错
        res.put("error", "上传失败：" + e.getMessage());
        return ResponseEntity.status(500).body(res);
    }
}

}
