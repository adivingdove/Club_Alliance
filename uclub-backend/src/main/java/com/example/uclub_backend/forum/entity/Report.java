package com.example.uclub_backend.forum.entity;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class Report {
    private Integer id;
    private Integer reporterId;
    private TargetType targetType; // 枚举
    private Integer targetId;
    private String reason;
    private ReportStatus status;   // 枚举
    private Timestamp createdAt;
}




