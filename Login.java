package org.example.springbootdemo;

import org.apache.tomcat.util.threads.ScheduledThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@RestController
@RequestMapping("/api/user")
public class Login {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private EmailTest mailService;
    private  String storeCode="";
    private  String storeCode_="";
    // 用户登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
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
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
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
    @PostMapping("/sendRegisterCode")
    public Map<String, Object> sendRegisterCode(@RequestBody Map<String, String> data) {
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
    @PostMapping("/sendResetCode")
    public Map<String, Object> sendResetCode(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = data.get("username");
            String email = data.get("email");

            if (username == null || email == null) {
                response.put("code", 400);
                response.put("message", "用户名和邮箱不能为空");
                response.put("data", null);
                return response;
            }

            String sql = "SELECT * FROM user_login WHERE Account = ?";
            Map<String, Object> userData;

            try {
                userData = jdbcTemplate.queryForMap(sql, username);
            } catch (EmptyResultDataAccessException e) {
                // 没查到数据
                response.put("code", 400);
                response.put("message", "用户不存在");
                response.put("data", null);
                return response;
            }

            if (!email.equals(userData.get("email"))) {
                response.put("code", 400);
                response.put("message", "邮箱与用户名不匹配");
                response.put("data", null);
                return response;
            }

            Random random=new Random();
            int number = random.nextInt(1000000);
            String code = String.format("%06d", number);
            storeCode_=code;

            mailService.sendSimpleMail(email, "验证码", "您的验证码是：" + code);

            response.put("code", 200);
            response.put("message", "重置密码验证码已发送到您的邮箱");
            response.put("data", null);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "发送验证码失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

    // 重置密码
    @PostMapping("/resetPassword")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = data.get("username");
            String email = data.get("email");
            String verificationCode = data.get("verificationCode");
            String newPassword = data.get("newPassword");

            if (username == null || email == null || verificationCode == null || newPassword == null) {
                response.put("code", 400);
                response.put("message", "所有字段都不能为空");
                response.put("data", null);
                return response;
            }

            // 检查用户是否存在
            String sql_ = "SELECT * FROM user_login WHERE Account = ?";
            Map<String, Object> user;
            try {
                user = jdbcTemplate.queryForMap(sql_, username);
            } catch (EmptyResultDataAccessException e) {
                // 没查到数据
                response.put("code", 400);
                response.put("message", "用户不存在");
                response.put("data", null);
                return response;
            }

            // 验证邮箱
            if (!email.equals(user.get("email"))) {
                response.put("code", 400);
                response.put("message", "邮箱与用户名不匹配");
                response.put("data", null);
                return response;
            }

            if (storeCode_ == null || !storeCode_.equals(verificationCode)) {
                response.put("code", 400);
                response.put("message", "验证码错误或已过期");
                response.put("data", null);
                return response;
            }

            // 更新密码
            user.put("password", newPassword);

            String sql = "UPDATE user_login SET Password = ? WHERE Account = '"+username+"'";

            jdbcTemplate.update(sql,
                    user.get("Password")
            );

            response.put("code", 200);
            response.put("message", "密码重置成功");
            response.put("data", null);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "密码重置失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

    // 获取用户信息
    /*@GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                response.put("data", null);
                return response;
            }

            String token = authHeader.substring(7); // 去掉"Bearer "前缀

            // 验证token
            if (!tokenManager.isTokenValid(token)) {
                response.put("code", 401);
                response.put("message", "token无效或已过期");
                response.put("data", null);
                return response;
            }

            // 从token获取用户名
            String username = tokenManager.getUsernameFromToken(token);

            // 从数据库获取用户信息
            String sql = "SELECT * FROM user_login WHERE Account = ?";
            Map<String, Object> userData = jdbcTemplate.queryForMap(sql, username);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("ID", userData.get("ID"));
            userInfo.put("Name", userData.get("Name"));
            userInfo.put("username", userData.get("Account"));
            userInfo.put("email", userData.get("email"));
            userInfo.put("Class", userData.get("Class"));
            userInfo.put("avatar", userData.get("Head"));
            userInfo.put("registerTime", "2024-01-01 00:00:00");
            userInfo.put("status",userData.get("Status"));// 可以从数据库获取

            response.put("code", 200);
            response.put("message", "获取用户信息成功");
            response.put("data", userInfo);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }*/


    // 退出登录
    /*@PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("code", 401);
                response.put("message", "未授权访问");
                response.put("data", null);
                return response;
            }

            String token = authHeader.substring(7);

            // 验证token
            if (!tokenManager.isTokenValid(token)) {
                response.put("code", 401);
                response.put("message", "token无效或已过期");
                response.put("data", null);
                return response;
            }

            // 删除token
            tokenManager.removeToken(token);

            response.put("code", 200);
            response.put("message", "退出登录成功");
            response.put("data", null);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "退出登录失败: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }*/
}
