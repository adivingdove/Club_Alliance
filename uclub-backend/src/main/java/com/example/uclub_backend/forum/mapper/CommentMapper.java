package com.example.uclub_backend.forum.mapper;

import com.example.uclub_backend.forum.entity.Comment;
import com.example.uclub_backend.forum.entity.CommentStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT * FROM comment WHERE id = #{commentId}")
    Comment findById(@Param("commentId") Long commentId);

    @Select("SELECT * FROM comment WHERE post_id = #{postId} AND status = 'active' ORDER BY created_at DESC")
    List<Comment> findByPostId(Long postId);

    @Insert("INSERT INTO comment(post_id, user_id, content, status, like_count, created_at) " +
            "VALUES(#{postId}, #{userId}, #{content}, #{status}, #{likeCount}, NOW())")
    void insert(Comment comment);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE comment SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") CommentStatus status);

    @Delete("DELETE FROM comment WHERE id = #{commentId}")
    void deleteById(@Param("commentId") Long commentId);
}
