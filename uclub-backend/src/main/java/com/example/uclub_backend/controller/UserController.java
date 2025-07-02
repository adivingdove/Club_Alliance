package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.service.EmailService;
import com.example.uclub_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService mailService;

    @Autowired
    private UserService userService;

    // 验证码缓存（邮箱->验证码和时间戳）
    private final Map<String, CodeInfo> emailCodeMap = new ConcurrentHashMap<>();
    private static class CodeInfo {
        String code;
        long timestamp;
        CodeInfo(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }

    // 用户注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        String account = data.get("account");
        String email = data.get("email");
        String password = data.get("password");
        String nickname = data.get("nickname");
        String headUrl = data.get("headUrl");
        String emailCode = data.get("emailCode");
        if (account == null || email == null || password == null || emailCode == null) {
            response.put("code", 400);
            response.put("message", "账号、邮箱、密码和验证码不能为空");
            return response;
        }
        // 校验验证码和有效期
        CodeInfo codeInfo = emailCodeMap.get(email);
        if (codeInfo == null || !codeInfo.code.equals(emailCode) || System.currentTimeMillis() - codeInfo.timestamp > 180000) {
            response.put("code", 400);
            response.put("message", "验证码错误或已过期");
            return response;
        }
        if (userRepository.existsByAccount(account)) {
            response.put("code", 400);
            response.put("message", "账号已存在");
            return response;
        }
        if (userRepository.existsByEmail(email)) {
            response.put("code", 400);
            response.put("message", "邮箱已存在");
            return response;
        }
        User user = new User();
        user.setAccount(account);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        // 设置默认头像，如果headUrl为空或null
        if (headUrl == null || headUrl.trim().isEmpty()) {
            user.setHeadUrl("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        } else {
            user.setHeadUrl(headUrl);
        }
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        // 注册成功后移除验证码
        emailCodeMap.remove(email);
        response.put("code", 200);
        response.put("message", "注册成功");
        return response;
    }

    // 用户登录（支持账号或邮箱）
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        String accountOrEmail = data.get("account");
        String password = data.get("password");
        if (accountOrEmail == null || password == null) {
            response.put("code", 400);
            response.put("message", "账号/邮箱和密码不能为空");
            return response;
        }
        Optional<User> userOpt = userRepository.findByAccount(accountOrEmail);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(accountOrEmail);
        }
        if (!userOpt.isPresent()) {
            response.put("code", 400);
            response.put("message", "用户不存在");
            return response;
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("code", 400);
            response.put("message", "密码错误");
            return response;
        }
        // 生成JWT token
        String token = tokenManager.generateToken(user.getAccount());
        tokenManager.storeToken(token, user.getAccount());

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
        response.put("message", "登录成功");
        response.put("token", token);
        response.put("data", userInfo);
        return response;
    }

    // 检查账号/邮箱是否已存在
    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam(required = false) String account, @RequestParam(required = false) String email) {
        Map<String, Object> response = new HashMap<>();
        if (account != null && userRepository.existsByAccount(account)) {
            response.put("exists", true);
            response.put("type", "account");
            return response;
        }
        if (email != null && userRepository.existsByEmail(email)) {
            response.put("exists", true);
            response.put("type", "email");
            return response;
        }
        response.put("exists", false);
        return response;
    }

    // 获取用户信息（通过id）
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam Integer id) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            response.put("code", 404);
            response.put("message", "用户不存在");
            return response;
        }
        User user = userOpt.get();
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
        response.put("data", userInfo);
        return response;
    }

    @PostMapping("/sendRegisterCode")
    public Map<String, Object> sendRegisterCode(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        String email = data.get("email");
        if (email == null || email.trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "邮箱不能为空");
            return response;
        }
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        emailCodeMap.put(email, new CodeInfo(code, System.currentTimeMillis()));
        mailService.sendSimpleMail(email, "注册验证码", "您的验证码是：" + code + "，3分钟内有效。");
        response.put("code", 200);
        response.put("message", "验证码已发送到您的邮箱");
        return response;
    }

    @PostMapping("/sendResetCode")
    public Map<String, Object> sendResetCode(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        String account = data.get("username");
        String email = data.get("email");
        if (account == null || email == null) {
            response.put("code", 400);
            response.put("message", "用户名和邮箱不能为空");
            return response;
        }
        Optional<User> userOpt = userRepository.findByAccount(account);
        if (!userOpt.isPresent() || !email.equals(userOpt.get().getEmail())) {
            response.put("code", 400);
            response.put("message", "用户名和邮箱不匹配");
            return response;
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        emailCodeMap.put(email, new CodeInfo(code, System.currentTimeMillis()));
        mailService.sendSimpleMail(email, "重置密码验证码", "您的验证码是：" + code + "，3分钟内有效。");
        response.put("code", 200);
        response.put("message", "验证码已发送到您的邮箱");
        return response;
    }

    @PostMapping("/resetPassword")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        String account = data.get("username");
        String email = data.get("email");
        String verificationCode = data.get("verificationCode");
        String newPassword = data.get("newPassword");
        if (account == null || email == null || verificationCode == null || newPassword == null) {
            response.put("code", 400);
            response.put("message", "所有字段都不能为空");
            return response;
        }
        Optional<User> userOpt = userRepository.findByAccount(account);
        if (!userOpt.isPresent() || !email.equals(userOpt.get().getEmail())) {
            response.put("code", 400);
            response.put("message", "用户名和邮箱不匹配");
            return response;
        }
        // 校验验证码和有效期
        CodeInfo codeInfo = emailCodeMap.get(email);
        if (codeInfo == null || !codeInfo.code.equals(verificationCode) || System.currentTimeMillis() - codeInfo.timestamp > 180000) {
            response.put("code", 400);
            response.put("message", "验证码错误或已过期");
            return response;
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // 重置成功后移除验证码
        emailCodeMap.remove(email);
        response.put("code", 200);
        response.put("message", "密码重置成功");
        return response;
    }

    // 列出用户
    @GetMapping
    public Map<String, Object> queryUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) User.UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> userPage = userService.getUsersByQuery(email, nickname, role, status, page, size);

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("data", userPage.getContent());
        resp.put("total", userPage.getTotalElements());
        resp.put("page", userPage.getNumber());
        resp.put("size", userPage.getSize());
        return resp;
    }

}
