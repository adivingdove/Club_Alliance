package com.example.uclub_backend.forum.service;

import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;

import com.example.uclub_backend.forum.repository.CommentRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    return commentRepository.findByPostIdAndStatus(postId, CommentStatus.active);
   }

public void addComment(Comment comment) {
    if (comment.getId() == null) {
        comment.setStatus(CommentStatus.active);
        comment.setLikeCount(0);
    }
    commentRepository.save(comment);

    // 只有 active 状态才真正 +1
    if (comment.getStatus() == CommentStatus.active) {
        postService.incrementCommentCount(comment.getPostId());
    }
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
            .orElseThrow(() -> new RuntimeException("用户不存在"));

    if (!comment.getPostId().equals(postId)) {
        throw new RuntimeException("评论与帖子不匹配");
    }
    if (!comment.getUserId().equals(userId) && user.getRole() != User.UserRole.系统管理员) {
        throw new IllegalArgumentException("无权限删除该评论");
    }

    commentRepository.deleteById(commentId);

}


public Comment getCommentById(Long id) {
    Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("评论不存在: id = " + id));
    if (comment.getStatus() != CommentStatus.active) {
        throw new RuntimeException("评论已被隐藏: id = " + id);
    }
    return comment;
}


    // 获取评论用户ID
    public Integer getUserId(Integer commentId) {
        return commentRepository.getUserId(commentId);
    }
    
    
  public List<Comment> getHotComments(int limit) {
    return commentRepository.findHotCommentsByStatus(CommentStatus.active, PageRequest.of(0, limit));
}


  public void deleteCommentsByPostId(Long postId) {
    commentRepository.deleteByPostId(postId);
  }

  
    public void updateCommentStatus(Long postId, Long commentId, String statusStr) {
    Optional<Comment> commentOptional = commentRepository.findById(commentId);
    if (commentOptional.isPresent()) {
        Comment comment = commentOptional.get();
        CommentStatus oldStatus = comment.getStatus();
        CommentStatus newStatus = CommentStatus.valueOf(statusStr);

        comment.setStatus(newStatus);
        commentRepository.save(comment);

        // 若是 active → hidden，减 1；hidden → active，加 1
        if (oldStatus == CommentStatus.active && newStatus != CommentStatus.active) {
            postService.decrementCommentCount(postId);
        } else if (oldStatus != CommentStatus.active && newStatus == CommentStatus.active) {
            postService.incrementCommentCount(postId);
        }
       }
    }



   

}
