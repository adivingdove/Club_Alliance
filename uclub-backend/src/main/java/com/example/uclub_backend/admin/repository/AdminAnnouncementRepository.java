package com.example.uclub_backend.admin.repository;

import com.example.uclub_backend.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface AdminAnnouncementRepository extends JpaRepository<Announcement, Integer> {

    // 查询所有系统公告（club 为 null）
    List<Announcement> findByClubIdIsNull();

    // 根据id查找系统公告（确保club为null）
    Announcement findByIdAndClubIdIsNull(Integer id);
}