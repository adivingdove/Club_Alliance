package com.example.uclub_backend.forum.repository;

import com.example.uclub_backend.forum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
