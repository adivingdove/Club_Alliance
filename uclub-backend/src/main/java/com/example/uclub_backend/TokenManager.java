package com.example.uclub_backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenManager {

    private static final String SECRET_KEY = "your-secret-key-here-make-it-long-and-secure";
    private static final long EXPIRATION_TIME = 365L * 24 * 60 * 60 * 1000;

    // 存储token和用户信息的映射（生产环境建议使用Redis）
    private static final Map<String, String> tokenUserMap = new ConcurrentHashMap<>();

    /**
     * 生成JWT token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * 验证token并获取用户名
     */
    public String validateTokenAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            // 检查token是否过期
            if (claims.getExpiration().before(new Date())) {
                System.out.println("Token已过期");
                return null;
            }

            String username = claims.getSubject();
            System.out.println("Token验证成功，用户名: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("Token验证失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 存储token和用户映射
     * 注意：这个方法在当前实现中不再需要存储映射，因为我们直接从JWT中获取用户信息
     */
    public void storeToken(String token, String username) {
        // 简单存储，实际上我们不再需要这个映射
        tokenUserMap.put(token, username);
        System.out.println("Token已存储，用户: " + username);
    }

    /**
     * 检查token是否有效
     */
    public boolean isTokenValid(String token) {
        return validateTokenAndGetUsername(token) != null;
    }

    /**
     * 直接从JWT中解析用户名（不验证过期时间）
     */
    public String parseUsernameFromJwt(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            System.out.println("从JWT解析用户名: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("解析JWT失败: " + e.getMessage());
            return null;
        }
    }
}
