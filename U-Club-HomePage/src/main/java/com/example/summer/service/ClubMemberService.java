package com.example.summer.service;

import com.example.summer.entity.ClubMember;
import com.example.summer.repository.ClubMemberRepository;
import com.example.summer.repository.ClubRepository;
import com.example.summer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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
} 