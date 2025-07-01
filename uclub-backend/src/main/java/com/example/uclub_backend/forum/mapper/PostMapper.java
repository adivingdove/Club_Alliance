package com.example.uclub_backend.forum.mapper;

import com.example.uclub_backend.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PostMapper {
@Select("""
    SELECT id, title, content, image_urls AS imageUrls, status,
           club_id AS clubId, user_id AS userId,
           like_count AS likeCount, comment_count AS commentCount,
           created_at AS createdAt
    FROM post
    ORDER BY (like_count + comment_count) DESC
    LIMIT 10
""")
List<Post> selectHotPosts();

// 根据用户ID查询帖子
List<Post> selectByUserId(@org.apache.ibatis.annotations.Param("userId") Long userId,
                         @org.apache.ibatis.annotations.Param("offset") int offset,
                         @org.apache.ibatis.annotations.Param("limit") int limit);

// 统计用户帖子数量
int countByUserId(@org.apache.ibatis.annotations.Param("userId") Long userId);

}
