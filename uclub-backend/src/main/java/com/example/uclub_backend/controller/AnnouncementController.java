package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.Announcement;
import com.example.uclub_backend.service.AnnouncementService;
import com.example.uclub_backend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> getAllAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/club/{clubId}")
    public Result<List<Announcement>> getAnnouncementsByClubId(@PathVariable Integer clubId) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsByClubId(clubId);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/club/{clubId}/all")
    public Result<List<Announcement>> getAnnouncementsForClub(@PathVariable Integer clubId) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsForClub(clubId);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public Result<List<Announcement>> getAnnouncementsByType(@PathVariable String type) {
        try {
            Announcement.AnnouncementType announcementType = Announcement.AnnouncementType.valueOf(type);
            List<Announcement> announcements = announcementService.getAnnouncementsByType(announcementType);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/creator/{creatorId}")
    public Result<List<Announcement>> getAnnouncementsByCreatorId(@PathVariable Integer creatorId) {
        try {
            List<Announcement> announcements = announcementService.getAnnouncementsByCreatorId(creatorId);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/system")
    public Result<List<Announcement>> getSystemAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getSystemAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<List<Announcement>> searchAnnouncements(@RequestParam String keyword) {
        try {
            List<Announcement> announcements = announcementService.searchAnnouncements(keyword);
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Integer id) {
        try {
            Announcement announcement = announcementService.getAnnouncementById(id).orElse(null);
            if (announcement == null) {
                return Result.error(404, "公告不存在");
            }
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            Announcement createdAnnouncement = announcementService.createAnnouncement(announcement);
            return Result.success(createdAnnouncement);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable Integer id, @RequestBody Announcement announcement) {
        try {
            Announcement updatedAnnouncement = announcementService.updateAnnouncement(id, announcement);
            return Result.success(updatedAnnouncement);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Integer id) {
        try {
            announcementService.deleteAnnouncement(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}