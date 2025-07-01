package com.example.uclub_backend.controller;

import com.example.uclub_backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public String ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        return aiService.askQuestion(question);
    }
} 