package com.example.uclub_backend.forum.mapper;
import com.example.uclub_backend.forum.entity.Report;
import com.example.uclub_backend.forum.entity.ReportStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    @Insert("INSERT INTO report (reporter_id, target_type, target_id, reason, status) " +
            "VALUES (#{reporterId}, #{targetType}, #{targetId}, #{reason}, #{status})")
    void insertReport(Report report);

    // 根据状态和关键词分页查询
    List<Report> selectReports(@Param("status") String status,
                               @Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("size") int size);

    // 查询总数（分页用）
    int countReports(@Param("status") String status,
                     @Param("keyword") String keyword);

    // 获取所有（不分页）
    List<Report> getAllReports(@Param("status") String status);

    // 根据 ID 获取举报记录
    Report getReportById(Long id);

    // 修改举报状态
    void updateReportStatus(@Param("id") Long id, @Param("status") ReportStatus status);

    // 删除举报
    void deleteReportById(Long id);
}
