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
    private static final long EXPIRATION_TIME =365L* 24 * 60 * 60 * 1000;

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
                return null;
            }

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 存储token和用户映射
     */
    public void storeToken(String token, String username) {
        tokenUserMap.put(token, username);
    }

    /**
     * 从存储中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return tokenUserMap.get(token);
    }

    /**
     * 删除token
     */
    public void removeToken(String token) {
        tokenUserMap.remove(token);
    }

    /**
     * 检查token是否存在
     */
    public boolean isTokenValid(String token) {
        String username = validateTokenAndGetUsername(token);
        if (username == null) {
            return false;
        }

        // 检查token是否在存储中
        return tokenUserMap.containsKey(token);
    }

    /**
 * 直接从 JWT 中解析出用户名（不依赖 tokenUserMap）
 */
public String parseUsernameFromJwt(String token) {
    try {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // 返回用户名
    } catch (Exception e) {
        System.out.println("❌ 解析 JWT 失败: " + e.getMessage());
        return null;
    }
}

}
