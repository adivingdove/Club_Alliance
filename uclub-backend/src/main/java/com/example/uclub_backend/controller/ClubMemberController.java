package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.Club;
import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.service.ClubMemberService;
import com.example.uclub_backend.service.ClubService;
import com.example.uclub_backend.service.UserService;
import com.example.uclub_backend.vo.ClubAdminVO;
import com.example.uclub_backend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/club-members")
public class ClubMemberController {

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClubService clubService;
    
    // 修改身份
    @PutMapping("/{memberId}/change-role")
    public Result<?> changeMemberRole(
        @PathVariable Integer memberId,
        @RequestParam String newRole 
    ) {
        try {
            clubMemberService.changeMemberRole(memberId, newRole);
            return Result.success("角色已更改为：" + newRole);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, "无效的角色名称");
        } catch (Exception e) {
            return Result.fail(500, e.getMessage());
        }
    }

    @GetMapping("/admins")
    public Result<List<ClubAdminVO>> getAdmins() {
        List<ClubMember> admins = clubMemberService.getAdmins();

        List<ClubAdminVO> result = admins.stream().map(admin -> {
            ClubAdminVO vo = new ClubAdminVO();
            vo.setId(admin.getId());
            vo.setUserId(admin.getUserId());
            vo.setUserName(userService.getUserNameById(admin.getUserId()));
            vo.setClubId(admin.getClubId());
            vo.setClubName(clubService.getClubNameById(Long.valueOf(admin.getClubId())));
            vo.setRole(String.valueOf(admin.getRole()));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }


}
