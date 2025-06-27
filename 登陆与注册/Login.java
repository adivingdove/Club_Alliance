package org.example.springbootdemo;

import org.apache.tomcat.util.threads.ScheduledThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
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

    // 验证码存储：Map<邮箱, Map<类型, Map<验证码信息>>>
    private final Map<String, Map<String, Map<String, Object>>> verificationCodes = new HashMap<>();

    // 验证码配置
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int MAX_SEND_COUNT = 5;

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

            String sql = "SELECT * FROM user WHERE account = ?";
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
            userInfo.put("ID", userData.get("id"));
            userInfo.put("Name", userData.get("nickname"));
            userInfo.put("username", userData.get("account"));
            userInfo.put("email", userData.get("email"));
            userInfo.put("Class", userData.get("role"));
            userInfo.put("avatar", userData.get("headUrl"));
            userInfo.put("status", userData.get("status"));
            userInfo.put("created_at",userData.get("created_at"));

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

            // 验证邮箱格式
            if (!isValidEmail(email)) {
                response.put("code", 400);
                response.put("message", "邮箱格式不正确");
                response.put("data", null);
                return response;
            }

            // 检查用户名是否已存在
            String checkUsernameSql = "SELECT COUNT(*) FROM user WHERE account = ?";
            int usernameCount = jdbcTemplate.queryForObject(checkUsernameSql, Integer.class, username);
            if (usernameCount > 0) {
                response.put("code", 400);
                response.put("message", "用户名已存在");
                response.put("data", null);
                return response;
            }

            // 检查邮箱是否已存在
            String checkEmailSql = "SELECT COUNT(*) FROM user WHERE email = ?";
            int emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, email);
            if (emailCount > 0) {
                response.put("code", 400);
                response.put("message", "邮箱已被注册");
                response.put("data", null);
                return response;
            }

            // 验证验证码
            if (!verifyCode(email, "register", emailCode)) {
                response.put("code", 400);
                response.put("message", "验证码错误或已过期");
                response.put("data", null);
                return response;
            }

            // 创建新用户
            String sql = "INSERT INTO user (nickname, account, password, role, email, headUrl, status,created_at) VALUES (?, ?, ?, ?, ?, ?, ?,?)";

            LocalDateTime date = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(date);
            jdbcTemplate.update(sql,
                    "用户" + username,  // Name
                    username,           // Account
                    password,           // Password
                    "普通用户",                  // Class
                    email,              // Email
                    "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png", // Head
                    "正常",   // Status
                     timestamp
            );

            // 注册成功后清除验证码
            clearCode(email, "register");

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

            // 验证邮箱格式
            if (!isValidEmail(email)) {
                response.put("code", 400);
                response.put("message", "邮箱格式不正确");
                response.put("data", null);
                return response;
            }

            // 检查邮箱是否已被注册
            String checkEmailSql = "SELECT COUNT(*) FROM user WHERE email = ?";
            int emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, email);
            if (emailCount > 0) {
                response.put("code", 400);
                response.put("message", "该邮箱已被注册");
                response.put("data", null);
                return response;
            }

            // 检查是否可以发送验证码
            String checkResult = canSendCode(email, "register");
            if (checkResult != null) {
                response.put("code", 400);
                response.put("message", checkResult);
                response.put("data", null);
                return response;
            }

            // 生成验证码
            String code = generateCode();
            storeCode(email, "register", code);

            // 发送邮件
            String subject = "注册验证码";
            String content = String.format(
                    "您好！\n\n您的注册验证码是：%s\n\n验证码有效期为%d分钟，请尽快使用。\n\n如果这不是您的操作，请忽略此邮件。",
                    code, CODE_EXPIRE_MINUTES
            );

            mailService.sendSimpleMail(email, subject, content);

            response.put("code", 200);
            response.put("message", "验证码已发送到您的邮箱");
            response.put("data", Map.of("expireMinutes", CODE_EXPIRE_MINUTES));

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

            // 验证邮箱格式
            if (!isValidEmail(email)) {
                response.put("code", 400);
                response.put("message", "邮箱格式不正确");
                response.put("data", null);
                return response;
            }

            // 检查用户是否存在
            String sql = "SELECT * FROM user WHERE account = ?";
            Map<String, Object> userData;

            try {
                userData = jdbcTemplate.queryForMap(sql, username);
            } catch (EmptyResultDataAccessException e) {
                response.put("code", 400);
                response.put("message", "用户不存在");
                response.put("data", null);
                return response;
            }

            // 验证邮箱是否匹配
            if (!email.equals(userData.get("email"))) {
                response.put("code", 400);
                response.put("message", "邮箱与用户名不匹配");
                response.put("data", null);
                return response;
            }

            // 检查是否可以发送验证码
            String checkResult = canSendCode(email, "reset");
            if (checkResult != null) {
                response.put("code", 400);
                response.put("message", checkResult);
                response.put("data", null);
                return response;
            }

            // 生成验证码
            String code = generateCode();
            storeCode(email, "reset", code);

            // 发送邮件
            String subject = "重置密码验证码";
            String content = String.format(
                    "您好！\n\n您正在重置密码，验证码是：%s\n\n验证码有效期为%d分钟，请尽快使用。\n\n如果这不是您的操作，请立即修改密码。",
                    code, CODE_EXPIRE_MINUTES
            );

            mailService.sendSimpleMail(email, subject, content);

            response.put("code", 200);
            response.put("message", "重置密码验证码已发送到您的邮箱");
            response.put("data", Map.of("expireMinutes", CODE_EXPIRE_MINUTES));

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

            // 验证邮箱格式
            if (!isValidEmail(email)) {
                response.put("code", 400);
                response.put("message", "邮箱格式不正确");
                response.put("data", null);
                return response;
            }

            // 检查用户是否存在
            String sql_ = "SELECT * FROM user WHERE account = ?";
            Map<String, Object> user;
            try {
                user = jdbcTemplate.queryForMap(sql_, username);
            } catch (EmptyResultDataAccessException e) {
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

            // 验证验证码
            if (!verifyCode(email, "reset", verificationCode)) {
                response.put("code", 400);
                response.put("message", "验证码错误或已过期");
                response.put("data", null);
                return response;
            }

            // 更新密码
            String updateSql = "UPDATE user SET password = ? WHERE account = ?";
            jdbcTemplate.update(updateSql, newPassword, username);

            // 重置成功后清除验证码
            clearCode(email, "reset");

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

    // 生成验证码
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 检查是否可以发送验证码
    private String canSendCode(String email, String codeType) {
        Map<String, Map<String, Object>> emailCodes = verificationCodes.get(email);
        if (emailCodes == null) {
            return null; // 可以发送
        }

        Map<String, Object> existingCode = emailCodes.get(codeType);
        if (existingCode == null) {
            return null; // 可以发送
        }

        // 检查是否在发送间隔内
        LocalDateTime lastSendTime = (LocalDateTime) existingCode.get("lastSendTime");
        if (LocalDateTime.now().isBefore(lastSendTime.plusSeconds(SEND_INTERVAL_SECONDS))) {
            long remainingSeconds = SEND_INTERVAL_SECONDS -
                    java.time.Duration.between(lastSendTime, LocalDateTime.now()).getSeconds();
            return "请等待 " + remainingSeconds + " 秒后再发送验证码";
        }

        // 检查发送次数限制
        Integer sendCount = (Integer) existingCode.get("sendCount");
        if (sendCount >= MAX_SEND_COUNT) {
            return "验证码发送次数已达上限，请稍后再试";
        }

        return null; // 可以发送
    }

    // 存储验证码
    private void storeCode(String email, String codeType, String code) {
        Map<String, Object> codeInfo = new HashMap<>();
        codeInfo.put("code", code);
        codeInfo.put("createTime", LocalDateTime.now());
        codeInfo.put("expireTime", LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        codeInfo.put("sendCount", 1);
        codeInfo.put("lastSendTime", LocalDateTime.now());

        verificationCodes.computeIfAbsent(email, k -> new HashMap<>()).put(codeType, codeInfo);
    }

    // 验证验证码
    private boolean verifyCode(String email, String codeType, String inputCode) {
        Map<String, Map<String, Object>> emailCodes = verificationCodes.get(email);
        if (emailCodes == null) {
            return false;
        }

        Map<String, Object> codeInfo = emailCodes.get(codeType);
        if (codeInfo == null) {
            return false;
        }

        // 检查是否过期
        LocalDateTime expireTime = (LocalDateTime) codeInfo.get("expireTime");
        if (LocalDateTime.now().isAfter(expireTime)) {
            emailCodes.remove(codeType);
            return false;
        }

        // 验证码匹配
        String storedCode = (String) codeInfo.get("code");
        boolean isValid = storedCode.equals(inputCode);
        if (isValid) {
            emailCodes.remove(codeType); // 验证成功后删除验证码
        }

        return isValid;
    }

    // 清除验证码
    private void clearCode(String email, String codeType) {
        Map<String, Map<String, Object>> emailCodes = verificationCodes.get(email);
        if (emailCodes != null) {
            emailCodes.remove(codeType);
            if (emailCodes.isEmpty()) {
                verificationCodes.remove(email);
            }
        }
    }

    // 更新发送信息
    private void updateSendInfo(String email, String codeType) {
        Map<String, Map<String, Object>> emailCodes = verificationCodes.get(email);
        if (emailCodes != null) {
            Map<String, Object> codeInfo = emailCodes.get(codeType);
            if (codeInfo != null) {
                Integer sendCount = (Integer) codeInfo.get("sendCount");
                codeInfo.put("sendCount", sendCount + 1);
                codeInfo.put("lastSendTime", LocalDateTime.now());
            }
        }
    }
}
