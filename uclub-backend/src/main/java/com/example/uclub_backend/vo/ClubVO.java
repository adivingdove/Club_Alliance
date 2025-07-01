package com.example.uclub_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClubVO {
    private Long id;
    private String name;
    private String description;
    private String tags;
    private Integer maxMembers;
    private Integer currentMembers;
    private String presidentName;
    private String presidentPhone;
    private String presidentEmail;
    private String advisorName;
    private String advisorPhone;
    private String meetingTime;
    private String meetingLocation;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String status;
}