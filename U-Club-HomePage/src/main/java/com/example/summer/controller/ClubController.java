package com.example.summer.controller;

import com.example.summer.entity.Club;
import com.example.summer.service.ClubService;
import com.example.summer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "*")
public class ClubController {
    
    @Autowired
    private ClubService clubService;
    
    @GetMapping
    public Result<List<Club>> getAllClubs() {
        try {
            List<Club> clubs = clubService.getAllClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/active")
    public Result<List<Club>> getActiveClubs() {
        try {
            List<Club> clubs = clubService.getActiveClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result<List<Club>> getPendingClubs() {
        try {
            List<Club> clubs = clubService.getPendingClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/creator/{creatorId}")
    public Result<List<Club>> getClubsByCreatorId(@PathVariable Integer creatorId) {
        try {
            List<Club> clubs = clubService.getClubsByCreatorId(creatorId);
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<List<Club>> searchClubs(@RequestParam String keyword) {
        try {
            List<Club> clubs = clubService.searchClubs(keyword);
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Club> getClubById(@PathVariable Integer id) {
        try {
            Club club = clubService.getClubById(id).orElse(null);
            if (club == null) {
                return Result.error(404, "社团不存在");
            }
            return Result.success(club);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<Club> createClub(@RequestBody Club club) {
        try {
            Club createdClub = clubService.createClub(club);
            return Result.success(createdClub);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Club> updateClub(@PathVariable Integer id, @RequestBody Club club) {
        try {
            Club updatedClub = clubService.updateClub(id, club);
            return Result.success(updatedClub);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteClub(@PathVariable Integer id) {
        try {
            clubService.deleteClub(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateClubStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Club.ClubStatus clubStatus = Club.ClubStatus.valueOf(status);
            clubService.updateClubStatus(id, clubStatus);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/apply")
    public Result<Void> applyToClub(@RequestBody Map<String, Object> applyForm) {
        try {
            Integer clubId = applyForm.get("clubId") != null ? Integer.valueOf(applyForm.get("clubId").toString()) : null;
            Integer creatorId = applyForm.get("creatorId") != null ? Integer.valueOf(applyForm.get("creatorId").toString()) : null;
            String applicant = (String) applyForm.get("applicant");
            String reason = (String) applyForm.get("reason");
            clubService.handleClubApplication(clubId, creatorId, applicant, reason);
            System.out.println("applyToClub 返回 Result.success()");
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 