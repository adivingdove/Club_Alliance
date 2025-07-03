package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubActivity;
import com.example.uclub_backend.entity.ActivityParticipant;
import com.example.uclub_backend.service.ClubActivityService;
import com.example.uclub_backend.service.ActivityParticipantService;
import com.example.uclub_backend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.example.uclub_backend.service.ClubService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    
    @Autowired
    private ClubActivityService clubActivityService;
    
    @Autowired
    private ActivityParticipantService activityParticipantService;

    @Autowired
    private ClubService clubService;
    
    @GetMapping("/test")
    public Result<String> test() {
        try {
            System.out.println("测试端点被调用");
            return Result.success("活动API正常工作");
        } catch (Exception e) {
            System.err.println("测试端点出错: " + e.getMessage());
            e.printStackTrace();
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-simple")
    public Map<String, Object> testSimple() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("简单测试端点被调用");
            response.put("code", 0);
            response.put("message", "简单测试成功");
            response.put("data", "活动API正常工作");
        } catch (Exception e) {
            System.err.println("简单测试端点出错: " + e.getMessage());
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "测试失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/test-basic")
    public String testBasic() {
        System.out.println("基础测试端点被调用");
        return "活动API基础测试成功";
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/cors-test")
    public Map<String, Object> corsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS测试成功");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ActivityService");
        return health;
    }
    
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("测试数据库连接...");
            List<ClubActivity> activities = clubActivityService.getAllActivities();
            response.put("code", 0);
            response.put("message", "数据库连接正常");
            response.put("data", "活动数量: " + activities.size());
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "数据库连接失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping
    public Result<List<ClubActivity>> getAllActivities() {
        try {
            System.out.println("开始获取所有活动");
            List<ClubActivity> activities = clubActivityService.getAllActivities();
            System.out.println("成功获取活动数量: " + activities.size());
            
            // 打印每个活动的状态信息，便于调试
            for (ClubActivity activity : activities) {
                System.out.println("活动ID: " + activity.getId() + 
                                  ", 标题: " + activity.getTitle() + 
                                  ", 状态: " + activity.getApplyStatus());
            }
            
            return Result.success(activities);
        } catch (Exception e) {
            System.err.println("获取所有活动失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/club/{clubId}")
    public Result<List<ClubActivity>> getActivitiesByClubId(@PathVariable Integer clubId) {
        try {
            List<ClubActivity> activities = clubActivityService.getActivitiesByClubId(clubId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/club/{clubId}/approved")
    public Result<List<ClubActivity>> getApprovedActivitiesByClubId(@PathVariable Integer clubId) {
        try {
            List<ClubActivity> activities = clubActivityService.getApprovedActivitiesByClubId(clubId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/creator/{creatorId}")
    public Result<List<ClubActivity>> getActivitiesByCreatorId(@PathVariable Integer creatorId) {
        try {
            List<ClubActivity> activities = clubActivityService.getActivitiesByCreatorId(creatorId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/upcoming")
    public Result<List<ClubActivity>> getUpcomingActivities() {
        try {
            List<ClubActivity> activities = clubActivityService.getUpcomingActivities();
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result<List<ClubActivity>> getPendingActivities() {
        try {
            System.out.println("开始获取待审核活动");
            List<ClubActivity> activities = clubActivityService.getPendingActivities();
            System.out.println("成功获取待审核活动数量: " + activities.size());
            
            // 打印每个待审核活动的信息
            for (ClubActivity activity : activities) {
                System.out.println("待审核活动ID: " + activity.getId() + 
                                  ", 标题: " + activity.getTitle() + 
                                  ", 状态: " + activity.getApplyStatus());
            }
            
            return Result.success(activities);
        } catch (Exception e) {
            System.err.println("获取待审核活动失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取待审核活动失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/{status}")
    public Result<List<ClubActivity>> getActivitiesByApplyStatus(@PathVariable String status) {
        try {
            ClubActivity.ApplyStatus applyStatus = ClubActivity.ApplyStatus.valueOf(status);
            List<ClubActivity> activities = clubActivityService.getActivitiesByApplyStatus(applyStatus);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<List<ClubActivity>> searchActivities(@RequestParam String keyword) {
        try {
            List<ClubActivity> activities = clubActivityService.searchActivities(keyword);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/date-range")
    public Result<List<ClubActivity>> getActivitiesByDateRange(
            @RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<ClubActivity> activities = clubActivityService.getActivitiesByDateRange(start, end);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<ClubActivity> getActivityById(@PathVariable Integer id) {
        try {
            Optional<ClubActivity> activity = clubActivityService.getActivityById(id);
            if (activity.isPresent()) {
                return Result.success(activity.get());
            } else {
                return Result.error("活动不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<ClubActivity> createActivity(@RequestBody ClubActivity activity) {
        ClubActivity created = clubActivityService.createActivity(activity);
        return Result.success(created);
    }
    
    @PutMapping("/{id}")
    public Result<ClubActivity> updateActivity(@PathVariable Integer id, @RequestBody ClubActivity activity) {
        ClubActivity updated = clubActivityService.updateActivity(id, activity);
        return Result.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Integer id) {
        try {
            clubActivityService.deleteActivity(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/apply-status")
    public Result<Void> updateApplyStatus(@PathVariable Integer id, @RequestParam String applyStatus) {
        try {
            System.out.println("收到更新活动状态请求，活动ID: " + id + ", 状态: " + applyStatus);
            
            // 安全地处理中文枚举值
            ClubActivity.ApplyStatus status;
            switch (applyStatus) {
                case "通过":
                    status = ClubActivity.ApplyStatus.通过;
                    break;
                case "拒绝":
                    status = ClubActivity.ApplyStatus.拒绝;
                    break;
                case "待审核":
                    status = ClubActivity.ApplyStatus.待审核;
                    break;
                default:
                    return Result.error("无效的审核状态: " + applyStatus);
            }
            
            clubActivityService.updateApplyStatus(id, status);
            System.out.println("活动状态更新成功，活动ID: " + id + ", 新状态: " + status);
            return Result.success();
        } catch (Exception e) {
            System.err.println("更新活动状态失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新活动状态失败: " + e.getMessage());
        }
    }
    
    // 用户申请加入活动
    @PostMapping("/{id}/join")
    public Result<Void> joinActivity(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            System.out.println("用户 " + userId + " 申请加入活动 " + id);
            boolean success = activityParticipantService.joinActivity(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("加入活动失败");
            }
        } catch (Exception e) {
            System.err.println("加入活动失败: " + e.getMessage());
            return Result.error("加入活动失败: " + e.getMessage());
        }
    }
    
    // 用户退出活动
    @PostMapping("/{id}/leave")
    public Result<Void> leaveActivity(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            System.out.println("用户 " + userId + " 退出活动 " + id);
            boolean success = activityParticipantService.leaveActivity(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("退出活动失败");
            }
        } catch (Exception e) {
            System.err.println("退出活动失败: " + e.getMessage());
            return Result.error("退出活动失败: " + e.getMessage());
        }
    }
    
    // 获取活动的参与者列表
    @GetMapping("/{id}/participants")
    public Result<List<Map<String, Object>>> getActivityParticipants(@PathVariable Integer id) {
        try {
            List<ActivityParticipant> participants = activityParticipantService.getActivityParticipants(id);
            List<Map<String, Object>> result = participants.stream()
                .map(participant -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", participant.getId());
                    map.put("userId", participant.getUserId());
                    map.put("joinTime", participant.getJoinTime());
                    map.put("status", participant.getStatus());
                    return map;
                })
                .collect(Collectors.toList());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取参与者列表失败: " + e.getMessage());
        }
    }
    
    // 检查用户是否参与某个活动
    @GetMapping("/{id}/participating")
    public Result<Boolean> isUserParticipating(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            boolean isParticipating = activityParticipantService.isUserParticipating(id, userId);
            return Result.success(isParticipating);
        } catch (Exception e) {
            return Result.error("检查参与状态失败: " + e.getMessage());
        }
    }
    
    // 获取活动的当前参与人数
    @GetMapping("/{id}/participant-count")
    public Result<Long> getActivityParticipantCount(@PathVariable Integer id) {
        try {
            long count = activityParticipantService.getActivityParticipantCount(id);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取参与人数失败: " + e.getMessage());
        }
    }

    // 获取所有"历史记录"类型的活动（非待审核）
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistoryActivities() {
        List<ClubActivity> activities = clubActivityService.getHistoryActivities();

        // 提取所有 clubId
        List<Integer> clubIds = activities.stream()
            .map(ClubActivity::getClubId)
            .distinct()
            .collect(Collectors.toList());

        // 获取社团名称映射
        Map<Integer, String> clubNameMap = clubService.getClubNamesByIds(clubIds);

        List<Map<String, Object>> result = activities.stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", activity.getId());
            map.put("title", activity.getTitle());
            map.put("clubId", activity.getClubId());
            map.put("clubName", clubNameMap.getOrDefault(activity.getClubId(), "未知社团"));
            map.put("startTime", activity.getStartTime());
            map.put("endTime", activity.getEndTime());
            map.put("applyStatus", activity.getApplyStatus());
            map.put("createdAt", activity.getCreatedAt());
            map.put("description", activity.getDescription());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/admin/pending")
    public Result<List<Map<String, Object>>> getAdminPendingActivities() {
        List<ClubActivity> activities = clubActivityService.getPendingActivities();

        // 提取所有 clubId
        List<Integer> clubIds = activities.stream()
            .map(ClubActivity::getClubId)
            .distinct()
            .collect(Collectors.toList());

        // 获取社团名称映射
        Map<Integer, String> clubNameMap = clubService.getClubNamesByIds(clubIds);

        List<Map<String, Object>> result = activities.stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", activity.getId());
            map.put("title", activity.getTitle());
            map.put("clubId", activity.getClubId());
            map.put("clubName", clubNameMap.getOrDefault(activity.getClubId(), "未知社团"));
            map.put("startTime", activity.getStartTime());
            map.put("endTime", activity.getEndTime());
            map.put("applyStatus", activity.getApplyStatus());
            map.put("createdAt", activity.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }


} 
