package com.example.uclub_backend.forum.repository;

import com.example.uclub_backend.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

@Query("SELECT p FROM Post p " +
       "WHERE (:title IS NULL OR p.title LIKE %:title%) " +
       "AND (:clubId IS NULL OR p.clubId = :clubId) " +
       "AND (:createdAfter IS NULL OR p.createdAt >= :createdAfter) " +
       "AND p.status = 'active'")
Page<Post> findByFilters(@Param("title") String title,
                         @Param("clubId") Long clubId,
                         @Param("createdAfter") LocalDateTime createdAfter,
                         Pageable pageable);

@Modifying
@Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
void incrementCommentCount(@Param("postId") Long postId);

@Modifying
@Query("UPDATE Post p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END WHERE p.id = :postId")
void decrementCommentCount(@Param("postId") Long postId);

@Modifying
@Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
void incrementLikeCount(@Param("postId") Long postId);

@Modifying
@Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
void decrementLikeCount(@Param("postId") Long postId);

@Query("SELECT DISTINCT p.clubId FROM Post p WHERE p.createdAt >= :since")
List<Integer> findRecentActiveClubIds(@Param("since") LocalDateTime since);



// 根据用户ID查询帖子，按创建时间倒序排列
List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

// 查询作者ID
@Query("SELECT p.userId FROM Post p WHERE p.id = :id")
Integer getUserId(@Param("id") Integer id);

@Query(value = """
    SELECT p.* FROM post p
    LEFT JOIN club c ON p.club_id = c.id
    WHERE (:title IS NULL OR p.title LIKE :title)
      AND (:clubName IS NULL OR c.name LIKE :clubName)
      AND (:createdAfter IS NULL OR p.created_at >= :createdAfter)
      AND p.status = 'active'
    ORDER BY p.id DESC
    """,
    countQuery = """
    SELECT COUNT(*) FROM post p
    LEFT JOIN club c ON p.club_id = c.id
    WHERE (:title IS NULL OR p.title LIKE :title)
      AND (:clubName IS NULL OR c.name LIKE :clubName)
      AND (:createdAfter IS NULL OR p.created_at >= :createdAfter)
      AND p.status = 'active'
    """,
    nativeQuery = true)
Page<Post> findByFiltersWithClubName(
    @Param("title") String title,
    @Param("clubName") String clubName,
    @Param("createdAfter") LocalDateTime createdAfter,
    Pageable pageable
);

//修正评论数
 @Modifying
    @Query("UPDATE Post p SET p.commentCount = :count WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("count") int count);

}
