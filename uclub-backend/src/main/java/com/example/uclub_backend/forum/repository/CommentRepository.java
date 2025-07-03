package com.example.uclub_backend.forum.repository;

import com.example.uclub_backend.forum.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 点赞数 +1
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // 点赞数 -1（避免负数）
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END WHERE c.id = :id")
    void decrementLikeCount(@Param("id") Long id);

   
    // 查询评论用户ID
    @Query("SELECT c.userId FROM Comment c WHERE c.id = :id")
    Integer getUserId(@Param("id") Integer id);

    @Query("SELECT c FROM Comment c ORDER BY c.likeCount DESC")
   List<Comment> findTopByOrderByLikeCountDesc(Pageable pageable);

    // 封装一下方便传参
 default List<Comment> findTopByOrderByLikeCountDesc(int limit) {
    return findTopByOrderByLikeCountDesc(PageRequest.of(0, limit));
}

}
