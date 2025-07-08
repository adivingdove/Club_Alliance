package com.example.uclub_backend.admin.controller;

import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.service.PostService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class PostAdminController {

    private final PostService postService;

    
    public PostAdminController(PostService postService) {
        this.postService = postService;
    }

    // 后台分页查询帖子列表
    @GetMapping("/posts")
    public Page<Post> getPostsForAdmin(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getPostPage(filters, page, size);
    }

    // 获取单个帖子
    @GetMapping("/posts/{id}")
    public Post getPostDetail(@PathVariable Long id) {
        return postService.getPostById(id);
    }

}
