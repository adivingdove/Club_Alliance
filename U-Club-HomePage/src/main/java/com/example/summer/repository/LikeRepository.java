package com.example.summer.repository;

import com.example.summer.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    List<Like> findByUserId(Long userId);
    
    List<Like> findByTargetTypeAndTargetId(Like.TargetType targetType, Long targetId);
    
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, Like.TargetType targetType, Long targetId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.targetType = :targetType AND l.targetId = :targetId")
    Long countByTarget(@Param("targetType") Like.TargetType targetType, @Param("targetId") Long targetId);
    
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, Like.TargetType targetType, Long targetId);
    
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, Like.TargetType targetType, Long targetId);
} 