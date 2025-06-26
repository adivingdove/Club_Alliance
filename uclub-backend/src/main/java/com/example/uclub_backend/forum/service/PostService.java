package com.example.uclub_backend.forum.service;
import com.example.uclub_backend.club.entity.Club;

import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.repository.ClubRepository;
import com.example.uclub_backend.forum.repository.PostRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
   private final PostRepository postRepository;
    private final ClubRepository clubRepository;

    public PostService(PostRepository postRepository, ClubRepository clubRepository) {
        this.postRepository = postRepository;
        this.clubRepository = clubRepository;
    }

    public Page<Post> getPostPage(Map<String, String> filters, int page, int size) {
    String title = filters.getOrDefault("title", "");
    String clubName = filters.getOrDefault("clubName", "");
    String timeRange = filters.getOrDefault("timeRange", "");
    String status = "active";

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

    LocalDateTime startTime = null;
    if ("today".equals(timeRange)) {
        startTime = LocalDate.now().atStartOfDay();
    } else if ("7days".equals(timeRange)) {
        startTime = LocalDateTime.now().minusDays(7);
    } else if ("30days".equals(timeRange)) {
        startTime = LocalDateTime.now().minusDays(30);
    }

    Long clubId = null;
    if (!clubName.isBlank()) {
        clubId = clubRepository.findByName(clubName)
                .map(Club::getId)
                .orElse(null);
    }

    
    Page<Post> postPage = postRepository.findByFilters(title, clubId, startTime, status, pageable);

    for (Post post : postPage.getContent()) {
        clubRepository.findById(post.getClub_id())
                .ifPresent(club -> post.setClubName(club.getName()));
    }

    return postPage;
}

    // 保存帖子（发帖功能）
    public Post save(Post post) {
        return postRepository.save(post);
    }

    // 查所有帖子（不分页）
    public List<Post> findAll() {
        return postRepository.findAll();
    }
    
    

public void incrementLikeCount(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("帖子不存在"));
    post.setLikeCount(post.getLikeCount() + 1);
    postRepository.save(post);
}
public Post getPostById(Long id) {
    return postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("帖子不存在"));
}

public void deletePostById(Long id, Long userId) {
    Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("帖子不存在"));

    if (!post.getUser_id().equals(userId)) {
        throw new IllegalArgumentException("无权限删除该帖子");
    }

    postRepository.deleteById(id);
}

}
