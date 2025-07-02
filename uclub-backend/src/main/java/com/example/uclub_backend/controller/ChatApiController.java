package com.example.uclub_backend.controller;

import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.service.OnlineUserService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private UserRepository userRepository;

@GetMapping("/online/{roomId}")
public List<Map<String, Object>> getOnlineUsers(@PathVariable String roomId) {
    Set<String> accounts = onlineUserService.getOnlineUsers(roomId);
    System.out.println("📡 获取房间 " + roomId + " 在线账号: " + accounts);

    List<Map<String, Object>> userList = new ArrayList<>();

    for (String account : accounts) {
        userRepository.findByAccount(account).ifPresent(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getHeadUrl());
            map.put("role", user.getRole().name());
            userList.add(map);
        });
    }

    System.out.println("✅ 返回在线用户列表: " + userList);
    return userList;
}

}
