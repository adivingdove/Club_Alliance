package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "tags")
    private String tags;

    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClubStatus status = ClubStatus.待审核;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "type")
    private Integer type;

    public enum ClubStatus {
        正常, 待审核, 已封禁
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
