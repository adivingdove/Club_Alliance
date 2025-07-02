package com.example.uclub_backend.vo;

import com.example.uclub_backend.entity.Club;

public class ClubResponse {
    private Integer id;
    private String name;
    private Integer creatorId;
    private String creatorNickname; 
    private Club.ClubStatus status;

    // Getters and Setters
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }
    public String getCreatorNickname() {
        return creatorNickname;
    }
    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }
    public Club.ClubStatus getStatus() {
        return status;
    }
    public void setStatus(Club.ClubStatus status) {
        this.status = status;
    }
}