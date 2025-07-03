package com.example.uclub_backend.forum.controller;

import com.example.uclub_backend.forum.entity.Like;
import com.example.uclub_backend.forum.entity.Post;
import com.example.uclub_backend.forum.mapper.PostMapper;
import com.example.uclub_backend.forum.service.LikeService;
import com.example.uclub_backend.forum.service.PostService;
import com.example.uclub_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.uclub_backend.entity.User;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    
   private final UserService userService;

   public PostController(PostService postService, LikeService likeService, UserService userService) {
    this.postService = postService;
    this.likeService = likeService;
    this.userService = userService;
   }

    @PostMapping
    public Map<String, Object> create(@RequestBody Post post) {
        Map<String, Object> res = new HashMap<>();
        try {
            Post saved = postService.save(post);
            res.put("code", 200);
            res.put("message", "发布成功");
            res.put("data", Map.of("post_id", saved.getId()));
        } catch (Exception e) {
            res.put("code", 500);
            res.put("message", "服务器错误: " + e.getMessage());
        }
        return res;
    }

@GetMapping
public Map<String, Object> getPosts(
        @RequestParam(defaultValue = "") String title,
        @RequestParam(defaultValue = "") String clubName,
        @RequestParam(defaultValue = "") String timeRange,
        @RequestParam(required = false) String startTime,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize) {

    Map<String, String> filters = new HashMap<>();
    filters.put("title", title);
    filters.put("clubName", clubName);
    filters.put("timeRange", timeRange);
    if (startTime != null && !startTime.isBlank()) {
        filters.put("startTime", startTime);
    }

    var postPage = postService.getPostPage(filters, page, pageSize);
    List<Post>posts = postPage.getContent();

    List<Map<String,Object>> postListWithUser = new ArrayList<>();
    for(Post post: posts){
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", post.getId());
        postMap.put("title", post.getTitle());
        postMap.put("content", post.getContent());
        postMap.put("clubId", post.getClubId());
        postMap.put("userId", post.getUserId());
        postMap.put("createdAt", post.getCreatedAt());
        postMap.put("likeCount", post.getLikeCount());
        postMap.put("commentCount", post.getCommentCount());
        postMap.put("clubName",post.getClubName());
        postMap.put("timeRange",timeRange);
        if (startTime != null && !startTime.isBlank()) {
            postMap.put("startTime", startTime);
        }


        // 查询用户信息
        User user = userService.getUserById(post.getUserId().intValue());
        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatarUrl", user.getHeadUrl());
            postMap.put("user", userMap); // 嵌入 user 信息
        }

        postListWithUser.add(postMap);
    }

    Map<String, Object> res = new HashMap<>();
    res.put("posts", postListWithUser);
    res.put("total", postPage.getTotalElements());
    return res;
}


     @Autowired
    private PostMapper postMapper; 

 @GetMapping("/hot")
public ResponseEntity<?> getHotPosts() {
    try {
        List<Post> hotPosts = postMapper.selectHotPosts();
        return ResponseEntity.ok(hotPosts);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "查询失败",
            "message", e.getMessage()
        ));
    }
}



@GetMapping("/{id}")
public ResponseEntity<?> getPost(@PathVariable Long id, @RequestParam(required = false) Long userId) {
    try {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", "帖子不存在"));
        }

        // 查询发帖人信息
        User author = userService.getUserById(post.getUserId().intValue());

        // 构造 user 显示信息
        Map<String, Object> userMap = new HashMap<>();
        if (author != null) {
            userMap.put("id", author.getId());
            userMap.put("nickname", author.getNickname());
            userMap.put("avatarUrl", author.getHeadUrl()); 
        }

        // 构造 post 数据
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("id", post.getId());
        postMap.put("title", post.getTitle());
        postMap.put("content", post.getContent());
        postMap.put("clubId", post.getClubId());
        postMap.put("userId", post.getUserId());
        postMap.put("createdAt", post.getCreatedAt());
        postMap.put("likeCount", post.getLikeCount());
        postMap.put("commentCount", post.getCommentCount());
        postMap.put("user", userMap); //  嵌入 user 对象

        // 构造最终响应体
        Map<String, Object> res = new HashMap<>();
        res.put("post", postMap);

        if (userId != null) {
            boolean liked = likeService.hasLiked(userId, Like.TargetType.post, id);
            res.put("liked", liked);
        }

        return ResponseEntity.ok(res);

    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", 404, "message", e.getMessage()));
    }
}


@PostMapping("/{id}/like")
public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id, @RequestParam Long userId) {
    boolean liked = likeService.toggleLike(userId, Like.TargetType.post, id);
    Map<String, Object> res = new HashMap<>();
    res.put("liked", liked);
    res.put("message", liked ? "点赞成功" : "取消点赞");
    return ResponseEntity.ok(res);
}


    

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestParam Integer userId) {
        try {
            postService.deletePostById(id, userId);
            return ResponseEntity.ok(Map.of("code", 200, "message", "删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("code", 403, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", 404, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "服务器错误：" + e.getMessage()));
        }
    }




   

}
