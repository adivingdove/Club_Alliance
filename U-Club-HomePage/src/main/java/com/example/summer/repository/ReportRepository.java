package com.example.summer.repository;

import com.example.summer.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    
    List<Report> findByReporterId(Integer reporterId);
    
    List<Report> findByStatus(Report.ReportStatus status);
    
    List<Report> findByTargetTypeAndTargetId(Report.TargetType targetType, Integer targetId);
    
    @Query("SELECT r FROM Report r WHERE r.status = '待处理'")
    List<Report> findPendingReports();
    
    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType")
    List<Report> findByTargetType(@Param("targetType") Report.TargetType targetType);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetType = :targetType AND r.targetId = :targetId")
    Long countByTarget(@Param("targetType") Report.TargetType targetType, @Param("targetId") Integer targetId);
} 