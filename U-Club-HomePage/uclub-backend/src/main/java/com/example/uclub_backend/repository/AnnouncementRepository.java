package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    
    List<Announcement> findByClubId(Integer clubId);
    
    List<Announcement> findByType(Announcement.AnnouncementType type);
    
    List<Announcement> findByCreatorId(Integer creatorId);
    
    @Query("SELECT a FROM Announcement a WHERE a.clubId IS NULL")
    List<Announcement> findSystemAnnouncements();
    
    @Query("SELECT a FROM Announcement a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword%")
    List<Announcement> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM Announcement a WHERE a.clubId = :clubId OR a.clubId IS NULL ORDER BY a.createdAt DESC")
    List<Announcement> findAnnouncementsForClub(@Param("clubId") Integer clubId);
} 