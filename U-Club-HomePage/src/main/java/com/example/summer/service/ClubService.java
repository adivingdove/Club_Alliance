package com.example.summer.service;

import com.example.summer.entity.Club;
import com.example.summer.repository.ClubRepository;
import com.example.summer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClubService {
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UserRepository userRepository;
    
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
        
        return clubRepository.save(club);
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
} 