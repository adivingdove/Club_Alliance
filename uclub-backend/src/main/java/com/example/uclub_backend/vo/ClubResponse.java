package com.example.uclub_backend.vo;

import com.example.uclub_backend.entity.Club;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClubResponse {
    private Integer id;
    private String name;
    private Integer creatorId;
    private String creatorNickname;
    private Club.ClubStatus status;
    private String description;
    private LocalDateTime createdAt;
}
