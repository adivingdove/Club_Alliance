package org.example.springbootdemo;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class VerificationCodeManager {

    // 验证码存储结构：Map<邮箱, Map<验证码类型, 验证码信息>>
    private final ConcurrentHashMap<String, Map<String, VerificationCode>> verificationCodes = new ConcurrentHashMap<>();

    // 验证码类型
    public static final String REGISTER_CODE = "register";
    public static final String RESET_CODE = "reset";

    // 验证码配置
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int MAX_SEND_COUNT = 5;

    // 验证码信息类
    public static class VerificationCode {
        public String code;
        public LocalDateTime createTime;
        public LocalDateTime expireTime;
        public int sendCount;
        public LocalDateTime lastSendTime;

        public VerificationCode(String code) {
            this.code = code;
            this.createTime = LocalDateTime.now();
            this.expireTime = this.createTime.plusMinutes(CODE_EXPIRE_MINUTES);
            this.sendCount = 1;
            this.lastSendTime = this.createTime;
        }
    }

    // 生成验证码
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // 验证邮箱格式
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // 检查是否可以发送验证码
    public String canSendCode(String email, String codeType) {
        Map<String, VerificationCode> emailCodes = verificationCodes.get(email);
        if (emailCodes == null) {
            return null; // 可以发送
        }

        VerificationCode existingCode = emailCodes.get(codeType);
        if (existingCode == null) {
            return null; // 可以发送
        }

        // 检查是否在发送间隔内
        if (LocalDateTime.now().isBefore(existingCode.lastSendTime.plusSeconds(SEND_INTERVAL_SECONDS))) {
            long remainingSeconds = SEND_INTERVAL_SECONDS -
                    java.time.Duration.between(existingCode.lastSendTime, LocalDateTime.now()).getSeconds();
            return "请等待 " + remainingSeconds + " 秒后再发送验证码";
        }

        // 检查发送次数限制
        if (existingCode.sendCount >= MAX_SEND_COUNT) {
            return "验证码发送次数已达上限，请稍后再试";
        }

        return null; // 可以发送
    }

    // 存储验证码
    public void storeCode(String email, String codeType, String code) {
        VerificationCode verificationCode = new VerificationCode(code);
        verificationCodes.computeIfAbsent(email, k -> new ConcurrentHashMap<>()).put(codeType, verificationCode);
    }

    // 验证验证码
    public boolean verifyCode(String email, String codeType, String inputCode) {
        Map<String, VerificationCode> emailCodes = verificationCodes.get(email);
        if (emailCodes == null) {
            return false;
        }

        VerificationCode verificationCode = emailCodes.get(codeType);
        if (verificationCode == null) {
            return false;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(verificationCode.expireTime)) {
            emailCodes.remove(codeType);
            return false;
        }

        // 验证码匹配
        boolean isValid = verificationCode.code.equals(inputCode);
        if (isValid) {
            emailCodes.remove(codeType); // 验证成功后删除验证码
        }

        return isValid;
    }

    // 清除验证码
    public void clearCode(String email, String codeType) {
        Map<String, VerificationCode> emailCodes = verificationCodes.get(email);
        if (emailCodes != null) {
            emailCodes.remove(codeType);
            if (emailCodes.isEmpty()) {
                verificationCodes.remove(email);
            }
        }
    }

    // 更新发送次数和时间
    public void updateSendInfo(String email, String codeType) {
        Map<String, VerificationCode> emailCodes = verificationCodes.get(email);
        if (emailCodes != null) {
            VerificationCode verificationCode = emailCodes.get(codeType);
            if (verificationCode != null) {
                verificationCode.sendCount++;
                verificationCode.lastSendTime = LocalDateTime.now();
            }
        }
    }

    // 获取验证码过期时间（分钟）
    public int getCodeExpireMinutes() {
        return CODE_EXPIRE_MINUTES;
    }

    // 定时清理过期验证码
    @PostConstruct
    public void scheduleCodeCleanup() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::cleanupExpiredCodes, 1, 5, TimeUnit.MINUTES);
    }

    private void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> {
            Map<String, VerificationCode> emailCodes = entry.getValue();
            emailCodes.entrySet().removeIf(codeEntry ->
                    now.isAfter(codeEntry.getValue().expireTime)
            );
            return emailCodes.isEmpty();
        });
    }
}
