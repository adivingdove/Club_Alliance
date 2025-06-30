package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Integer> {

    List<Club> findByStatus(Club.ClubStatus status);

    List<Club> findByCreatorId(Integer creatorId);

    @Query("SELECT c FROM Club c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword% OR c.tags LIKE %:keyword%")
    List<Club> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Club c WHERE c.status = '正常'")
    List<Club> findActiveClubs();

    @Query("SELECT c FROM Club c WHERE c.status = '待审核'")
    List<Club> findPendingClubs();

    boolean existsByName(String name);

    // 关键字模糊查询（搜索名字或描述等字段）
    @Query("SELECT c FROM Club c WHERE (:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)")
    Page<Club> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Club> findByNameContaining(String keyword);

}