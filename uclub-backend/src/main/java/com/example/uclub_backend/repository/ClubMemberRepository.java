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

    List<ClubMember> findByClubIdAndRoleIn(Integer clubId, List<ClubMember.MemberRole> roles);

    // 查询指定社团的管理员（非普通成员）
    @Query("select cm from ClubMember cm where cm.clubId = :clubId and cm.role <> '成员'")
    List<ClubMember> findAdminsByClubId(@Param("clubId") Integer clubId);

    // 查询所有管理员（非普通成员）
    @Query("select cm from ClubMember cm where cm.role <> '成员'")
    List<ClubMember> findAllAdmins();

    @Query("select cm from ClubMember cm where cm.clubId in :clubIds and cm.role <> '成员'")
    List<ClubMember> findByClubIdInAndRoleNot(@Param("clubIds") List<Integer> clubIds,
                                              @Param("role") ClubMember.MemberRole role);

    List<ClubMember> findByClubIdAndJoinStatusAndCollect(Integer clubId, ClubMember.JoinStatus joinStatus, Integer collect);

}