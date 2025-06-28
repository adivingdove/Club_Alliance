package com.example.uclub_backend.forum.controller;
import com.example.uclub_backend.forum.entity.Report;
import com.example.uclub_backend.forum.entity.ReportStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.example.uclub_backend.forum.mapper.ReportMapper;
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportMapper reportMapper;

    @PostMapping
    public ResponseEntity<?> report(@RequestBody Report report) {
        System.out.println(" 收到举报请求：" + report);
        try {
            // 强制设置为“待处理”
            report.setStatus(ReportStatus.待处理);

            // 插入数据库
            reportMapper.insertReport(report);

            return ResponseEntity.ok().body(Map.of("message", "举报成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "参数非法", "detail", e.getMessage()));
        } catch (Exception e) {
             e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("error", "举报失败", "detail", e.getMessage()));
        }
    }
}
