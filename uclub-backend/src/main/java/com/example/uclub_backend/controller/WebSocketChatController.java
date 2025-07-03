package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.ChatMessage;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.service.OnlineUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
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

    @MessageMapping("/chat.send.{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            Principal principal,  //  从这里获取用户名
                            ChatMessage message) {

        message.setTime(LocalDateTime.now().toString());

        if (principal == null || principal.getName() == null) {
            System.out.println(" Principal 为空，未登录用户");
            return;
        }

        String account = principal.getName();  // token 里的用户名
        onlineUserService.addUserToRoom(roomId, account);
        Optional<User> optionalUser = userRepository.findByAccount(account);
        if (optionalUser.isEmpty()) {
            System.out.println(" 用户不存在: " + account);
            return;
        }

        User user = optionalUser.get();
        message.setSender(user.getNickname());
        message.setAvatar(user.getHeadUrl() != null ? user.getHeadUrl() : "");

        String role = "成员"; // 默认身份

        if (!roomId.equals("public") && roomId.startsWith("club-")) {
            try {
                Integer clubId = Integer.parseInt(roomId.substring(5));
                Optional<ClubMember> clubMemberOpt = clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId);

                if (clubMemberOpt.isPresent()) {
                    ClubMember member = clubMemberOpt.get();
                    if ("已通过".equals(member.getJoinStatus().name())) {
                        role = member.getRole().name();
                    } else {
                        System.out.println(" 用户未通过审核: " + member.getJoinStatus().name());
                    }
                } else {
                    System.out.println(" 用户未加入该社团");
                }
            } catch (NumberFormatException e) {
                System.out.println(" roomId 格式错误：" + roomId);
            }
        }

        message.setRole(role);

        System.out.println(" 发送消息：" + message.getSender() + " (" + role + ") : " + message.getContent());

        messagingTemplate.convertAndSend("/topic/" + roomId, message);
    }
}
