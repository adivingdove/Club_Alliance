package com.example.uclub_backend.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionService {
    private final ConcurrentHashMap<String, String> sessionToUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionToRoom = new ConcurrentHashMap<>();

    // 注册 session 与对应用户、房间的关系
    public void registerSession(String sessionId, String username, String roomId) {
        sessionToUser.put(sessionId, username);
        sessionToRoom.put(sessionId, roomId);
    }

    public String getUsername(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    public String getRoomId(String sessionId) {
        return sessionToRoom.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionToUser.remove(sessionId);
        sessionToRoom.remove(sessionId);
    }
}
