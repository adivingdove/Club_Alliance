package com.example.uclub_backend.interceptor;

import com.example.uclub_backend.TokenManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.lang.NonNull;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenManager tokenManager;

    public WebSocketHandshakeInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {

        String token = null;
        // 1. 优先从 header 取
        var headers = request.getHeaders().get("token");
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        // 2. 如果 header 没有，从 query string 取
        if (token == null && request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            token = httpServletRequest.getParameter("token");
        }
        // 3. 兼容 SockJS fallback，手动解析 query string
        if (token == null && request.getURI().getQuery() != null) {
            for (String param : request.getURI().getQuery().split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }
        if (token != null) {
            String username = tokenManager.parseUsernameFromJwt(token); //  使用解析函数
            if (username != null) {
                attributes.put("user", username); // 设置 Principal 用户名
                return true;
            }
        }
        System.out.println(" 握手失败，未提供有效 token");
        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        // 无需处理
    }
}
