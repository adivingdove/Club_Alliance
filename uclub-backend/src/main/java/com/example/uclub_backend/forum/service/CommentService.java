package com.example.uclub_backend.forum.service;

import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;

import com.example.uclub_backend.forum.repository.CommentRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

   private final CommentRepository commentRepository;

   @Autowired
   private UserRepository userRepository;
   

   private final PostService postService;

   public CommentService(@Lazy PostService postService, CommentRepository commentRepository) {
    this.postService = postService;
    this.commentRepository = commentRepository;
}

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);

    }

    public void addComment(Comment comment) {
    if (comment.getId() == null) {
        comment.setStatus(CommentStatus.active);
        comment.setLikeCount(0);
    }
    commentRepository.save(comment); // 插入或更新
    }

 

   public void likeComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("评论不存在"));
    comment.setLikeCount(comment.getLikeCount() + 1);
    commentRepository.save(comment);
}


public void updateStatus(Long commentId, CommentStatus status) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("评论不存在"));
    comment.setStatus(status);
    commentRepository.save(comment);
}


   public void deleteComment(Long postId, Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("评论不存在"));

    User user = userRepository.findById(Math.toIntExact(userId))
            .orElseThrow(() ->new RuntimeException("用户不存在"));

    if (!comment.getPostId().equals(postId)) {
        throw new RuntimeException("评论与帖子不匹配");
    }
    if (!comment.getUserId().equals(userId) && user.getRole() != User.UserRole.系统管理员) {
        throw new IllegalArgumentException("无权限删除该评论");
    }

    commentRepository.deleteById(commentId);
    postService.decrementCommentCount(postId);
}


    public Comment getCommentById(Long id) {
    return commentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评论不存在: id = " + id));
    }

    // 获取评论用户ID
    public Integer getUserId(Integer commentId) {
        return commentRepository.getUserId(commentId);
    }
    
    
public List<Comment> getHotComments(int limit) {
    return commentRepository.findTopByOrderByLikeCountDesc(limit);
}

public void deleteCommentsByPostId(Long postId) {
    commentRepository.deleteByPostId(postId);
}

}
