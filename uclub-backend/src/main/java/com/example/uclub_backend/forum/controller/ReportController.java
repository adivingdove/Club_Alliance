package com.example.uclub_backend.forum.controller;
import com.example.uclub_backend.forum.entity.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

import com.example.uclub_backend.forum.mapper.ReportMapper;
import com.example.uclub_backend.forum.vo.ReportVO;
import com.example.uclub_backend.service.UserService;
import com.example.uclub_backend.forum.service.*;

import org.springframework.beans.BeanUtils;


@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<?> report(@RequestBody Report report) {
        System.out.println(" 收到举报请求：" + report);
        try {
            // 强制设置为“待处理”
            report.setStatus(ReportStatus.待处理);

            // 插入数据库
            reportMapper.insertReport(report);

            return ResponseEntity.ok().body(Map.of("message", "举报成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "参数非法", "detail", e.getMessage()));
        } catch (Exception e) {
             e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("error", "举报失败", "detail", e.getMessage()));
        }
    }

    // 获取举报记录
    @GetMapping("/list")
    public ResponseEntity<?> getReportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,   // 可选：待处理、已处理等
            @RequestParam(required = false) String keyword   // 可选：举报内容关键字
    ) {
        try {
            int offset = (page - 1) * size;
            List<Report> reports = reportMapper.selectReports(status, keyword, offset, size);
            int total = reportMapper.countReports(status, keyword);
            return ResponseEntity.ok(Map.of("data", reports, "total", total));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "查询失败", "detail", e.getMessage()));
        }
    }

    // 处理举报
    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateReportStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        try {
            ReportStatus enumStatus = ReportStatus.valueOf(status);
            reportMapper.updateReportStatus(id, enumStatus);
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "状态更新失败", "detail", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "举报不存在"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", report.getId());
        result.put("reporter",userService.getUserById(report.getReporterId()));
        result.put("targetType", report.getTargetType());
        result.put("targetId", report.getTargetId());
        result.put("reason", report.getReason());
        result.put("status", report.getStatus());
        result.put("createdAt", report.getCreatedAt());

        // 如果是评论，获取其所属帖子ID
        if (report.getTargetType() == TargetType.评论 ) {
            try {
                Comment comment = commentService.getCommentById(Long.valueOf(report.getTargetId()));
                result.put("postId", comment.getPostId());
                result.put("reportedUser",userService.getUserById(Math.toIntExact(comment.getUserId())));
            } catch (RuntimeException e) {
                result.put("postId", null);
            }
        }

        // 如果是帖子，重新赋值reportedUser
        if(report.getTargetType() == TargetType.帖子){
            try{
                Post post = postService.getPostById(Long.valueOf(report.getTargetId()));
                result.put("reportedUser",userService.getUserById(Math.toIntExact(post.getUserId())));
            }catch(RuntimeException e){
                result.put("reportedUser",null);
            }
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/admin/list")
    public ResponseEntity<?> getAdminReportList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            int offset = (page - 1) * size;
            List<Report> reports = reportMapper.selectReports(status, keyword, offset, size);
            int total = reportMapper.countReports(status, keyword);

            List<ReportVO> voList = reports.stream().map(report -> {
                ReportVO vo = new ReportVO();
                BeanUtils.copyProperties(report, vo);

                vo.setReporterNickname(userService.getUserNameById(report.getReporterId()));

                Integer reportedUserId = getReportedUserIdByTarget(report.getTargetType(), report.getTargetId());
                vo.setReportedUserId(reportedUserId);

                if (reportedUserId != null) {
                    vo.setReportedUserNickname(userService.getUserNameById(reportedUserId));
                }

                // 如果举报对象是评论，补充 postId 字段
                vo.setPostId(report.getTargetType() == TargetType.评论
                        ? commentService.getCommentById(report.getTargetId().longValue()).getPostId().intValue()
                        : report.getTargetId());

                return vo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("data", voList, "total", total));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "查询失败", "detail", e.getMessage()));
        }
    }

    // 根据目标类型获取被举报人ID（可扩展）
    private Integer getReportedUserIdByTarget(TargetType type, Integer targetId) {
        if(type == null) return null; // 防止传入null导致NullPointerException
        switch (type) {
            case 帖子:
                return postService.getUserId(targetId);
            case 评论:
                return commentService.getUserId(targetId);
            case 用户:
                return targetId;
            case 活动:
                // 待实现活动举报逻辑
                return null;
            case 公告:
                // 待实现公告举报逻辑
                return null;
            default:
                // 可记录日志或抛出异常
                return null;
        }
    }
}

