package com.example.uclub_backend.forum.mapper;
import com.example.uclub_backend.forum.entity.Report;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper {

    @Insert("INSERT INTO report (reporter_id, target_type, target_id, reason, status) " +
            "VALUES (#{reporterId}, #{targetType}, #{targetId}, #{reason}, #{status})")
    void insertReport(Report report);
}
