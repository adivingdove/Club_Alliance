package com.example.uclub_backend.forum.service;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.repository.ForumClubRepository;
import com.example.uclub_backend.forum.repository.PostRepository;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ForumClubRepository forumClubRepository;

    @Autowired
    private UserRepository userRepository;

    public PostService(PostRepository postRepository, ForumClubRepository forumClubRepository) {
        this.postRepository = postRepository;
        this.forumClubRepository = forumClubRepository;
    }

    //  分页查询

public Page<Post> getPostPage(Map<String, String> filters, int page, int size) {
    String title = filters.getOrDefault("title", "").trim();
    String clubName = filters.getOrDefault("clubName", "").trim();

    String timeRange = filters.getOrDefault("timeRange", "");
    String startTimeStr = filters.get("startTime"); // 前端显式传入
    String fuzzyTitle = title.isEmpty() ? null : "%" + title + "%";
    String fuzzyClubName = clubName.isEmpty() ? null : "%" + clubName + "%";
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

    // 支持 startTime 优先（前端传 ISO 字符串）
    LocalDateTime startTime = null;
    if (startTimeStr != null && !startTimeStr.isBlank()) {
        try {
           startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            // 可记录日志，继续使用 timeRange fallback
        }
    } else {
        if ("today".equals(timeRange)) {
            startTime = LocalDate.now().atStartOfDay();
        } else if ("7days".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(7);
        } else if ("30days".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(30);
        }
    }
    
   
   
Page<Post> postPage = postRepository.findByFiltersWithClubName(
    fuzzyTitle,
    fuzzyClubName,
    startTime,
    pageable
);

    // 填充社团名
    for (Post post : postPage.getContent()) {
        forumClubRepository.findById(post.getClubId())
                .ifPresent(club -> post.setClubName(club.getName()));
    }

    return postPage;
}


    // 发帖
    public Post save(Post post) {
        return postRepository.save(post);
    }

    //  查所有（不分页）
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    //  点赞
    public void incrementLikeCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    // 查单个帖子
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
    }

    //  删除
    public void deletePostById(Long id, Integer userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        System.out.println("当前用户的角色为："+user.getRole());

        if (!post.getUserId().equals(userId) && user.getRole() != User.UserRole.系统管理员) {
            throw new IllegalArgumentException("无权限删除该帖子");
        }

        postRepository.deleteById(id);
    }

    @Transactional
    // 评论数 +1
     public void incrementCommentCount(Long postId) {
        postRepository.incrementCommentCount(postId);
     }

    // 评论数 -1
     @Transactional
      public void decrementCommentCount(Long postId) {
        postRepository.decrementCommentCount(postId);
     }

    // 获取用户ID
    public Integer getUserId(Integer postId) {
        return postRepository.getUserId(postId);
    }
}
