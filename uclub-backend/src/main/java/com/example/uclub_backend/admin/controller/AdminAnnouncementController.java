package com.example.uclub_backend.admin.controller;

import com.example.uclub_backend.entity.Announcement;
import com.example.uclub_backend.admin.service.AdminAnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/system-announcements")
public class AdminAnnouncementController {

    private final AdminAnnouncementService service;

    public AdminAnnouncementController(AdminAnnouncementService service) {
        this.service = service;
    }

    // 查询所有系统公告
    @GetMapping
    public List<Announcement> list() {
        return service.getAllSystemAnnouncements();
    }

    // 查询指定ID系统公告
    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getById(@PathVariable Integer id) {
        Announcement ann = service.getSystemAnnouncementById(id);
        if (ann == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ann);
    }

    // 新建或更新系统公告
    @PostMapping
    public Announcement createOrUpdate(@RequestBody Announcement announcement) {
        return service.saveSystemAnnouncement(announcement);
    }

    // 删除系统公告
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteSystemAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
