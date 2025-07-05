package com.example.uclub_backend.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.example.uclub_backend.entity.ChatMessage;
import com.example.uclub_backend.repository.ChatMessageRepository;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/history/{room}")
    public List<ChatMessage> getHistory(@PathVariable String room,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("time").descending());
        return chatMessageRepository.findByRoomOrderByTimeDesc(room, pageable).getContent();
    }
}
