package com.example.uclub_backend.forum.handler;

import com.example.uclub_backend.forum.entity.ReportStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;

public class ReportStatusHandler extends BaseTypeHandler<ReportStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ReportStatus status, JdbcType jdbcType) throws SQLException {
        ps.setString(i, status.name()); // 使用枚举的名称，如 "待处理"
    }

    @Override
    public ReportStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return ReportStatus.valueOf(rs.getString(columnName));
    }

    @Override
    public ReportStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return ReportStatus.valueOf(rs.getString(columnIndex));
    }

    @Override
    public ReportStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return ReportStatus.valueOf(cs.getString(columnIndex));
    }
}
