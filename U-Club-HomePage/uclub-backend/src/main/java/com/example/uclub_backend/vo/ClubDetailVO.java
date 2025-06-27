package com.example.uclub_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClubDetailVO {
    private Integer id;
    private String name;
    private String logoUrl;
    private String tags;
    private Integer creatorId;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Integer type;
    private List<ClubMemberVO> members;
    private List<ClubActivityVO> activities;
    
    @Data
    public static class ClubMemberVO {
        private Integer id;
        private Integer userId;
        private String name;
        private String avatar;
        private String role;
        private String joinStatus;
        private LocalDateTime joinedAt;
    }
    
    @Data
    public static class ClubActivityVO {
        private Integer id;
        private String title;
        private String description;
        private String location;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer maxParticipants;
        private String status;
        private String img;
    }
} 