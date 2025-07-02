package com.example.uclub_backend.config;

import com.example.uclub_backend.entity.User;
import com.example.uclub_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@system.com";
        String adminAccount = "admin";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setAccount(adminAccount);
            admin.setPassword(passwordEncoder.encode("admin123")); // 初始密码
            admin.setRole(User.UserRole.系统管理员); // 枚举类型
            admin.setNickname("系统管理员");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            System.out.println("✅ 已自动创建系统管理员账号：admin / admin123");
        }
    }
}
