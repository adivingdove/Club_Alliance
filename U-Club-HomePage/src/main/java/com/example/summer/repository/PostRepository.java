package com.example.summer.repository;

import com.example.summer.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByClubId(Long clubId);
    
    List<Post> findByUserId(Long userId);
    
    List<Post> findByStatus(Post.PostStatus status);
    
    @Query("SELECT p FROM Post p WHERE p.clubId = :clubId AND p.status = 'active' ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByClubId(@Param("clubId") Long clubId);
    
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'active' ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<Post> findPopularPosts();
    
    @Query("SELECT p FROM Post p WHERE p.status = 'active' ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts();
}