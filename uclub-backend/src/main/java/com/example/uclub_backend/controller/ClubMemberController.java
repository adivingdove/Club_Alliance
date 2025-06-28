package com.example.uclub_backend.controller;

import com.example.uclub_backend.entity.ClubMember;
import com.example.uclub_backend.service.ClubMemberService;
import com.example.uclub_backend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/club-members")
public class ClubMemberController {

    @Autowired
    private ClubMemberService clubMemberService;

    @GetMapping("/admins")
    public Result<List<ClubMember>> searchAdmins(
            @RequestParam(required = false) String clubName) {
        List<ClubMember> admins;
        if (clubName == null || clubName.isEmpty()) {
            admins = clubMemberService.getAllAdmins();
        } else {
            admins = clubMemberService.getAdminsByClubName(clubName);
        }
        return Result.success(admins);
    }



    // 撤销管理员身份（角色改为成员）
    @PutMapping("/{memberId}/revoke")
    public Result<?> revokeAdmin(@PathVariable Integer memberId) {
        try {
            clubMemberService.revokeAdminRole(memberId);
            return Result.success("撤销管理员身份成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }


}
