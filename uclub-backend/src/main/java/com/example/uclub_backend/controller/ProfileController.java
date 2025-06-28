package com.example.uclub_backend.controller;

import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.ClubActivity;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.repository.ClubActivityRepository;
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
    private TokenManager tokenManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 头像上传接口
    @PostMapping("/upload/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file, 
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

            // 生成唯一文件名
            String filename = UUID.randomUUID() + suffix;
            File dest = new File(uploadDir, filename);
            file.transferTo(dest);

            // 返回访问URL
            String avatarUrl = "/uploads/avatars/" + filename;

            response.put("code", 200);
            response.put("message", "头像上传成功");
            response.put("data", Map.of("url", avatarUrl));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
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

            // 获取用户创建的活动
            List<ClubActivity> createdActivities = clubActivityRepository.findByCreatorId(user.getId());
            List<Map<String, Object>> activities = new ArrayList<>();

            for (ClubActivity activity : createdActivities) {
                Map<String, Object> activityInfo = new HashMap<>();
                activityInfo.put("id", activity.getId());
                activityInfo.put("title", activity.getTitle());
                activityInfo.put("description", activity.getDescription());
                activityInfo.put("startTime", activity.getStartTime());
                activityInfo.put("endTime", activity.getEndTime());
                activityInfo.put("location", activity.getLocation());
                activityInfo.put("applyStatus", activity.getApplyStatus());
                activityInfo.put("createdAt", activity.getCreatedAt());
                
                // 获取社团信息
                Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
                if (clubOpt.isPresent()) {
                    activityInfo.put("clubName", clubOpt.get().getName());
                }
                
                activities.add(activityInfo);
            }

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

            // 获取用户加入的社团
            List<ClubMember> memberships = clubMemberRepository.findByUserIdAndJoinStatus(user.getId(), ClubMember.JoinStatus.已通过);
            List<Integer> clubIds = new ArrayList<>();
            for (ClubMember membership : memberships) {
                clubIds.add(membership.getClubId());
            }

            // 获取这些社团的最近活动
            List<Map<String, Object>> activities = new ArrayList<>();
            if (!clubIds.isEmpty()) {
                List<ClubActivity> recentActivities = clubActivityRepository.findByClubIdIn(clubIds);
                
                // 按创建时间排序，取最近10个
                recentActivities.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                recentActivities = recentActivities.subList(0, Math.min(10, recentActivities.size()));

                for (ClubActivity activity : recentActivities) {
                    Map<String, Object> activityInfo = new HashMap<>();
                    activityInfo.put("id", activity.getId());
                    activityInfo.put("title", activity.getTitle());
                    activityInfo.put("description", activity.getDescription());
                    activityInfo.put("startTime", activity.getStartTime());
                    activityInfo.put("endTime", activity.getEndTime());
                    activityInfo.put("location", activity.getLocation());
                    activityInfo.put("applyStatus", activity.getApplyStatus());
                    activityInfo.put("createdAt", activity.getCreatedAt());
                    
                    // 获取社团信息
                    Optional<Club> clubOpt = clubRepository.findById(activity.getClubId());
                    if (clubOpt.isPresent()) {
                        activityInfo.put("clubName", clubOpt.get().getName());
                    }
                    
                    activities.add(activityInfo);
                }
            }

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
}
