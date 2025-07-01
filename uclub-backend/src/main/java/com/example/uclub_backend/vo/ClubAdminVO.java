package com.example.uclub_backend.vo;

import lombok.Data;

@Data
public class ClubAdminVO {
    private Integer id;
    private Integer userId;
    private String userName;
    private Integer clubId;
    private String clubName;
    private String role;
}
