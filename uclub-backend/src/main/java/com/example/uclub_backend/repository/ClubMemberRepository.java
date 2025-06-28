package com.example.uclub_backend.repository;

import com.example.uclub_backend.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Integer> {

    List<ClubMember> findByClubId(Integer clubId);

    List<ClubMember> findByUserId(Integer userId);

    List<ClubMember> findByClubIdAndJoinStatus(Integer clubId, ClubMember.JoinStatus joinStatus);

    Optional<ClubMember> findByClubIdAndUserId(Integer clubId, Integer userId);

    @Query("SELECT cm FROM ClubMember cm WHERE cm.clubId = :clubId AND cm.role = '社长'")
    Optional<ClubMember> findClubPresident(@Param("clubId") Integer clubId);

    @Query("SELECT COUNT(cm) FROM ClubMember cm WHERE cm.clubId = :clubId AND cm.joinStatus = '已通过'")
    Long countActiveMembersByClubId(@Param("clubId") Integer clubId);

    boolean existsByClubIdAndUserId(Integer clubId, Integer userId);

    @Query("SELECT cm FROM ClubMember cm WHERE cm.userId = :userId AND cm.joinStatus = '已通过'")
    List<ClubMember> findUserActiveClubs(@Param("userId") Integer userId);

    Optional<ClubMember> findByUserIdAndClubId(Integer userId, Integer clubId);

    List<ClubMember> findByUserIdAndCollect(Integer userId, Integer collect);

    List<ClubMember> findByUserIdAndJoinStatus(Integer userId, ClubMember.JoinStatus joinStatus);

    List<ClubMember> findByClubIdIn(List<Integer> clubIds);
}