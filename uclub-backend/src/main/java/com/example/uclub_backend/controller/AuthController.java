package com.example.uclub_backend.controller;

import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenManager tokenManager;

    // 校验当前登录用户密码
    @PostMapping("/verify-admin")
    public Map<String, Object> verifyAdmin(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String account = payload.get("account");
        String password = payload.get("password");

        if (account == null || password == null) {
            response.put("code", 400);
            response.put("message", "账号和密码不能为空");
            return response;
        }

        Optional<User> userOpt = userRepository.findByAccount(account);
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

        // 验证：是否为系统管理员
        if (user.getRole()!= User.UserRole.系统管理员) {
            response.put("code", 403);
            response.put("message", "无权限：该用户不是系统管理员");
            return response;
        }

        // 验证成功，生成 token
        String token = tokenManager.generateToken(user.getAccount());
        tokenManager.storeToken(token, user.getAccount());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("account", user.getAccount());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        userInfo.put("createdAt", user.getCreatedAt());

        response.put("code", 200);
        response.put("message", "系统管理员验证成功");
        response.put("token", token);
        response.put("data", userInfo);

        return response;
    }

}

