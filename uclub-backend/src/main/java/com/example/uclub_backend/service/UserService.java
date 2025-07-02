package com.example.uclub_backend.service;

import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.mapper.UserMapper;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.uclub_backend.*;
import jakarta.persistence.criteria.Predicate;


import java.util.*;

@Service
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private EmailService mailService;
    private  String storeCode="";
    private  String storeCode_="";

    @Autowired
    private UserMapper userMapper;

    // 用户登录
    public Map<String, Object> login(Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (username == null || password == null) {
                response.put("code", 400);
                response.put("message", "用户名和密码不能为空");
                response.put("data", null);
                return response;
            }

            String sql = "SELECT * FROM user_login WHERE Account = ?";
            Map<String, Object> userData = jdbcTemplate.queryForMap(sql, username);

            if (userData == null) {
                response.put("code", 400);
                response.put("message", "用户不存在");
                response.put("data", null);
                return response;
            }

            if (!password.equals(userData.get("Password"))) {
                response.put("code", 400);
                response.put("message", "密码错误");
                response.put("data", null);
                return response;
            }

            // 生成JWT token
            String token = tokenManager.generateToken(username);

            // 存储token和用户映射
            tokenManager.storeToken(token, username);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("ID", userData.get("ID"));
            userInfo.put("Name", userData.get("Name"));
            userInfo.put("username", userData.get("Account"));
            userInfo.put("email", userData.get("email"));
            userInfo.put("Class", userData.get("Class"));
            userInfo.put("avatar", userData.get("Head"));
            userInfo.put("status", userData.get("Status"));

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", userInfo);

            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", data);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "登录失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

    // 用户注册
    public Map<String, Object> register(Map<String, String> registerData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = registerData.get("username");
            String email = registerData.get("email");
            String password = registerData.get("password");
            String emailCode = registerData.get("emailCode");

            if (username == null || email == null || password == null || emailCode == null) {
                response.put("code", 400);
                response.put("message", "所有字段都不能为空");
                response.put("data", null);
                return response;
            }

            String sql_ = "SELECT * FROM user_login WHERE Account = ?";
            Map<String, Object> user;

            user=jdbcTemplate.queryForMap(sql_, email);
            if (user != null) {
                response.put("code", 400);
                response.put("message", "用户名已存在");
                response.put("data", null);
                return response;
            }

            if (storeCode == null || !storeCode.equals(emailCode)) {
                response.put("code", 400);
                response.put("message", "验证码错误或已过期");
                response.put("data", null);
                return response;
            }

            // 创建新用户
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("Account", username);
            newUser.put("Password", password);
            newUser.put("Name","用户"+username);
            newUser.put("Email",email);
            newUser.put("Class",0);
            newUser.put("Head","https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            newUser.put("Status","正常");

            String sql = "INSERT INTO user_login ( Name,Account, Password, Class,email,Head) VALUES (?, ?, ?, ?,?,?)";

            jdbcTemplate.update(sql,newUser.get("Name"),
                    newUser.get("Account"),
                    newUser.get("Password"),
                    newUser.get("Class"),
                    newUser.get("Email"),
                    newUser.get("Head"),
                    newUser.get("Status")
            );

            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", null);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "注册失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

    // 发送注册验证码
    public Map<String, Object> sendRegisterCode(Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = data.get("email");

            if (email == null || email.trim().isEmpty()) {
                response.put("code", 400);
                response.put("message", "邮箱不能为空");
                response.put("data", null);
                return response;
            }

            // 检查邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("code", 400);
                response.put("message", "邮箱格式不正确");
                response.put("data", null);
                return response;
            }

            Random random=new Random();
            int number = random.nextInt(1000000);
            String code = String.format("%06d", number);
            storeCode=code;

            mailService.sendSimpleMail(email, "验证码", "您的验证码是：" + code);

            response.put("code", 200);
            response.put("message", "验证码已发送到您的邮箱");
            response.put("data", null);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "发送验证码失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

    // 发送重置密码验证码
    public Map<String, Object> sendResetCode(Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "请通过 Controller 发送验证码");
        response.put("data", null);
        return response;
    }

    // 重置密码
    public Map<String, Object> resetPassword(Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "请通过 Controller 重置密码");
        response.put("data", null);
        return response;
    }


    @Autowired
    private UserRepository userRepository;

    public void updateUserStatus(Integer id, String newStatus) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String statusStr = newStatus;
            User.UserStatus status = user.getStatus();
            user.setStatus(status);

            userRepository.save(user);
            System.out.println("已经修改");
        } else {
            throw new RuntimeException("用户未找到");
        }
    }

    public boolean verifyUserPasswordAndRoleAndStatus(String email, String password, String requiredRole, String requiredStatus) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        //三重校验
        return user.getPassword().equals(password)
                && requiredRole.equals(user.getRole())
                && requiredStatus.equals(user.getStatus());
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    public String getUserNameById(Integer userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getNickname() : "未知用户";
    }

    public Page<User> getUsersByQuery(String email, String nickname, User.UserRole role, User.UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(cb.like(root.get("email"), "%" + email + "%"));
            }
            if (nickname != null && !nickname.trim().isEmpty()) {
                predicates.add(cb.like(root.get("nickname"), "%" + nickname + "%"));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }


    // 批量查询昵称
    public Map<Integer,String> getUserNamesByIds(List<Integer> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        Map<Integer,String> nicknames = new HashMap<>();
        for (User user : users) {
            nicknames.put(user.getId(), user.getNickname());
        }
        return nicknames;
    }

    
}