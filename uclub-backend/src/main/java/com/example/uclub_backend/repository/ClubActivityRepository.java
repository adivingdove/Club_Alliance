package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.ClubActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClubActivityRepository extends JpaRepository<ClubActivity, Integer> {
    
    List<ClubActivity> findByClubId(Integer clubId);
    
    List<ClubActivity> findByCreatorId(Integer creatorId);
    
    List<ClubActivity> findByApplyStatus(ClubActivity.ApplyStatus applyStatus);
    
    @Query("SELECT ca FROM ClubActivity ca WHERE ca.clubId = :clubId AND ca.applyStatus = :applyStatus")
    List<ClubActivity> findApprovedActivitiesByClubId(@Param("clubId") Integer clubId, @Param("applyStatus") ClubActivity.ApplyStatus applyStatus);
    
    @Query("SELECT ca FROM ClubActivity ca WHERE ca.startTime >= :startDate AND ca.applyStatus = :applyStatus")
    List<ClubActivity> findUpcomingActivities(@Param("startDate") LocalDateTime startDate, @Param("applyStatus") ClubActivity.ApplyStatus applyStatus);
    
    @Query("SELECT ca FROM ClubActivity ca WHERE ca.title LIKE %:keyword% OR ca.description LIKE %:keyword%")
    List<ClubActivity> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT ca FROM ClubActivity ca WHERE ca.startTime >= :startDate AND ca.startTime <= :endDate")
    List<ClubActivity> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ca FROM ClubActivity ca WHERE ca.applyStatus = :applyStatus")
    List<ClubActivity> findPendingActivities(@Param("applyStatus") ClubActivity.ApplyStatus applyStatus);
} 
