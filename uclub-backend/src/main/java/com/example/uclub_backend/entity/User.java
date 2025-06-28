package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "head_url")
    private String headUrl;

    @Column(nullable = false, unique = true)
    private String account;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.普通用户;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.正常;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum UserRole {
        普通用户, 社团管理员, 系统管理员
    }

    public enum UserStatus {
        正常, 禁言, 封禁
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 