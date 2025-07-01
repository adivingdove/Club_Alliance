package com.example.uclub_backend.service;

import com.example.uclub_backend.entity.ClubActivity;
import com.example.uclub_backend.mapper.ClubActivityMapper;
import com.example.uclub_backend.repository.ClubActivityRepository;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClubActivityService {
    
    @Autowired
    private ClubActivityRepository clubActivityRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubActivityMapper clubActivityMapper;
    
    public List<ClubActivity> getAllActivities() {
        return clubActivityRepository.findAll();
    }

    public List<ClubActivity> getHistoryActivities() {
        return clubActivityMapper.findByApplyStatusNot("待审核");
    }

    public void updateApplyStatus(Long id, String status) {
        clubActivityMapper.updateApplyStatus(id, status, LocalDateTime.now());
    }
    public List<ClubActivity> getActivitiesByClubId(Integer clubId) {
        return clubActivityRepository.findByClubId(clubId);
    }
    
    public List<ClubActivity> getApprovedActivitiesByClubId(Integer clubId) {
        return clubActivityRepository.findApprovedActivitiesByClubId(clubId, ClubActivity.ApplyStatus.通过);
    }
    
    public List<ClubActivity> getActivitiesByCreatorId(Integer creatorId) {
        return clubActivityRepository.findByCreatorId(creatorId);
    }
    
    public List<ClubActivity> getUpcomingActivities() {
        return clubActivityRepository.findUpcomingActivities(LocalDateTime.now(), ClubActivity.ApplyStatus.通过);
    }
    
    public List<ClubActivity> getPendingActivities() {
        return clubActivityRepository.findPendingActivities(ClubActivity.ApplyStatus.待审核);
    }
    
    public List<ClubActivity> getActivitiesByApplyStatus(ClubActivity.ApplyStatus applyStatus) {
        return clubActivityRepository.findByApplyStatus(applyStatus);
    }
    
    public List<ClubActivity> searchActivities(String keyword) {
        return clubActivityRepository.findByKeyword(keyword);
    }
    
    public List<ClubActivity> getActivitiesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return clubActivityRepository.findByDateRange(startDate, endDate);
    }
    
    public Optional<ClubActivity> getActivityById(Integer id) {
        return clubActivityRepository.findById(id);
    }
    
    @Transactional
    public ClubActivity createActivity(ClubActivity activity) {
        // 检查社团是否存在
        if (!clubRepository.existsById(activity.getClubId())) {
            throw new RuntimeException("社团不存在");
        }
        
        // 检查创建者是否存在
        if (!userRepository.existsById(activity.getCreatorId())) {
            throw new RuntimeException("创建者不存在");
        }
        
        // 检查时间逻辑
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            if (activity.getStartTime().isAfter(activity.getEndTime())) {
                throw new RuntimeException("开始时间不能晚于结束时间");
            }
        }
        
        return clubActivityRepository.save(activity);
    }
    
    @Transactional
    public ClubActivity updateActivity(Integer id, ClubActivity activity) {
        Optional<ClubActivity> existingActivity = clubActivityRepository.findById(id);
        if (existingActivity.isEmpty()) {
            throw new RuntimeException("活动不存在");
        }
        
        ClubActivity activityToUpdate = existingActivity.get();
        activityToUpdate.setTitle(activity.getTitle());
        activityToUpdate.setDescription(activity.getDescription());
        activityToUpdate.setLocation(activity.getLocation());
        activityToUpdate.setStartTime(activity.getStartTime());
        activityToUpdate.setEndTime(activity.getEndTime());
        activityToUpdate.setMaxParticipants(activity.getMaxParticipants());
        activityToUpdate.setApplyStatus(activity.getApplyStatus());
        
        return clubActivityRepository.save(activityToUpdate);
    }
    
    @Transactional
    public void deleteActivity(Integer id) {
        if (!clubActivityRepository.existsById(id)) {
            throw new RuntimeException("活动不存在");
        }
        clubActivityRepository.deleteById(id);
    }
    
    @Transactional
    public void updateApplyStatus(Integer id, ClubActivity.ApplyStatus applyStatus) {
        System.out.println("开始更新活动状态，活动ID: " + id + ", 新状态: " + applyStatus);
        
        Optional<ClubActivity> activity = clubActivityRepository.findById(id);
        if (activity.isEmpty()) {
            System.err.println("活动不存在，ID: " + id);
            throw new RuntimeException("活动不存在");
        }
        
        ClubActivity activityToUpdate = activity.get();
        ClubActivity.ApplyStatus oldStatus = activityToUpdate.getApplyStatus();
        System.out.println("活动当前状态: " + oldStatus + ", 即将更新为: " + applyStatus);
        
        activityToUpdate.setApplyStatus(applyStatus);
        ClubActivity savedActivity = clubActivityRepository.save(activityToUpdate);
        
        System.out.println("活动状态更新成功，活动ID: " + savedActivity.getId() + 
                          ", 标题: " + savedActivity.getTitle() + 
                          ", 新状态: " + savedActivity.getApplyStatus());
    }


    public List<ClubActivity> getHistoryByClubId(Integer clubId) {
        return clubActivityMapper.selectHistoryByClubId(clubId);
    }

      public ClubActivity getById(Integer activityId) {
        return clubActivityMapper.selectById(activityId);
    }
} 
