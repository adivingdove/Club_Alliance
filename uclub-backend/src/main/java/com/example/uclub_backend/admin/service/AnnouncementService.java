package com.example.uclub_backend.admin.service;

import com.example.uclub_backend.admin.dto.AnnouncementDTO;
import com.example.uclub_backend.admin.entity.Announcement;
import com.example.uclub_backend.admin.repository.AnnouncementRepository;
import com.example.uclub_backend.model.User;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.uclub_backend.admin.dto.AnnouncementDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;


    public Announcement publishSystemAnnouncement(String title, String content, Integer creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("发布人不存在"));

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(Announcement.Type.系统);
        announcement.setClub(null);
        announcement.setCreator(creator);
        announcement.setCreatedAt(LocalDateTime.now());

        return announcementRepository.save(announcement);
    }



    public List<AnnouncementDTO> getSystemAnnouncementDTOs() {
        List<Announcement> list = announcementRepository.findByClubIdIsNullOrderByCreatedAtDesc();
        return list.stream().map(a -> {
            AnnouncementDTO dto = new AnnouncementDTO();
            Integer aid = a.getId();
            dto.setId(aid != null ? aid.longValue() : null);
            dto.setTitle(a.getTitle());
            dto.setContent(a.getContent());
            dto.setCreatedAt(a.getCreatedAt());
            dto.setCreatorName(a.getCreator() != null ? a.getCreator().getNickname() : "未知");
            return dto;
        }).collect(Collectors.toList());
    }



}

