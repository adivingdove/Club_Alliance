package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.repository.PostRepository;
import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.service.PostService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
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
            Map<String, Object> data = new HashMap<>();
            data.put("code", 200);
            data.put("message", "发布成功");
            data.put("post_id", saved.getId());
            res.put("data", data);
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<>();
            data.put("code", 500);
            data.put("message", "服务器错误: " + e.getMessage());
            res.put("data", data);
        }
        return res;
    }

@GetMapping
public Map<String, Object> getPosts(
        @RequestParam(defaultValue = "") String title,
        @RequestParam(defaultValue = "") String clubName,
        @RequestParam(defaultValue = "") String timeRange,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize) {

    // 目前只用 title 查询，clubName 和 timeRange 可保留拓展用
  Map<String, String> filters = new HashMap<>();
filters.put("title", title);

if (!clubName.isBlank()) {
    filters.put("clubName", clubName);
}

if (!timeRange.isBlank()) {
    filters.put("timeRange", timeRange);
}

     var postPage = postService.getPostPage(filters, page, pageSize);
    Map<String, Object> res = new HashMap<>();
    res.put("posts", postPage.getContent());
    res.put("total", postPage.getTotalElements());
    return res;
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        try {
            Post post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        postService.incrementLikeCount(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            postService.deletePostById(id, userId);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "服务器错误：" + e.getMessage()));
        }
    }
}
