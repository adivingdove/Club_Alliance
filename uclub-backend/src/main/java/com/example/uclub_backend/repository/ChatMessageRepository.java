package com.example.uclub_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.example.uclub_backend.entity.ChatMessage;
import com.example.uclub_backend.repository.ChatMessageRepository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomOrderByTimeDesc(String room, Pageable pageable);
}
