package com.example.uclub_backend.mapper;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClubMapper {

    /**
     * 获取所有社团
     */
    List<Club> selectAll();

    /**
     * 根据ID获取社团
     */
    Club selectById(@Param("id") Long id);

    /**
     * 根据状态获取社团
     */
    List<Club> selectByStatus(@Param("status") String status);

    /**
     * 根据分类获取社团
     */
    List<Club> selectByTags(@Param("tags") String tags);

    /**
     * 搜索社团
     */
    List<Club> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取可加入的社团（未满员）
     */
    List<Club> selectAvailableClubs();

    /**
     * 插入社团
     */
    int insert(Club club);

    /**
     * 更新社团
     */
    int update(Club club);

    /**
     * 删除社团
     */
    int deleteById(@Param("id") Long id);

    /**
     * 检查社团名称是否存在
     */
    boolean existsByName(@Param("name") String name);

    /**
     * 更新社团成员数量
     */
    int updateMemberCount(@Param("id") Long id, @Param("currentMembers") Integer currentMembers);

    @Select("SELECT * FROM club_member WHERE role != '成员'")
    List<ClubMember> findAdmins();
}