package com.example.uclub_backend.service;

import com.example.uclub_backend.entity.ActivityParticipant;
import com.example.uclub_backend.entity.ClubActivity;
import com.example.uclub_backend.repository.ActivityParticipantRepository;
import com.example.uclub_backend.repository.ClubActivityRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityParticipantService {
    
    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;
    
    @Autowired
    private ClubActivityRepository clubActivityRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 用户申请加入活动
    @Transactional
    public boolean joinActivity(Integer activityId, Integer userId) {
        // 检查活动是否存在
        Optional<ClubActivity> activityOpt = clubActivityRepository.findById(activityId);
        if (activityOpt.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }
        
        ClubActivity activity = activityOpt.get();
        
        // 检查活动是否已通过审核
        if (activity.getApplyStatus() != ClubActivity.ApplyStatus.通过) {
            throw new RuntimeException("活动尚未通过审核，无法加入");
        }
        
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户是否已经参与该活动
        if (activityParticipantRepository.existsByActivityIdAndUserId(activityId, userId)) {
            throw new RuntimeException("您已经参与该活动");
        }
        
        // 检查活动人数是否已满
        if (activity.getMaxParticipants() != null) {
            long currentCount = activityParticipantRepository.countActiveParticipantsByActivityId(activityId);
            if (currentCount >= activity.getMaxParticipants()) {
                throw new RuntimeException("活动人数已满，无法加入");
            }
        }
        
        // 创建参与记录
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivityId(activityId);
        participant.setUserId(userId);
        participant.setStatus(ActivityParticipant.ParticipantStatus.已加入);
        
        activityParticipantRepository.save(participant);
        
        // 更新活动的当前参与人数
        updateActivityParticipantCount(activityId);
        
        return true;
    }
    
    // 用户退出活动
    @Transactional
    public boolean leaveActivity(Integer activityId, Integer userId) {
        Optional<ActivityParticipant> participantOpt = activityParticipantRepository.findByActivityIdAndUserId(activityId, userId);
        if (participantOpt.isEmpty()) {
            throw new RuntimeException("您未参与该活动");
        }
        
        ActivityParticipant participant = participantOpt.get();
        participant.setStatus(ActivityParticipant.ParticipantStatus.已退出);
        activityParticipantRepository.save(participant);
        
        // 更新活动的当前参与人数
        updateActivityParticipantCount(activityId);
        
        return true;
    }
    
    // 获取活动的参与者列表
    public List<ActivityParticipant> getActivityParticipants(Integer activityId) {
        return activityParticipantRepository.findByActivityIdAndStatus(activityId, ActivityParticipant.ParticipantStatus.已加入);
    }
    
    // 获取用户参与的活动列表
    public List<ActivityParticipant> getUserParticipatedActivities(Integer userId) {
        return activityParticipantRepository.findByUserId(userId);
    }
    
    // 检查用户是否参与某个活动
    public boolean isUserParticipating(Integer activityId, Integer userId) {
        return activityParticipantRepository.existsByActivityIdAndUserId(activityId, userId);
    }
    
    // 获取活动的当前参与人数
    public long getActivityParticipantCount(Integer activityId) {
        return activityParticipantRepository.countActiveParticipantsByActivityId(activityId);
    }
    
    // 更新活动的当前参与人数
    @Transactional
    public void updateActivityParticipantCount(Integer activityId) {
        long count = activityParticipantRepository.countActiveParticipantsByActivityId(activityId);
        Optional<ClubActivity> activityOpt = clubActivityRepository.findById(activityId);
        if (activityOpt.isPresent()) {
            ClubActivity activity = activityOpt.get();
            activity.setCurrentParticipants((int) count);
            clubActivityRepository.save(activity);
        }
    }
} 