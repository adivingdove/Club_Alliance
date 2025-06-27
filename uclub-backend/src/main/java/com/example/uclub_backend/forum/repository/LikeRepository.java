package com.example.uclub_backend.forum.repository;

import com.example.uclub_backend.forum.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
   Optional<Like> findByUserIdAndTargetTypeAndTargetId(Long userId, Like.TargetType targetType, Long targetId);
  void deleteByUserIdAndTargetTypeAndTargetId(Long userId, Like.TargetType targetType, Long targetId);

}