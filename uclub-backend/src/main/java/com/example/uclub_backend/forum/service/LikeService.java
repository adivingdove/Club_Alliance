package com.example.uclub_backend.forum.service;

import com.example.uclub_backend.forum.entity.Like;
import com.example.uclub_backend.forum.repository.LikeRepository;
import com.example.uclub_backend.forum.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

@Transactional
public boolean toggleLike(Long userId, Like.TargetType targetType, Long targetId) {
    System.out.println("[toggleLike] 查询是否已点赞：userId=" + userId + ", targetType=" + targetType + ", targetId=" + targetId);
    Optional<Like> existing = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

    if (existing.isPresent()) {
        System.out.println("已点赞，执行取消点赞");
        likeRepository.delete(existing.get());
        postRepository.decrementLikeCount(targetId);
        return false;
    } else {
        System.out.println("未点赞，执行新增点赞");
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeRepository.save(like);
        postRepository.incrementLikeCount(targetId);
        return true;
    }
}


public boolean hasLiked(Long userId, Like.TargetType targetType, Long targetId) {
    return likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
}


}
