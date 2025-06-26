package com.example.uclub_backend.forum.repository;

import com.example.uclub_backend.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

   @Query("SELECT p FROM Post p " +
       "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
       "AND (:clubId IS NULL OR p.club_id = :clubId) " +
       "AND (:startTime IS NULL OR p.created_at >= :startTime) " +
       "AND p.status = :status")
Page<Post> findByFilters(
        @Param("title") String title,
        @Param("clubId") Long clubId,
        @Param("startTime") LocalDateTime startTime,
        @Param("status") String status,
        Pageable pageable
);

}
