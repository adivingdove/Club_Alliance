package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;
import com.example.uclub_backend.forum.service.CommentService;
import com.example.uclub_backend.forum.service.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;
    private final PostService postService; //  加上这句

    //  构造方法注入两个服务
    public CommentController(CommentService commentService, PostService postService) {
        this.commentService = commentService;
        this.postService = postService;
    }

    // 获取评论列表
    @GetMapping
    public List<Comment> getComments(@PathVariable Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    // 添加评论
    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Comment comment) {
        comment.setPostId(postId);
        commentService.save(comment);
        postService.incrementCommentCount(postId); // 更新帖子评论数
        return ResponseEntity.ok().build();
    }

    // 点赞评论
    @PostMapping("/{commentId}/like")
    public void likeComment(@PathVariable Long commentId) {
        commentService.likeComment(commentId);
    }

    // 修改评论状态
    @PutMapping("/{commentId}/status")
    public void updateStatus(@PathVariable Long commentId,
                             @RequestParam CommentStatus status) {
        commentService.updateStatus(commentId, status);
    }

    // 删除评论（仅限评论作者）
@DeleteMapping("/{commentId}")
public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                       @PathVariable Long commentId,
                                       @RequestParam Long userId) {
    commentService.deleteComment(postId, commentId, userId);
    postService.decrementCommentCount(postId);

    return ResponseEntity.ok().build();
}

}
