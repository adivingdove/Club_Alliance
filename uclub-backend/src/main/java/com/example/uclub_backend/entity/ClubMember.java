package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club_member")
public class ClubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role = MemberRole.成员;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_status")
    private JoinStatus joinStatus = JoinStatus.待审核;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "collect")
    private Integer collect = 0; // 0表示未收藏，1表示已收藏

    @Column(name = "applicant_info")
    private String applicantInfo; // 申请人信息

    @Column(name = "apply_reason")
    private String applyReason; // 申请理由

    public enum MemberRole {
        成员, 干事, 副社长, 社长
    }

    public enum JoinStatus {
        待审核, 已通过, 已拒绝
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
