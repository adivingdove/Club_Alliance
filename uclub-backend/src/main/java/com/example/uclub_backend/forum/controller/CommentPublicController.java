package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.service.CommentService;
import com.example.uclub_backend.service.UserService;
import com.example.uclub_backend.forum.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentPublicController {

    private final CommentService commentService;
    private final UserService userService;
    private final PostService postService;

    public CommentPublicController(CommentService commentService, UserService userService, PostService postService) {
        this.commentService = commentService;
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/hot")
    public List<Map<String, Object>> getHotComments(@RequestParam(defaultValue = "5") int limit) {
        List<Comment> comments = commentService.getHotComments(limit);
        return comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("likeCount", comment.getLikeCount());
            map.put("createdAt", comment.getCreatedAt());
            map.put("postId", comment.getPostId());
            map.put("user", userService.getUserById(comment.getUserId().intValue()));
            map.put("post", postService.getPostTitle(comment.getPostId()));
            return map;
        }).toList();
    }
}
