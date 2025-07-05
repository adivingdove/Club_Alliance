package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;
import com.example.uclub_backend.forum.entity.Like;
import com.example.uclub_backend.forum.service.CommentService;
import com.example.uclub_backend.forum.service.LikeService;
import com.example.uclub_backend.forum.service.PostService;
import com.example.uclub_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final LikeService likeService;
    private final UserService userService;

    public CommentController(CommentService commentService, PostService postService, LikeService likeService, UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.likeService = likeService;
        this.userService = userService;
    }

    // 获取帖子下评论（树结构），仅限 active 状态
    @GetMapping
    public List<Map<String, Object>> getComments(@PathVariable Long postId, @RequestParam(required = false) Long userId) {
        List<Comment> flatList = commentService.getCommentsByPostId(postId);
        Map<Long, Map<String, Object>> mapById = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Comment comment : flatList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("createdAt", comment.getCreatedAt());
            map.put("likeCount", comment.getLikeCount());
            map.put("liked", userId != null && likeService.hasLiked(userId, Like.TargetType.comment, comment.getId()));
            map.put("replies", new ArrayList<Map<String, Object>>());

            User user = userService.getUserById(comment.getUserId().intValue());
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatarUrl", user.getHeadUrl());
                map.put("user", userMap);
            }

            mapById.put(comment.getId(), map);
        }

        for (Comment comment : flatList) {
            Map<String, Object> current = mapById.get(comment.getId());
            if (comment.getParentCommentId() == null) {
                roots.add(current);
            } else {
                Map<String, Object> parent = mapById.get(comment.getParentCommentId());
                if (parent != null) {
                    List<Map<String, Object>> replies = (List<Map<String, Object>>) parent.get("replies");
                    replies.add(current);
                }
            }
        }

        return roots;
    }

    // 新增评论
    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId);
        commentService.addComment(comment);
        
        return ResponseEntity.ok().build();
    }

    // 删除评论（并考虑是否需要更新评论数）
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @RequestParam Long userId) {
        Comment comment = commentService.getCommentById(commentId);
        boolean wasActive = comment.getStatus() == CommentStatus.active;

        commentService.deleteComment(postId, commentId, userId);
        if (wasActive) {
            postService.decrementCommentCount(postId);
        }

        return ResponseEntity.ok().build();
    }

    // 修改评论状态
    @PutMapping("/{commentId}/status")
    public void updateStatus(@PathVariable Long commentId, @RequestParam CommentStatus status) {
        commentService.updateStatus(commentId, status);
    }

    // 点赞或取消点赞评论
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

    // 获取评论详情（只返回 active 状态）
    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentDetail(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        if (comment == null || comment.getStatus() != CommentStatus.active) {
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

    // 后台更新评论状态
    @PutMapping("/admin/{commentId}/status")
    public ResponseEntity<?> updateCommentStatus(@PathVariable Long postId,
                                                 @PathVariable Long commentId,
                                                 @RequestParam String status) {
        commentService.updateCommentStatus(postId, commentId, status);
        return ResponseEntity.ok().build();
    }

    //  热门评论接口（新增，只查 active）
    @GetMapping("/hot")
    public ResponseEntity<List<Map<String, Object>>> getHotComments(@PathVariable Long postId,
                                                                     @RequestParam(defaultValue = "5") int limit,
                                                                     @RequestParam(required = false) Long userId) {
        List<Comment> hotComments = commentService.getHotComments(limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment comment : hotComments) {
            if (!Objects.equals(comment.getPostId(), postId)) continue; // 只返回当前帖子的热评

            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("createdAt", comment.getCreatedAt());
            map.put("likeCount", comment.getLikeCount());
            map.put("liked", userId != null && likeService.hasLiked(userId, Like.TargetType.comment, comment.getId()));

            User user = userService.getUserById(comment.getUserId().intValue());
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatarUrl", user.getHeadUrl());
                map.put("user", userMap);
            }

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}
