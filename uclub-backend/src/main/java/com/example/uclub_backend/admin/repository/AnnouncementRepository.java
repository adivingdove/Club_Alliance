package com.example.uclub_backend.admin.repository;

import com.example.uclub_backend.admin.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    List<Announcement> findByType(Announcement.Type type);

    List<Announcement> findByClubIdIsNullOrderByCreatedAtDesc();


}
