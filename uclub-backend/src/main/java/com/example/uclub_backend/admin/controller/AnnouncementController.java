package com.example.uclub_backend.admin.controller;

import com.example.uclub_backend.admin.entity.Announcement;
import com.example.uclub_backend.admin.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.uclub_backend.admin.dto.AnnouncementDTO;


@RestController
@RequestMapping("/api/admin/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/system")
    public ResponseEntity<?> publishSystemAnnouncement(@RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        Integer creatorId = (Integer) payload.get("creatorId");

        if (title == null || content == null || creatorId == null) {
            return ResponseEntity.badRequest().body("缺少参数");
        }

        Announcement announcement = announcementService.publishSystemAnnouncement(title, content, creatorId);
        return ResponseEntity.ok(announcement);
    }

    @GetMapping("/system")
    public ResponseEntity<List<AnnouncementDTO>> getSystemAnnouncements() {
        List<AnnouncementDTO> dtos = announcementService.getSystemAnnouncementDTOs();
        return ResponseEntity.ok(dtos);
    }

}

