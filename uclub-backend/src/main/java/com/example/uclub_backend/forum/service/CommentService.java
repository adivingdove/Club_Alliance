package com.example.uclub_backend.forum.service;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;
import com.example.uclub_backend.forum.mapper.CommentMapper;
import com.example.uclub_backend.forum.repository.CommentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

   private final CommentRepository commentRepository;

   private final CommentMapper commentMapper;

   private final PostService postService;

   public CommentService(CommentMapper commentMapper, PostService postService,CommentRepository commentRepository) {
     this.commentMapper = commentMapper;
     this.postService = postService;
     this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentMapper.findByPostId(postId);
    }

    public void addComment(Comment comment) {
        comment.setStatus(CommentStatus.active); // 默认状态
        comment.setLikeCount(0);
        commentMapper.insert(comment);
    }

    public void likeComment(Long commentId) {
        commentMapper.incrementLikeCount(commentId);
    }

    public void updateStatus(Long commentId, CommentStatus status) {
        commentMapper.updateStatus(commentId, status);
    }
    public void save(Comment comment) {
        if (comment.getId() == null) {
            addComment(comment);
        } else {

            addComment(comment);
        }
    }

    public void deleteComment(Long postId, Long commentId, Long userId) {
    Comment comment = commentMapper.findById(commentId);
    if (comment == null || !comment.getPostId().equals(postId)) {
        throw new RuntimeException("评论不存在");
    }
    if (!comment.getUserId().equals(userId)) {
        throw new IllegalArgumentException("无权限删除该评论");
    }
    commentMapper.deleteById(commentId);
    postService.decrementCommentCount(postId);
   }

    public Comment findById(Long commentId) {
        return commentMapper.findById(commentId);
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


}
