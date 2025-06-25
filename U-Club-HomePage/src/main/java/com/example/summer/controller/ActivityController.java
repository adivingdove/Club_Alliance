package com.example.summer.controller;

import com.example.summer.entity.ClubActivity;
import com.example.summer.service.ClubActivityService;
import com.example.summer.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {
    
    @Autowired
    private ClubActivityService clubActivityService;
    
    @GetMapping
    public Result<List<ClubActivity>> getAllActivities() {
        try {
            List<ClubActivity> activities = clubActivityService.getAllActivities();
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/club/{clubId}")
    public Result<List<ClubActivity>> getActivitiesByClubId(@PathVariable Integer clubId) {
        try {
            List<ClubActivity> activities = clubActivityService.getActivitiesByClubId(clubId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/club/{clubId}/approved")
    public Result<List<ClubActivity>> getApprovedActivitiesByClubId(@PathVariable Integer clubId) {
        try {
            List<ClubActivity> activities = clubActivityService.getApprovedActivitiesByClubId(clubId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/creator/{creatorId}")
    public Result<List<ClubActivity>> getActivitiesByCreatorId(@PathVariable Integer creatorId) {
        try {
            List<ClubActivity> activities = clubActivityService.getActivitiesByCreatorId(creatorId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/upcoming")
    public Result<List<ClubActivity>> getUpcomingActivities() {
        try {
            List<ClubActivity> activities = clubActivityService.getUpcomingActivities();
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result<List<ClubActivity>> getPendingActivities() {
        try {
            List<ClubActivity> activities = clubActivityService.getPendingActivities();
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/status/{status}")
    public Result<List<ClubActivity>> getActivitiesByApplyStatus(@PathVariable String status) {
        try {
            ClubActivity.ApplyStatus applyStatus = ClubActivity.ApplyStatus.valueOf(status);
            List<ClubActivity> activities = clubActivityService.getActivitiesByApplyStatus(applyStatus);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<List<ClubActivity>> searchActivities(@RequestParam String keyword) {
        try {
            List<ClubActivity> activities = clubActivityService.searchActivities(keyword);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/date-range")
    public Result<List<ClubActivity>> getActivitiesByDateRange(
            @RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<ClubActivity> activities = clubActivityService.getActivitiesByDateRange(start, end);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<ClubActivity> getActivityById(@PathVariable Integer id) {
        try {
            ClubActivity activity = clubActivityService.getActivityById(id).orElse(null);
            if (activity == null) {
                return Result.error(404, "活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<ClubActivity> createActivity(@RequestBody ClubActivity activity) {
        try {
            ClubActivity createdActivity = clubActivityService.createActivity(activity);
            return Result.success(createdActivity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<ClubActivity> updateActivity(@PathVariable Integer id, @RequestBody ClubActivity activity) {
        try {
            ClubActivity updatedActivity = clubActivityService.updateActivity(id, activity);
            return Result.success(updatedActivity);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Integer id) {
        try {
            clubActivityService.deleteActivity(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/apply-status")
    public Result<Void> updateApplyStatus(@PathVariable Integer id, @RequestParam String applyStatus) {
        try {
            ClubActivity.ApplyStatus status = ClubActivity.ApplyStatus.valueOf(applyStatus);
            clubActivityService.updateApplyStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 