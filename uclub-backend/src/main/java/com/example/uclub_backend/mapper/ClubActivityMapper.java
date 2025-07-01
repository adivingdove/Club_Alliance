package com.example.uclub_backend.mapper;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubActivity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ClubActivityMapper {

    @Select("SELECT * FROM club_activity WHERE apply_status = #{status}")
    List<ClubActivity> findByApplyStatus(String status);

    @Select("SELECT * FROM club_activity WHERE apply_status != #{status}")
    List<ClubActivity> findByApplyStatusNot(String status);

    @Update("UPDATE club_activity SET apply_status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateApplyStatus(@Param("id") Long id,
                           @Param("status") String status,
                           @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT * FROM club_activity WHERE club_id = #{clubId} AND end_time < NOW() ORDER BY end_time DESC")
    List<ClubActivity> selectHistoryByClubId(@Param("clubId") Integer clubId);

    @Select("SELECT * FROM club_activity WHERE id = #{id}")
    ClubActivity selectById(@Param("id") Integer id);
}
