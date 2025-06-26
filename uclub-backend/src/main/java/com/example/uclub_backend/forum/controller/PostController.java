package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.service.PostService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

import java.util.List;

@RestController
@RequestMapping("/api/posts")

public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

@PostMapping
public Map<String, Object> create(@RequestBody Post post) {
    Map<String, Object> res = new HashMap<>();
    try {
        Post saved = postService.save(post);
        res.put("code", 200);
        res.put("message", "发布成功");
        Map<String, Object> data = new HashMap<>();
        data.put("post_id", saved.getId());
        res.put("data", data);
    } catch (Exception e) {
        res.put("code", 500);
        res.put("message", "服务器错误: " + e.getMessage());
        e.printStackTrace(); // 控制台打印完整错误信息
    }
    return res;
}



    @GetMapping
    public List<Post> getAll() {
        return postService.findAll();
    }
}
