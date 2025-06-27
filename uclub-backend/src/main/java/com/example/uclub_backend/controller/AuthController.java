package com.example.uclub_backend.controller;

import com.example.uclub_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 校验当前登录用户密码
    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        boolean result = userService.verifyUserPasswordAndRoleAndStatus(email, password, "系统管理员", "正常");

        return ResponseEntity.ok(result);
    }


}

