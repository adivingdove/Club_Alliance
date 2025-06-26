package com.example.uclub_backend.club.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "club")
@Data
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    private String tags;

    @Column(name = "creator_id")
    private Long creatorId;

    private String description;

    private String status;

    @Column(name = "created_at")
    private String createdAt;
}
