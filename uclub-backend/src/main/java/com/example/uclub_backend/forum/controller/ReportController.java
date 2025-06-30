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

    // 获取举报记录
    @GetMapping("/list")
    public ResponseEntity<?> getReportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,   // 可选：待处理、已处理等
            @RequestParam(required = false) String keyword   // 可选：举报内容关键字
    ) {
        try {
            int offset = (page - 1) * size;
            List<Report> reports = reportMapper.selectReports(status, keyword, offset, size);
            int total = reportMapper.countReports(status, keyword);
            return ResponseEntity.ok(Map.of("data", reports, "total", total));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "查询失败", "detail", e.getMessage()));
        }
    }

    // 处理举报
    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateReportStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        try {
            ReportStatus enumStatus = ReportStatus.valueOf(status);
            reportMapper.updateReportStatus(id, enumStatus);
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "状态更新失败", "detail", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        try {
            Report report = reportMapper.getReportById(id);
            if (report == null) {
                return ResponseEntity.status(404).body(Map.of("error", "举报记录不存在"));
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "查询失败", "detail", e.getMessage()));
        }
    }

}
