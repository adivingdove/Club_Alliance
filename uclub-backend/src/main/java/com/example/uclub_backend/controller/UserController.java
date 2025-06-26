package com.example.uclub_backend.controller;

import com.example.uclub_backend.model.User;
import com.example.uclub_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getUsersByQuery(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        // 调用 service 层来处理查询逻辑
        return userService.getUsersByQuery(email, nickname, role, status);
    }
}
