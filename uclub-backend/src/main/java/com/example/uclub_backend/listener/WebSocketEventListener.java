package com.example.uclub_backend.listener;

import com.example.uclub_backend.service.OnlineUserService;
import com.example.uclub_backend.service.WebSocketSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private WebSocketSessionService sessionService;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        if (principal != null && destination != null && destination.startsWith("/topic/")) {
            String roomId = destination.substring("/topic/".length());
            String username = principal.getName();
            onlineUserService.addUserToRoom(roomId, username);
            sessionService.registerSession(sessionId, username, roomId);
            System.out.println(" 加入房间: " + username + " -> " + roomId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        String username = sessionService.getUsername(sessionId);
        String roomId = sessionService.getRoomId(sessionId);

        if (username != null && roomId != null) {
            onlineUserService.removeUserFromRoom(roomId, username);
            System.out.println(" 用户断开连接: " + username + " 离开房间: " + roomId);
        }

        sessionService.removeSession(sessionId);
    }
}
