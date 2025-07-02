package com.example.uclub_backend.service;


import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    // 可根据 roomId 区分不同聊天室
    private final ConcurrentHashMap<String, Set<String>> roomUserMap = new ConcurrentHashMap<>();

    // 用户加入房间
    public void addUserToRoom(String roomId, String username) {
        roomUserMap.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(username);
    }

    // 用户离开房间
    public void removeUserFromRoom(String roomId, String username) {
        Set<String> users = roomUserMap.get(roomId);
        if (users != null) {
            users.remove(username);
            if (users.isEmpty()) {
                roomUserMap.remove(roomId);
            }
        }
    }

    // 获取某个房间的所有在线用户
    public Set<String> getOnlineUsers(String roomId) {
        return roomUserMap.getOrDefault(roomId, Set.of());
    }
}
