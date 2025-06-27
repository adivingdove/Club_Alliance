package com.example.uclub_backend.service;

import com.example.uclub_backend.entity.Announcement;
import com.example.uclub_backend.repository.AnnouncementRepository;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }
    
    public List<Announcement> getAnnouncementsByClubId(Integer clubId) {
        return announcementRepository.findByClubId(clubId);
    }
    
    public List<Announcement> getAnnouncementsByType(Announcement.AnnouncementType type) {
        return announcementRepository.findByType(type);
    }
    
    public List<Announcement> getAnnouncementsByCreatorId(Integer creatorId) {
        return announcementRepository.findByCreatorId(creatorId);
    }
    
    public List<Announcement> getSystemAnnouncements() {
        return announcementRepository.findSystemAnnouncements();
    }
    
    public List<Announcement> searchAnnouncements(String keyword) {
        return announcementRepository.findByKeyword(keyword);
    }
    
    public List<Announcement> getAnnouncementsForClub(Integer clubId) {
        return announcementRepository.findAnnouncementsForClub(clubId);
    }
    
    public Optional<Announcement> getAnnouncementById(Integer id) {
        return announcementRepository.findById(id);
    }
    
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        // 检查创建者是否存在
        if (!userRepository.existsById(announcement.getCreatorId())) {
            throw new RuntimeException("创建者不存在");
        }
        
        // 如果指定了社团ID，检查社团是否存在
        if (announcement.getClubId() != null && !clubRepository.existsById(announcement.getClubId())) {
            throw new RuntimeException("社团不存在");
        }
        
        return announcementRepository.save(announcement);
    }
    
    @Transactional
    public Announcement updateAnnouncement(Integer id, Announcement announcement) {
        Optional<Announcement> existingAnnouncement = announcementRepository.findById(id);
        if (existingAnnouncement.isEmpty()) {
            throw new RuntimeException("公告不存在");
        }
        
        Announcement announcementToUpdate = existingAnnouncement.get();
        announcementToUpdate.setTitle(announcement.getTitle());
        announcementToUpdate.setContent(announcement.getContent());
        announcementToUpdate.setType(announcement.getType());
        announcementToUpdate.setClubId(announcement.getClubId());
        
        return announcementRepository.save(announcementToUpdate);
    }
    
    @Transactional
    public void deleteAnnouncement(Integer id) {
        if (!announcementRepository.existsById(id)) {
            throw new RuntimeException("公告不存在");
        }
        announcementRepository.deleteById(id);
    }
} 