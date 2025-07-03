package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Integer> {
    
    // 根据活动ID查找所有参与者
    List<ActivityParticipant> findByActivityId(Integer activityId);
    
    // 根据用户ID查找用户参与的所有活动
    List<ActivityParticipant> findByUserId(Integer userId);
    
    // 根据用户ID和状态查找用户参与的活动
    List<ActivityParticipant> findByUserIdAndStatus(Integer userId, ActivityParticipant.ParticipantStatus status);
    
    // 根据活动ID和用户ID查找特定参与记录
    Optional<ActivityParticipant> findByActivityIdAndUserId(Integer activityId, Integer userId);
    
    // 检查用户是否已参与某个活动（仅限已加入状态）
    boolean existsByActivityIdAndUserIdAndStatus(Integer activityId, Integer userId, ActivityParticipant.ParticipantStatus status);
    
    // 统计活动的当前参与人数
    @Query("SELECT COUNT(ap) FROM ActivityParticipant ap WHERE ap.activityId = :activityId AND ap.status = '已加入'")
    long countActiveParticipantsByActivityId(@Param("activityId") Integer activityId);
    
    // 根据活动ID和状态查找参与者
    List<ActivityParticipant> findByActivityIdAndStatus(Integer activityId, ActivityParticipant.ParticipantStatus status);
    
    // 查找所有指定活动、用户、且状态为已加入的参与记录
    List<ActivityParticipant> findAllByActivityIdAndUserIdAndStatus(Integer activityId, Integer userId, ActivityParticipant.ParticipantStatus status);
} 