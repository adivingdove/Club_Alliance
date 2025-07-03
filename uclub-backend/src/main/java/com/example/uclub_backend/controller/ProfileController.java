package com.example.uclub_backend.controller;

import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.ClubActivity;
import com.example.uclub_backend.entity.ActivityParticipant;
import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.repository.ClubActivityRepository;
import com.example.uclub_backend.repository.ActivityParticipantRepository;
import com.example.uclub_backend.forum.repository.PostRepository;
import com.example.uclub_backend.service.EmailService;
import com.example.uclub_backend.service.ActivityParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ClubActivityRepository clubActivityRepository;

    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ActivityParticipantService activityParticipantService;

    // 验证码缓存（建议生产用Redis，这里用Map演示）
    private Map<String, String> oldEmailCodeMap = new HashMap<>();
    private Map<String, String> newEmailCodeMap = new HashMap<>();

    // 头像上传接口
    @PostMapping("/upload/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file, 
                                                           @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("头像上传请求 - Token: " + token);
            
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            System.out.println("Token验证结果 - 用户账号: " + userAccount);
            
            if (userAccount == null) {
                System.out.println("Token验证失败");
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                System.out.println("用户不存在: " + userAccount);
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            System.out.println("文件信息 - 名称: " + file.getOriginalFilename() + ", 大小: " + file.getSize());

            // 验证文件类型
            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String suffix = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(suffix)) {
                response.put("code", 400);
                response.put("message", "只支持 JPG、PNG、GIF 格式的图片");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                response.put("code", 400);
                response.put("message", "文件大小不能超过 2MB");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建头像存储目录
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars";
            Files.createDirectories(Paths.get(uploadDir));
            System.out.println("上传目录: " + uploadDir);

            // 生成唯一文件名
            String filename = UUID.randomUUID() + suffix;
            File dest = new File(uploadDir, filename);
            file.transferTo(dest);
            System.out.println("文件保存成功: " + dest.getAbsolutePath());

            // 返回访问URL
            String avatarUrl = "/uploads/avatars/" + filename;
            System.out.println("头像URL: " + avatarUrl);

            response.put("code", 200);
            response.put("message", "头像上传成功");
            response.put("data", Map.of("url", avatarUrl));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("头像上传异常: " + e.getMessage());
            response.put("code", 500);
            response.put("message", "头像上传失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 更新用户信息接口
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> data,
                                                            @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 更新允许修改的字段
            if (data.containsKey("nickname")) {
                String nickname = (String) data.get("nickname");
                if (nickname != null && !nickname.trim().isEmpty()) {
                    user.setNickname(nickname.trim());
                }
            }

            if (data.containsKey("headUrl")) {
                String headUrl = (String) data.get("headUrl");
                if (headUrl != null && !headUrl.trim().isEmpty()) {
                    user.setHeadUrl(headUrl.trim());
                }
            }

            // 保存更新
            userRepository.save(user);

            // 返回更新后的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("account", user.getAccount());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("headUrl", user.getHeadUrl());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("createdAt", user.getCreatedAt());

            response.put("code", 200);
            response.put("message", "信息更新成功");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "信息更新失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 获取用户详细信息接口
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getProfileInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 返回用户详细信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("account", user.getAccount());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("headUrl", user.getHeadUrl());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("createdAt", user.getCreatedAt());

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取用户信息失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 修改密码接口
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> data,
                                                             @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 验证旧密码
            String oldPassword = data.get("oldPassword");
            String newPassword = data.get("newPassword");
            String confirmPassword = data.get("confirmPassword");

            if (oldPassword == null || newPassword == null || confirmPassword == null) {
                response.put("code", 400);
                response.put("message", "所有密码字段都不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("code", 400);
                response.put("message", "新密码和确认密码不一致");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword.length() < 6 || newPassword.length() > 20) {
                response.put("code", 400);
                response.put("message", "新密码长度必须在6-20个字符之间");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证旧密码是否正确
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                response.put("code", 400);
                response.put("message", "旧密码错误");
                return ResponseEntity.badRequest().body(response);
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            response.put("code", 200);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "密码修改失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 获取我的社团接口
    @GetMapping("/my-clubs")
    public ResponseEntity<Map<String, Object>> getMyClubs(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 获取用户加入的社团
            List<ClubMember> memberships = clubMemberRepository.findByUserIdAndJoinStatus(user.getId(), ClubMember.JoinStatus.已通过);
            List<Map<String, Object>> clubs = new ArrayList<>();

            for (ClubMember membership : memberships) {
                Optional<Club> clubOpt = clubRepository.findById(membership.getClubId());
                if (clubOpt.isPresent()) {
                    Club club = clubOpt.get();
                    Map<String, Object> clubInfo = new HashMap<>();
                    clubInfo.put("id", club.getId());
                    clubInfo.put("name", club.getName());
                    clubInfo.put("description", club.getDescription());
                    clubInfo.put("logoUrl", club.getLogoUrl());
                    clubInfo.put("status", club.getStatus());
                    clubInfo.put("role", membership.getRole());
                    clubInfo.put("joinTime", membership.getJoinedAt());
                    clubs.add(clubInfo);
                }
            }

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", clubs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取我的社团失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 获取我的活动接口
    @GetMapping("/my-activities")
    public ResponseEntity<Map<String, Object>> getMyActivities(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();
            System.out.println("获取用户活动 - 用户ID: " + user.getId());

            // 获取用户参与的所有活动（包括自己创建的和别人创建但自己参与的活动）
            List<Map<String, Object>> activities = new ArrayList<>();

            // 1. 获取用户创建的活动（从club_activity表）
            List<ClubActivity> createdActivities = clubActivityRepository.findByCreatorId(user.getId());
            System.out.println("用户创建的活动数量: " + createdActivities.size());
            
            for (ClubActivity activity : createdActivities) {
                Map<String, Object> activityInfo = buildActivityInfo(activity, true, null);
                activities.add(activityInfo);
            }
            
            // 2. 获取用户参与但不是创建者的活动（从activity_participant表，只获取状态为"已加入"的记录）
            // 直接使用repository方法获取已加入状态的活动，避免在内存中过滤
            List<ActivityParticipant> participatedActivities = activityParticipantRepository.findByUserIdAndStatus(
                user.getId(), ActivityParticipant.ParticipantStatus.已加入);
            System.out.println("用户已加入的活动记录数量: " + participatedActivities.size());
            
            for (ActivityParticipant participant : participatedActivities) {
                System.out.println("处理参与记录 - 活动ID: " + participant.getActivityId() + ", 状态: " + participant.getStatus());
                
                // 检查是否已经添加过（避免重复，用户创建的活动和参与的活动可能重复）
                boolean alreadyAdded = activities.stream()
                    .anyMatch(activity -> activity.get("id").equals(participant.getActivityId()));
                
                if (!alreadyAdded) {
                    // 通过活动ID从club_activity表获取活动详细信息
                    Optional<ClubActivity> activityOpt = clubActivityRepository.findById(participant.getActivityId());
                    if (activityOpt.isPresent()) {
                        ClubActivity activity = activityOpt.get();
                        Map<String, Object> activityInfo = buildActivityInfo(activity, false, participant);
                        activities.add(activityInfo);
                        System.out.println("添加参与的活动: " + activity.getTitle());
                    } else {
                        System.out.println("活动不存在，ID: " + participant.getActivityId());
                    }
                } else {
                    System.out.println("活动已存在，跳过: " + participant.getActivityId());
                }
            }
            
            System.out.println("最终返回的活动总数: " + activities.size());
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", activities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取我的活动失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 辅助方法：构建活动信息
    private Map<String, Object> buildActivityInfo(ClubActivity activity, boolean isCreator, ActivityParticipant participant) {
        Map<String, Object> activityInfo = new HashMap<>();
        activityInfo.put("id", activity.getId());
        activityInfo.put("title", activity.getTitle());
        activityInfo.put("description", activity.getDescription());
        activityInfo.put("startTime", activity.getStartTime());
        activityInfo.put("endTime", activity.getEndTime());
        activityInfo.put("location", activity.getLocation());
        activityInfo.put("applyStatus", activity.getApplyStatus());
        activityInfo.put("createdAt", activity.getCreatedAt());
        activityInfo.put("isCreator", isCreator);
        
        // 如果是参与者，添加参与相关信息
        if (!isCreator && participant != null) {
            activityInfo.put("joinTime", participant.getJoinTime());
            activityInfo.put("participantStatus", participant.getStatus());
        }
        
        // 统计参与人数（只统计状态为"已加入"的）
        long participantCount = activityParticipantRepository.countActiveParticipantsByActivityId(activity.getId());
        activityInfo.put("participantCount", participantCount);
        
        // 判断活动状态
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String activityStatus;
        if (activity.getEndTime().isBefore(now)) {
            activityStatus = "已结束";
        } else if (activity.getStartTime().isBefore(now)) {
            activityStatus = "进行中";
        } else {
            activityStatus = "未开始";
        }
        activityInfo.put("activityStatus", activityStatus);
        
        // 获取社团信息
        Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
        if (clubOpt.isPresent()) {
            activityInfo.put("clubName", clubOpt.get().getName());
        }
        
        return activityInfo;
    }

    // 获取最近活动接口
    @GetMapping("/recent-activities")
    public ResponseEntity<Map<String, Object>> getRecentActivities(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 获取用户参与或创建的所有活动（只包括未开始和进行中的活动，且状态为"通过"）
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // 1. 获取用户创建的活动
            List<ClubActivity> createdActivities = clubActivityRepository.findByCreatorId(user.getId());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            // 过滤出未开始和进行中的活动，且状态为"通过"（不包含已结束和待审核的活动）
            List<ClubActivity> recentCreatedActivities = createdActivities.stream()
                .filter(activity -> activity.getEndTime().isAfter(now) && activity.getApplyStatus() == ClubActivity.ApplyStatus.通过)
                .collect(java.util.stream.Collectors.toList());
            
            for (ClubActivity activity : recentCreatedActivities) {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("id", activity.getId());
                activityInfo.put("title", activity.getTitle());
                activityInfo.put("description", activity.getDescription());
                activityInfo.put("startTime", activity.getStartTime());
                activityInfo.put("endTime", activity.getEndTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("applyStatus", activity.getApplyStatus());
                activityInfo.put("createdAt", activity.getCreatedAt());
                activityInfo.put("isCreator", true); // 标记为创建者
                
                // 判断活动状态
                String activityStatus;
                if (activity.getStartTime().isAfter(now)) {
                    activityStatus = "未开始";
                } else {
                    activityStatus = "进行中";
                }
                activityInfo.put("activityStatus", activityStatus);
                
                // 获取社团信息
                Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
                if (clubOpt.isPresent()) {
                    activityInfo.put("clubName", clubOpt.get().getName());
                }
                
                activities.add(activityInfo);
            }
            
            // 2. 获取用户参与但不是创建者的活动（只获取状态为"已加入"的记录）
            List<ActivityParticipant> participatedActivities = activityParticipantRepository.findByUserIdAndStatus(
                user.getId(), ActivityParticipant.ParticipantStatus.已加入);
            
            for (ActivityParticipant participant : participatedActivities) {
                // 检查是否已经添加过（避免重复）
                boolean alreadyAdded = activities.stream()
                    .anyMatch(activity -> activity.get("id").equals(participant.getActivityId()));
                
                if (!alreadyAdded) {
                    Optional<ClubActivity> activityOpt = clubActivityRepository.findById(participant.getActivityId());
                    if (activityOpt.isPresent()) {
                        ClubActivity activity = activityOpt.get();
                        
                        // 只添加未开始和进行中的活动，且状态为"通过"
                        if (activity.getEndTime().isAfter(now) && activity.getApplyStatus() == ClubActivity.ApplyStatus.通过) {
                            Map<String, Object> activityInfo = new HashMap<>();
                            activityInfo.put("id", activity.getId());
                            activityInfo.put("title", activity.getTitle());
                            activityInfo.put("description", activity.getDescription());
                            activityInfo.put("startTime", activity.getStartTime());
                            activityInfo.put("endTime", activity.getEndTime());
                            activityInfo.put("location", activity.getLocation());
                            activityInfo.put("applyStatus", activity.getApplyStatus());
                            activityInfo.put("createdAt", activity.getCreatedAt());
                            activityInfo.put("isCreator", false); // 标记为参与者
                            activityInfo.put("joinTime", participant.getJoinTime());
                            activityInfo.put("participantStatus", participant.getStatus());
                            
                            // 判断活动状态
                            String activityStatus;
                            if (activity.getStartTime().isAfter(now)) {
                                activityStatus = "未开始";
                            } else {
                                activityStatus = "进行中";
                            }
                            activityInfo.put("activityStatus", activityStatus);
                    
                            // 获取社团信息
                            Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
                            if (clubOpt.isPresent()) {
                                activityInfo.put("clubName", clubOpt.get().getName());
                            }
                    
                            activities.add(activityInfo);
                        }
                    }
                }
            }
            
            // 按开始时间排序，取最近12个
            activities.sort((a, b) -> ((java.time.LocalDateTime) a.get("startTime")).compareTo((java.time.LocalDateTime) b.get("startTime")));
            activities = activities.subList(0, Math.min(12, activities.size()));

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", activities);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取最近活动失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 获取我的帖子接口
    @GetMapping("/my-posts")
    public ResponseEntity<Map<String, Object>> getMyPosts(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("ProfileController - getMyPosts called");
            System.out.println("ProfileController - Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
            
            // 验证token并获取用户信息
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            System.out.println("ProfileController - User account from token: " + userAccount);
            
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 获取用户发布的帖子
            List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(Long.valueOf(user.getId()));
            List<Map<String, Object>> postsList = new ArrayList<>();

            for (Post post : posts) {
                Map<String, Object> postInfo = new HashMap<>();
                postInfo.put("id", post.getId());
                postInfo.put("title", post.getTitle());
                postInfo.put("content", post.getContent());
                postInfo.put("likeCount", post.getLikeCount());
                postInfo.put("commentCount", post.getCommentCount());
                postInfo.put("status", post.getStatus());
                postInfo.put("createdAt", post.getCreatedAt());
                postInfo.put("imageUrlList", post.getImageUrlList());
                
                // 获取社团信息
                if (post.getClubId() != null) {
                    Optional<Club> clubOpt = clubRepository.findById(post.getClubId().intValue());
                    if (clubOpt.isPresent()) {
                        postInfo.put("clubName", clubOpt.get().getName());
                        postInfo.put("clubId", clubOpt.get().getId());
                    } else {
                        postInfo.put("clubName", "无");
                        postInfo.put("clubId", null);
                    }
                } else {
                    postInfo.put("clubName", "无");
                    postInfo.put("clubId", null);
                }
                
                postsList.add(postInfo);
            }

            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", postsList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "获取我的帖子失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 同时发送新旧邮箱验证码
    @PostMapping("/send-both-email-codes")
    public ResponseEntity<Map<String, Object>> sendBothEmailCodes(@RequestBody Map<String, String> data, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userAccount = tokenManager.validateTokenAndGetUsername(token.replace("Bearer ", ""));
            if (userAccount == null) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                return ResponseEntity.status(401).body(response);
            }
            Optional<User> userOpt = userRepository.findByAccount(userAccount);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }
            User user = userOpt.get();
            String newEmail = data.get("newEmail");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "新邮箱不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            // 生成验证码
            String oldCode = String.valueOf((int)((Math.random()*9+1)*100000));
            String newCode = String.valueOf((int)((Math.random()*9+1)*100000));
            oldEmailCodeMap.put(user.getEmail(), oldCode);
            newEmailCodeMap.put(newEmail, newCode);
            // 发送邮件
            emailService.sendSimpleMail(user.getEmail(), "旧邮箱验证码", "您的旧邮箱验证码是: " + oldCode + "，3分钟内有效。");
            emailService.sendSimpleMail(newEmail, "新邮箱验证码", "您的新邮箱验证码是: " + newCode + "，3分钟内有效。");
            response.put("code", 200);
            response.put("message", "验证码已分别发送到新旧邮箱");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "发送验证码失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
