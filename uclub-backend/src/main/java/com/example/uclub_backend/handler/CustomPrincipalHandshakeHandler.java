package com.example.uclub_backend.handler;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import org.springframework.lang.NonNull;
public class CustomPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
                                      @NonNull WebSocketHandler wsHandler,
                                      @NonNull Map<String, Object> attributes) {
        String username = (String) attributes.get("user");
        if (username != null) {
            return () -> username;  // 返回一个自定义 Principal
        }
        return null;
    }
}