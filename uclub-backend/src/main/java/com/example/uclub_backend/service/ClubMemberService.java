package com.example.uclub_backend.service;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.vo.ApplicationVO;
import com.example.uclub_backend.vo.ClubDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClubMemberService {

    @Autowired
    private ClubMemberRepository clubMemberRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ClubMember> getMembersByClubId(Integer clubId) {
        return clubMemberRepository.findByClubId(clubId);
    }

    public List<ClubMember> getActiveMembersByClubId(Integer clubId) {
        return clubMemberRepository.findByClubIdAndJoinStatus(clubId, ClubMember.JoinStatus.已通过);
    }

    public List<ClubMember> getMembersByUserId(Integer userId) {
        return clubMemberRepository.findByUserId(userId);
    }

    public List<ClubMember> getUserActiveClubs(Integer userId) {
        return clubMemberRepository.findUserActiveClubs(userId);
    }

    public Optional<ClubMember> getMemberById(Integer id) {
        return clubMemberRepository.findById(id);
    }

    public Optional<ClubMember> getMemberByClubIdAndUserId(Integer clubId, Integer userId) {
        return clubMemberRepository.findByClubIdAndUserId(clubId, userId);
    }

    public Optional<ClubMember> getClubPresident(Integer clubId) {
        return clubMemberRepository.findClubPresident(clubId);
    }

    @Transactional
    public ClubMember addMember(ClubMember member) {
        // 检查社团是否存在
        if (!clubRepository.existsById(member.getClubId())) {
            throw new RuntimeException("社团不存在");
        }

        // 检查用户是否存在
        if (!userRepository.existsById(member.getUserId())) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否已经是该社团成员
        if (clubMemberRepository.existsByClubIdAndUserId(member.getClubId(), member.getUserId())) {
            throw new RuntimeException("该用户已经是该社团成员");
        }

        return clubMemberRepository.save(member);
    }

    @Transactional
    public ClubMember updateMember(Integer id, ClubMember member) {
        Optional<ClubMember> existingMember = clubMemberRepository.findById(id);
        if (existingMember.isEmpty()) {
            throw new RuntimeException("成员不存在");
        }

        ClubMember memberToUpdate = existingMember.get();
        memberToUpdate.setRole(member.getRole());
        memberToUpdate.setJoinStatus(member.getJoinStatus());

        return clubMemberRepository.save(memberToUpdate);
    }

    @Transactional
    public void removeMember(Integer id) {
        Optional<ClubMember> member = clubMemberRepository.findById(id);
        if (member.isEmpty()) {
            throw new RuntimeException("成员不存在");
        }

        clubMemberRepository.deleteById(id);
    }

    @Transactional
    public void changeJoinStatus(Integer id, ClubMember.JoinStatus joinStatus) {
        Optional<ClubMember> member = clubMemberRepository.findById(id);
        if (member.isEmpty()) {
            throw new RuntimeException("成员不存在");
        }

        ClubMember memberToUpdate = member.get();
        memberToUpdate.setJoinStatus(joinStatus);
        clubMemberRepository.save(memberToUpdate);
    }

    @Transactional
    public void changeMemberRole(Integer id, ClubMember.MemberRole role) {
        Optional<ClubMember> member = clubMemberRepository.findById(id);
        if (member.isEmpty()) {
            throw new RuntimeException("成员不存在");
        }

        ClubMember memberToUpdate = member.get();
        memberToUpdate.setRole(role);
        clubMemberRepository.save(memberToUpdate);
    }

    public Long getActiveMemberCount(Integer clubId) {
        return clubMemberRepository.countActiveMembersByClubId(clubId);
    }

    public boolean isUserInClub(Integer userId, Integer clubId) {
        return clubMemberRepository.existsByClubIdAndUserId(clubId, userId);
    }

    // 收藏相关方法
    @Transactional
    public boolean addToFavorites(Integer userId, Integer clubId) {
        // 检查是否已经存在记录
        Optional<ClubMember> existingMember = clubMemberRepository.findByUserIdAndClubId(userId, clubId);

        if (existingMember.isPresent()) {
            // 如果记录存在，更新收藏状态
            ClubMember member = existingMember.get();
            member.setCollect(1);
            clubMemberRepository.save(member);
            return true;
        } else {
            // 如果记录不存在，创建新记录（仅收藏，不加入社团）
            ClubMember member = new ClubMember();
            member.setUserId(userId);
            member.setClubId(clubId);
            member.setCollect(1);
            member.setJoinStatus(ClubMember.JoinStatus.已拒绝); // 仅收藏，不加入
            clubMemberRepository.save(member);
            return true;
        }
    }

    @Transactional
    public boolean removeFromFavorites(Integer userId, Integer clubId) {
        Optional<ClubMember> member = clubMemberRepository.findByUserIdAndClubId(userId, clubId);
        if (member.isPresent()) {
            ClubMember memberToUpdate = member.get();
            memberToUpdate.setCollect(0);
            clubMemberRepository.save(memberToUpdate);
            return true;
        }
        return false;
    }

    public boolean isFavorited(Integer userId, Integer clubId) {
        Optional<ClubMember> member = clubMemberRepository.findByUserIdAndClubId(userId, clubId);
        return member.isPresent() && member.get().getCollect() == 1;
    }

    public List<Club> getUserFavorites(Integer userId) {
        List<ClubMember> favorites = clubMemberRepository.findByUserIdAndCollect(userId, 1);
        List<Integer> clubIds = favorites.stream()
                .map(ClubMember::getClubId)
                .collect(Collectors.toList());

        if (clubIds.isEmpty()) {
            return List.of();
        }

        return clubRepository.findAllById(clubIds);
    }

    // 申请加入社团
    @Transactional
    public boolean applyToClub(Integer userId, Integer clubId, String applicant, String reason) {
        // 检查社团是否存在
        if (!clubRepository.existsById(clubId)) {
            throw new RuntimeException("社团不存在");
        }

        // 检查是否已经申请过
        Optional<ClubMember> existingMember = clubMemberRepository.findByUserIdAndClubId(userId, clubId);
        if (existingMember.isPresent()) {
            ClubMember member = existingMember.get();
            if (member.getJoinStatus() == ClubMember.JoinStatus.待审核) {
                throw new RuntimeException("您已经申请过该社团，请等待审核");
            } else if (member.getJoinStatus() == ClubMember.JoinStatus.已通过) {
                throw new RuntimeException("您已经是该社团成员");
            } else if (member.getJoinStatus() == ClubMember.JoinStatus.已拒绝) {
                // 如果之前被拒绝，可以重新申请
                member.setJoinStatus(ClubMember.JoinStatus.待审核);
                member.setApplicantInfo(applicant);
                member.setApplyReason(reason);
                clubMemberRepository.save(member);
                return true;
            }
        }

        // 创建新的申请记录
        ClubMember member = new ClubMember();
        member.setUserId(userId);
        member.setClubId(clubId);
        member.setJoinStatus(ClubMember.JoinStatus.待审核);
        member.setCollect(0); // 默认不收藏
        member.setApplicantInfo(applicant);
        member.setApplyReason(reason);
        clubMemberRepository.save(member);

        return true;
    }

    // 获取用户的申请状态
    public ClubMember.JoinStatus getApplicationStatus(Integer userId, Integer clubId) {
        Optional<ClubMember> member = clubMemberRepository.findByUserIdAndClubId(userId, clubId);
        return member.map(ClubMember::getJoinStatus).orElse(null);
    }

    // 获取用户加入的社团
    public List<Club> getUserJoinedClubs(Integer userId) {
        System.out.println("[DEBUG] getUserJoinedClubs called with userId: " + userId);

        // 获取用户作为成员加入的社团
        List<ClubMember> members = clubMemberRepository.findByUserIdAndJoinStatus(userId, ClubMember.JoinStatus.已通过);
        List<Integer> memberClubIds = members.stream()
                .map(ClubMember::getClubId)
                .collect(Collectors.toList());

        System.out.println("[DEBUG] User joined clubs (as member): " + memberClubIds);

        // 获取用户创建的社团
        List<Club> createdClubs = clubRepository.findByCreatorId(userId);
        List<Integer> createdClubIds = createdClubs.stream()
                .map(Club::getId)
                .collect(Collectors.toList());

        System.out.println("[DEBUG] User created clubs: " + createdClubIds);

        // 合并所有社团ID（去重）
        Set<Integer> allClubIds = new HashSet<>();
        allClubIds.addAll(memberClubIds);
        allClubIds.addAll(createdClubIds);

        System.out.println("[DEBUG] All club IDs: " + allClubIds);

        if (allClubIds.isEmpty()) {
            System.out.println("[DEBUG] No clubs found for user " + userId);
            return List.of();
        }

        List<Club> result = clubRepository.findAllById(allClubIds);
        System.out.println("[DEBUG] Final result: " + result.size() + " clubs");
        return result;
    }

    // 获取社团创建者收到的申请信息
    public Map<String, List<ApplicationVO>> getApplicationsByCreator(Integer creatorId) {
        // 获取该创建者创建的所有社团
        List<Club> creatorClubs = clubRepository.findByCreatorId(creatorId);
        List<Integer> clubIds = creatorClubs.stream()
                .map(Club::getId)
                .collect(Collectors.toList());

        if (clubIds.isEmpty()) {
            return Map.of("pending", List.of(), "processed", List.of());
        }

        // 获取这些社团的所有申请
        List<ClubMember> allApplications = clubMemberRepository.findByClubIdIn(clubIds);

        // 分离待审核和已处理的申请
        List<ClubMember> pendingApplications = allApplications.stream()
                .filter(app -> app.getJoinStatus() == ClubMember.JoinStatus.待审核)
                .collect(Collectors.toList());

        List<ClubMember> processedApplications = allApplications.stream()
                .filter(app -> app.getJoinStatus() != ClubMember.JoinStatus.待审核)
                .collect(Collectors.toList());

        // 转换为VO
        List<ApplicationVO> pendingVOs = convertToApplicationVO(pendingApplications, creatorClubs);
        List<ApplicationVO> processedVOs = convertToApplicationVO(processedApplications, creatorClubs);

        return Map.of("pending", pendingVOs, "processed", processedVOs);
    }

    // 处理申请（通过或拒绝）
    @Transactional
    public boolean processApplication(Integer applicationId, Integer creatorId, String action) {
        Optional<ClubMember> application = clubMemberRepository.findById(applicationId);
        if (application.isEmpty()) {
            throw new RuntimeException("申请不存在");
        }

        ClubMember app = application.get();

        // 验证创建者权限
        Club club = clubRepository.findById(app.getClubId()).orElse(null);
        if (club == null || !club.getCreatorId().equals(creatorId)) {
            throw new RuntimeException("无权限处理此申请");
        }

        // 更新申请状态
        if ("approve".equals(action)) {
            app.setJoinStatus(ClubMember.JoinStatus.已通过);
        } else if ("reject".equals(action)) {
            app.setJoinStatus(ClubMember.JoinStatus.已拒绝);
        } else {
            throw new RuntimeException("无效的操作");
        }

        clubMemberRepository.save(app);
        return true;
    }

    // 转换ClubMember为ApplicationVO
    private List<ApplicationVO> convertToApplicationVO(List<ClubMember> members, List<Club> clubs) {
        Map<Integer, Club> clubMap = clubs.stream()
                .collect(Collectors.toMap(Club::getId, club -> club));

        return members.stream().map(member -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(member.getId());
            vo.setClubId(member.getClubId());
            vo.setUserId(member.getUserId());
            vo.setStatus(member.getJoinStatus().name());
            vo.setAppliedAt(member.getJoinedAt());
            vo.setApplicantInfo(member.getApplicantInfo());
            vo.setReason(member.getApplyReason());

            // 设置社团信息
            Club club = clubMap.get(member.getClubId());
            if (club != null) {
                vo.setClubName(club.getName());
                vo.setCreatorId(club.getCreatorId());
            }

            // 设置申请人姓名（从申请人信息中提取或使用默认值）
            String applicantName = member.getApplicantInfo();
            if (applicantName != null && applicantName.contains("/")) {
                applicantName = applicantName.split("/")[0]; // 取姓名部分
            } else if (applicantName == null || applicantName.isEmpty()) {
                applicantName = "用户" + member.getUserId();
            }
            vo.setApplicantName(applicantName);

            return vo;
        }).collect(Collectors.toList());
    }

    // 任命成员角色
    @Transactional
    public boolean appointMemberRole(Integer clubId, Integer memberId, Integer creatorId, String role) {
        // 验证创建者权限
        Club club = clubRepository.findById(clubId).orElse(null);
        if (club == null || !club.getCreatorId().equals(creatorId)) {
            throw new RuntimeException("无权限进行此操作");
        }

        // 获取成员记录
        Optional<ClubMember> memberOpt = clubMemberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("成员不存在");
        }

        ClubMember member = memberOpt.get();
        if (!member.getClubId().equals(clubId)) {
            throw new RuntimeException("成员不属于该社团");
        }

        // 验证角色
        ClubMember.MemberRole memberRole;
        try {
            memberRole = ClubMember.MemberRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的角色");
        }

        // 不能任命自己为其他角色（社长不能改变自己的角色）
        if (member.getUserId().equals(creatorId)) {
            throw new RuntimeException("不能修改自己的角色");
        }

        // 更新角色
        member.setRole(memberRole);
        clubMemberRepository.save(member);

        return true;
    }

    // 获取社团成员列表（用于管理）
    public List<ClubDetailVO.ClubMemberVO> getClubMembersForManagement(Integer clubId, Integer creatorId) {
        // 验证创建者权限
        Club club = clubRepository.findById(clubId).orElse(null);
        if (club == null || !club.getCreatorId().equals(creatorId)) {
            throw new RuntimeException("无权限查看成员列表");
        }

        // 获取所有已通过的成员
        List<ClubMember> members = clubMemberRepository.findByClubIdAndJoinStatus(clubId, ClubMember.JoinStatus.已通过);

        return members.stream().map(member -> {
            ClubDetailVO.ClubMemberVO memberVO = new ClubDetailVO.ClubMemberVO();
            memberVO.setId(member.getId());
            memberVO.setUserId(member.getUserId());
            memberVO.setRole(member.getRole().name());
            memberVO.setJoinStatus(member.getJoinStatus().name());
            memberVO.setJoinedAt(member.getJoinedAt());

            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(member.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                memberVO.setName(user.getNickname());
                memberVO.setAvatar(user.getHeadUrl());
            } else {
                memberVO.setName("用户" + member.getUserId());
                memberVO.setAvatar(null);
            }

            return memberVO;
        }).collect(Collectors.toList());
    }

    public List<ClubMember> getClubAdminsByClubId(Integer clubId) {
        // 查询指定社团中角色为干事、副社长、社长的成员
        return clubMemberRepository.findByClubIdAndRoleIn(
                clubId,
                Arrays.asList(ClubMember.MemberRole.干事, ClubMember.MemberRole.副社长, ClubMember.MemberRole.社长)
        );
    }

    @Transactional
    public void revokeAdminRole(Integer memberId) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("成员不存在"));
        // 角色变为普通成员
        member.setRole(ClubMember.MemberRole.成员);
        clubMemberRepository.save(member);
    }

    // 获取所有管理员（排除普通成员）
    public List<ClubMember> getAllAdmins() {
        return clubMemberRepository.findAllAdmins();
    }

    // 根据社团名称模糊查询管理员
    public List<ClubMember> getAdminsByClubName(String clubName) {
        List<Club> clubs = clubRepository.findByNameContaining(clubName);
        if (clubs.isEmpty()) return List.of();

        List<Integer> clubIds = clubs.stream().map(Club::getId).toList();

        // 查询所有这些社团的管理员
        return clubMemberRepository.findByClubIdInAndRoleNot(clubIds, ClubMember.MemberRole.成员);
    }


}