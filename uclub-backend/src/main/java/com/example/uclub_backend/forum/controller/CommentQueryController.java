package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.service.CommentService;
import com.example.uclub_backend.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


public class CommentQueryController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;
    // 新增接口：通过评论ID获取评论及所属帖子信息
    @GetMapping("/api/comments/{commentId}/with-post")
    public ResponseEntity<?> getCommentWithPost(@PathVariable Long commentId) {
        Comment comment = commentService.findById(commentId);
        if (comment == null) {
            return ResponseEntity.status(404).body(Map.of("message", "评论不存在"));
        }

        var post = postService.getPostById(comment.getPostId());
        if (post == null) {
            return ResponseEntity.status(404).body(Map.of("message", "所属帖子不存在"));
        }

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", comment.getId());
        commentMap.put("content", comment.getContent());
        commentMap.put("userId", comment.getUserId());
        commentMap.put("createdAt", comment.getCreatedAt());

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", post.getId());
        postMap.put("title", post.getTitle());
        postMap.put("content", post.getContent());
        postMap.put("userId", post.getUserId());
        postMap.put("clubId", post.getClubId());
        postMap.put("createdAt", post.getCreatedAt());

        return ResponseEntity.ok(Map.of(
                "comment", commentMap,
                "post", postMap
        ));
    }
    
}
