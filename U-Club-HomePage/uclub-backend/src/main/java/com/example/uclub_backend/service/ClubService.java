package com.example.uclub_backend.service;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.ClubRepository;
import com.example.uclub_backend.repository.UserRepository;
import com.example.uclub_backend.repository.ClubMemberRepository;
import com.example.uclub_backend.vo.ClubDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClubService {
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClubMemberRepository clubMemberRepository;
    
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }
    
    public List<Club> getActiveClubs() {
        return clubRepository.findActiveClubs();
    }
    
    public List<Club> getPendingClubs() {
        return clubRepository.findPendingClubs();
    }
    
    public List<Club> getClubsByCreatorId(Integer creatorId) {
        return clubRepository.findByCreatorId(creatorId);
    }
    
    public List<Club> searchClubs(String keyword) {
        return clubRepository.findByKeyword(keyword);
    }
    
    public Optional<Club> getClubById(Integer id) {
        return clubRepository.findById(id);
    }
    
    @Transactional
    public Club createClub(Club club) {
        // 检查创建者是否存在
        if (!userRepository.existsById(club.getCreatorId())) {
            throw new RuntimeException("创建者不存在");
        }
        
        if (clubRepository.existsByName(club.getName())) {
            throw new RuntimeException("社团名称已存在");
        }
        
        // 保存社团
        Club savedClub = clubRepository.save(club);
        
        // 自动添加创建者为社长
        ClubMember member = new ClubMember();
        member.setUserId(club.getCreatorId());
        member.setClubId(savedClub.getId());
        member.setRole(ClubMember.MemberRole.社长);
        member.setJoinStatus(ClubMember.JoinStatus.已通过);
        member.setCollect(0); // 默认不收藏自己创建的社团
        clubMemberRepository.save(member);
        
        return savedClub;
    }
    
    @Transactional
    public Club updateClub(Integer id, Club club) {
        Optional<Club> existingClub = clubRepository.findById(id);
        if (existingClub.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        
        Club clubToUpdate = existingClub.get();
        clubToUpdate.setName(club.getName());
        clubToUpdate.setLogoUrl(club.getLogoUrl());
        clubToUpdate.setTags(club.getTags());
        clubToUpdate.setDescription(club.getDescription());
        clubToUpdate.setStatus(club.getStatus());
        
        return clubRepository.save(clubToUpdate);
    }
    
    @Transactional
    public void deleteClub(Integer id) {
        if (!clubRepository.existsById(id)) {
            throw new RuntimeException("社团不存在");
        }
        clubRepository.deleteById(id);
    }
    
    @Transactional
    public void updateClubStatus(Integer id, Club.ClubStatus status) {
        Optional<Club> club = clubRepository.findById(id);
        if (club.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        
        Club clubToUpdate = club.get();
        clubToUpdate.setStatus(status);
        clubRepository.save(clubToUpdate);
    }
    
    public List<Club> getClubsByStatus(Club.ClubStatus status) {
        return clubRepository.findByStatus(status);
    }
    
    public void handleClubApplication(Integer clubId, Integer creatorId, String applicant, String reason) {
        if (clubId == null || creatorId == null || applicant == null || reason == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // TODO: 保存申请到数据库，通知社团创建者等
        System.out.println("[ClubApplication] clubId=" + clubId + ", creatorId=" + creatorId + ", applicant=" + applicant + ", reason=" + reason);
    }
    
    // 获取社团详情（包含成员信息）
    public ClubDetailVO getClubDetail(Integer clubId) {
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new RuntimeException("社团不存在");
        }
        
        Club club = clubOpt.get();
        ClubDetailVO detailVO = new ClubDetailVO();
        
        // 复制社团基本信息
        detailVO.setId(club.getId());
        detailVO.setName(club.getName());
        detailVO.setLogoUrl(club.getLogoUrl());
        detailVO.setTags(club.getTags());
        detailVO.setCreatorId(club.getCreatorId());
        detailVO.setDescription(club.getDescription());
        detailVO.setStatus(club.getStatus().name());
        detailVO.setCreatedAt(club.getCreatedAt());
        detailVO.setType(club.getType());
        
        // 获取成员信息
        List<ClubMember> members = clubMemberRepository.findByClubId(clubId);
        List<ClubDetailVO.ClubMemberVO> memberVOs = members.stream()
                .map(member -> {
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
                })
                .collect(Collectors.toList());
        
        detailVO.setMembers(memberVOs);
        detailVO.setActivities(List.of()); // 暂时设为空，后续可以添加活动信息
        
        return detailVO;
    }
} 