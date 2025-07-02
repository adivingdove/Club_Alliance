package com.example.uclub_backend.forum.vo;

import com.example.uclub_backend.forum.entity.ReportStatus;
import com.example.uclub_backend.forum.entity.TargetType;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class ReportVO {
    private Integer id;
    private Integer reporterId;         // 举报人ID
    private String reporterNickname;    // 举报人昵称（新增）
    private Integer targetId;           // 被举报目标ID（帖子/评论等）
    private TargetType targetType;      // 被举报目标类型
    private Integer reportedUserId;     // 被举报人ID
    private String reportedUserNickname;// 被举报人昵称
    private String reason;              // 举报理由
    private ReportStatus status;        // 状态：待处理 / 已处理
    private Timestamp createdAt;        // 创建时间
    private Integer postId;


    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }
    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }
    public Integer getReporterId() {
        return reporterId;
    }
    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }
    public Integer getTargetId() {
        return targetId;
    }
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }
    public TargetType getTargetType() {
        return targetType;
    }
    public void setReportedUserId(Integer reportedUserId) {
        this.reportedUserId = reportedUserId;
    }
    public Integer getReportedUserId() {
        return reportedUserId;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }
    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    public ReportStatus getStatus() {
        return status;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setPostId(Integer postId){ this.postId = postId; }
    public Integer getPostId(){ return postId; }
}