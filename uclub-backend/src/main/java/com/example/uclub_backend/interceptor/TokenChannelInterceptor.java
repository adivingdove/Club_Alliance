package com.example.uclub_backend.interceptor;

import com.example.uclub_backend.TokenManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.security.Principal;
import java.util.List;

public class TokenChannelInterceptor implements ChannelInterceptor {

    private final TokenManager tokenManager;

    public TokenChannelInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
        List<String> tokens = accessor.getNativeHeader("token");
        if (tokens != null && !tokens.isEmpty()) {
            String token = tokens.get(0);
            String username = tokenManager.parseUsernameFromJwt(token); //  使用解析函数
            if (username != null) {
                accessor.setUser(() -> username);
            }
        }
    }

    return message;
}

}
