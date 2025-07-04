package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.ChatMessage;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.ChatMessageRepository;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.service.OnlineUserService;
import com.example.uclub_backend.repository.ChatMessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebSocketChatController {
    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

 
@MessageMapping("/chat.send.{roomId}")
public void sendMessage(@DestinationVariable String roomId,
                        Principal principal,
                        Map<String, Object> payload) {

    System.out.println("✅ WebSocket 方法已被触发");

    if (principal == null || principal.getName() == null) {
        System.out.println("❌ Principal 为空");
        return;
    }

    Optional<User> optionalUser = userRepository.findByAccount(principal.getName());
    if (optionalUser.isEmpty()) {
        System.out.println("❌ 用户不存在: " + principal.getName());
        return;
    }

    User user = optionalUser.get();

    String content = (String) payload.get("content");
    if (content == null || content.isBlank()) {
        System.out.println("❌ 消息内容为空");
        return;
    }

    ChatMessage message = new ChatMessage();
    message.setRoom(roomId);
    message.setTime(LocalDateTime.now());
    message.setContent(content);
    message.setSender(user.getNickname());
    message.setAvatar(user.getHeadUrl());
    message.setSenderId(user.getId());
    
    String role = "成员";
    if (roomId.startsWith("club-")) {
        try {
            Integer clubId = Integer.parseInt(roomId.substring(5));
            Optional<ClubMember> cm = clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId);
            if (cm.isPresent() && "已通过".equals(cm.get().getJoinStatus().name())) {
                role = cm.get().getRole().name();
            }
        } catch (Exception e) {
            System.out.println("⚠️ clubId 格式错误");
        }
    }
    message.setRole(role);

    chatMessageRepository.save(message);
    messagingTemplate.convertAndSend("/topic/" + roomId, message);
    System.out.println("✅ 消息已发送并存储：" + content);
}


}
