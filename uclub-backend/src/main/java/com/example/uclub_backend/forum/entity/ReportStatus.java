package com.example.uclub_backend.forum.entity;

import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.JdbcType;


@MappedTypes(ReportStatus.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public enum ReportStatus {
    待处理,
    已处理
}
