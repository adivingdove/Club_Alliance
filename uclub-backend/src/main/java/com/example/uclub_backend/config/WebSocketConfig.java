package com.example.uclub_backend.config;
import org.springframework.messaging.simp.config.ChannelRegistration;
import com.example.uclub_backend.handler.CustomPrincipalHandshakeHandler;
import com.example.uclub_backend.TokenManager;
import com.example.uclub_backend.interceptor.TokenChannelInterceptor;
import com.example.uclub_backend.interceptor.WebSocketHandshakeInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.lang.NonNull;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        @Autowired
    private TokenManager tokenManager;

    // 配置客户端连接入口
@Override
public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat")
            .setHandshakeHandler(new CustomPrincipalHandshakeHandler())  //  设置 Principal
            .addInterceptors(new WebSocketHandshakeInterceptor(tokenManager))
            .setAllowedOriginPatterns("*")
            .withSockJS();
}



    // 配置消息代理（订阅/发布前缀）
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    // 注册自定义拦截器处理 CONNECT 请求中的 token
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new TokenChannelInterceptor(tokenManager));
    }
}
