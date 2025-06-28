package com.example.uclub_backend.admin.service;

import com.example.uclub_backend.entity.Announcement;
import com.example.uclub_backend.admin.repository.AdminAnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAnnouncementService {

    private final AdminAnnouncementRepository repository;

    public AdminAnnouncementService(AdminAnnouncementRepository repository) {
        this.repository = repository;
    }

    // 获取所有系统公告
    public List<Announcement> getAllSystemAnnouncements() {
        return repository.findByClubIdIsNull();
    }

    // 根据ID获取系统公告
    public Announcement getSystemAnnouncementById(Integer id) {
        return repository.findByIdAndClubIdIsNull(id);
    }

    // 保存/更新系统公告，确保club字段为null
    public Announcement saveSystemAnnouncement(Announcement announcement) {
        announcement.setClubId(null); // 这里强制 club = null，表示系统公告
        return repository.save(announcement);
    }

    // 删除系统公告
    public void deleteSystemAnnouncement(Integer id) {
        Announcement ann = getSystemAnnouncementById(id);
        if (ann != null) {
            repository.delete(ann);
        }
    }
}
