package com.example.summer.repository;

import com.example.summer.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByPostId(Long postId);
    
    List<Comment> findByUserId(Long userId);
    
    List<Comment> findByStatus(Comment.CommentStatus status);
    
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.status = 'active' ORDER BY c.createdAt ASC")
    List<Comment> findActiveCommentsByPostId(@Param("postId") Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword%")
    List<Comment> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.status = 'active'")
    Long countActiveCommentsByPostId(@Param("postId") Long postId);
} 