package com.example.uclub_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {
    @Autowired
    private RestTemplate restTemplate;
    private final String apiKey = "sk-9eeac3034e9f42e8ba8f1789fbc0025c";
    private final String apiUrl = "https://api.deepseek.com/v1/chat/completions";

    public String askQuestion(String question) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            body.put("messages", List.of(Map.of("role", "user", "content", question)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            return (String) ((Map) choices.get(0).get("message")).get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "AI服务暂时不可用，请稍后重试。";
        }
    }
} 