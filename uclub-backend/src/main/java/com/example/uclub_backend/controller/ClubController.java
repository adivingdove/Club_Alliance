package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.service.ClubService;
import com.example.uclub_backend.service.ClubMemberService;
import com.example.uclub_backend.vo.Result;
import com.example.uclub_backend.vo.ApplicationVO;
import com.example.uclub_backend.vo.ClubDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private ClubMemberService clubMemberService;
    
    @GetMapping
    public Result<List<Club>> getAllClubs() {
        try {
            List<Club> clubs = clubService.getAllClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/active")
    public Result<List<Club>> getActiveClubs() {
        try {
            List<Club> clubs = clubService.getActiveClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result<List<Club>> getPendingClubs() {
        try {
            List<Club> clubs = clubService.getPendingClubs();
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/creator/{creatorId}")
    public Result<List<Club>> getClubsByCreatorId(@PathVariable Integer creatorId) {
        try {
            List<Club> clubs = clubService.getClubsByCreatorId(creatorId);
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public Result<List<Club>> searchClubs(@RequestParam String keyword) {
        try {
            List<Club> clubs = clubService.searchClubs(keyword);
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Club> getClubById(@PathVariable Integer id) {
        try {
            Club club = clubService.getClubById(id).orElse(null);
            if (club == null) {
                return Result.error(404, "社团不存在");
            }
            return Result.success(club);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/detail")
    public Result<ClubDetailVO> getClubDetail(@PathVariable Integer id) {
        try {
            ClubDetailVO clubDetail = clubService.getClubDetail(id);
            return Result.success(clubDetail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping
    public Result<Club> createClub(@RequestBody Club club) {
        try {
            Club createdClub = clubService.createClub(club);
            return Result.success(createdClub);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result<Club> updateClub(@PathVariable Integer id, @RequestBody Club club) {
        try {
            Club updatedClub = clubService.updateClub(id, club);
            return Result.success(updatedClub);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteClub(@PathVariable Integer id) {
        try {
            clubService.deleteClub(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateClubStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Club.ClubStatus clubStatus = Club.ClubStatus.valueOf(status);
            clubService.updateClubStatus(id, clubStatus);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/apply")
    public Result<Void> applyToClub(@RequestBody Map<String, Object> applyForm) {
        try {
            Integer clubId = applyForm.get("clubId") != null ? Integer.valueOf(applyForm.get("clubId").toString()) : null;
            Integer creatorId = applyForm.get("creatorId") != null ? Integer.valueOf(applyForm.get("creatorId").toString()) : null;
            String applicant = (String) applyForm.get("applicant");
            String reason = (String) applyForm.get("reason");
            clubService.handleClubApplication(clubId, creatorId, applicant, reason);
            System.out.println("applyToClub 返回 Result.success()");
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/apply")
    public Result<Void> applyToClub(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            String applicant = (String) request.get("applicant");
            String reason = (String) request.get("reason");
            
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            boolean success = clubMemberService.applyToClub(userId, id, applicant, reason);
            if (success) {
                return Result.success();
            } else {
                return Result.error("申请失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 获取申请状态
    @GetMapping("/{id}/application-status")
    public Result<String> getApplicationStatus(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            ClubMember.JoinStatus status = clubMemberService.getApplicationStatus(userId, id);
            return Result.success(status != null ? status.name() : "未申请");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 获取用户加入的社团
    @GetMapping("/joined")
    public Result<List<Club>> getUserJoinedClubs(@RequestParam Integer userId) {
        try {
            List<Club> clubs = clubMemberService.getUserJoinedClubs(userId);
            return Result.success(clubs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 获取申请信息
    @GetMapping("/applications")
    public Result<Map<String, List<ApplicationVO>>> getApplications(@RequestParam Integer creatorId) {
        try {
            Map<String, List<ApplicationVO>> applications = clubMemberService.getApplicationsByCreator(creatorId);
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 处理申请
    @PutMapping("/applications/{id}/{action}")
    public Result<Void> processApplication(@PathVariable Integer id, @PathVariable String action, @RequestBody Map<String, Object> request) {
        try {
            Integer creatorId = (Integer) request.get("creatorId");
            if (creatorId == null) {
                return Result.error("创建者ID不能为空");
            }
            
            boolean success = clubMemberService.processApplication(id, creatorId, action);
            if (success) {
                return Result.success();
            } else {
                return Result.error("处理失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 任命成员角色
    @PutMapping("/{clubId}/members/{memberId}/role")
    public Result<Void> appointMemberRole(@PathVariable Integer clubId, @PathVariable Integer memberId, @RequestBody Map<String, Object> request) {
        try {
            Integer creatorId = (Integer) request.get("creatorId");
            String role = (String) request.get("role");
            
            if (creatorId == null) {
                return Result.error("创建者ID不能为空");
            }
            if (role == null) {
                return Result.error("角色不能为空");
            }
            
            boolean success = clubMemberService.appointMemberRole(clubId, memberId, creatorId, role);
            if (success) {
                return Result.success();
            } else {
                return Result.error("任命失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 获取社团成员列表（用于管理）
    @GetMapping("/{clubId}/members")
    public Result<List<ClubDetailVO.ClubMemberVO>> getClubMembers(@PathVariable Integer clubId, @RequestParam Integer creatorId) {
        try {
            List<ClubDetailVO.ClubMemberVO> members = clubMemberService.getClubMembersForManagement(clubId, creatorId);
            return Result.success(members);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // 收藏相关API
    @PostMapping("/{id}/favorite")
    public Result<Void> addToFavorites(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            boolean success = clubMemberService.addToFavorites(userId, id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("收藏失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}/favorite")
    public Result<Void> removeFromFavorites(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            boolean success = clubMemberService.removeFromFavorites(userId, id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/favorite")
    public Result<Boolean> isFavorited(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            boolean isFavorited = clubMemberService.isFavorited(userId, id);
            return Result.success(isFavorited);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/favorites")
    public Result<List<Club>> getUserFavorites(@RequestParam Integer userId) {
        try {
            List<Club> favorites = clubMemberService.getUserFavorites(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/page")
    public Result<Page<Club>> getClubsPage(
            @RequestParam(defaultValue = "0") int page,  //默认值改成 0
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Club> clubsPage = clubService.getClubsPage(page, pageSize, keyword);
            return Result.success(clubsPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/hot")
  public Result<List<Club>> getHotClubs() {
    try {
        List<Club> hotClubs = clubService.getHotClubs();
        return Result.success(hotClubs);
    } catch (Exception e) {
        return Result.error("获取热门社团失败：" + e.getMessage());
    }
}

    // 后台管理接口
    @PutMapping("/admin/{id}/audit")
    public Result<Void> auditClub(@PathVariable Integer id, @RequestParam String action) {
        try {
            if (!action.equalsIgnoreCase("approve") && !action.equalsIgnoreCase("reject")) {
                return Result.error("非法操作参数");
            }
            Club.ClubStatus status = action.equalsIgnoreCase("approve") ?
                    Club.ClubStatus.正常 : Club.ClubStatus.已封禁;
            clubService.updateClubStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }


} 