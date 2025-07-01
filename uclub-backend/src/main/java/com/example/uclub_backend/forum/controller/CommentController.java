package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;
import com.example.uclub_backend.forum.entity.Like;
import com.example.uclub_backend.forum.service.CommentService;
import com.example.uclub_backend.forum.service.LikeService;
import com.example.uclub_backend.forum.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.service.UserService; 

import java.util.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin
public class CommentController {
    
    private final CommentService commentService;
    private final PostService postService;
    private final LikeService likeService; // 添加 LikeService 注入
    private final UserService userService;

   public CommentController(CommentService commentService, PostService postService, LikeService likeService, UserService userService) {
    this.commentService = commentService;
    this.postService = postService;
    this.likeService = likeService;
    this.userService = userService;
    }

    @GetMapping
public List<Map<String, Object>> getComments(@PathVariable Long postId, @RequestParam(required = false) Long userId) {
    List<Comment> comments = commentService.getCommentsByPostId(postId);
    List<Map<String, Object>> result = new ArrayList<>();

    for (Comment comment : comments) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("content", comment.getContent());
        map.put("createdAt", comment.getCreatedAt());
        map.put("likeCount", comment.getLikeCount());
        map.put("liked", userId != null && likeService.hasLiked(userId, Like.TargetType.comment, comment.getId()));

        // 查询用户信息
        User user = userService.getUserById(comment.getUserId().intValue());
        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatarUrl", user.getHeadUrl()); // 或 getAvatarUrl()
            map.put("user", userMap);
        }

        result.add(map);
    }

    return result;
}


    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId);
        commentService.save(comment);
        postService.incrementCommentCount(postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @RequestParam Long userId) {
        commentService.deleteComment(postId, commentId, userId);
        postService.decrementCommentCount(postId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{commentId}/status")
    public void updateStatus(@PathVariable Long commentId, @RequestParam CommentStatus status) {
        commentService.updateStatus(commentId, status);
    }

    // 点赞/取消点赞评论
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(@PathVariable Long commentId,
                                                                  @RequestParam Long userId) {
        boolean liked = likeService.toggleLike(userId, Like.TargetType.comment, commentId);
      int likeCount = commentService.getCommentById(commentId).getLikeCount();  
        Map<String, Object> res = new HashMap<>();
        res.put("liked", liked);
        res.put("likeCount", likeCount);
        res.put("message", liked ? "点赞成功" : "取消点赞");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentDetail(@PathVariable Long id) {
        Comment comment = commentService.findById(id);
        if (comment == null) {
            return ResponseEntity.status(404).body(Map.of("message", "评论不存在"));
        }

        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "postId", comment.getPostId(),
                "userId", comment.getUserId(),
                "createdAt", comment.getCreatedAt()
        ));
    }
}
