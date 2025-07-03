package com.example.uclub_backend.config;

import com.example.uclub_backend.TokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        
        System.out.println("JWT Filter - Request URI: " + requestURI);
        System.out.println("JWT Filter - Authorization Header: " + (authHeader != null ? authHeader : "null"));
        System.out.println("JWT Filter - Request Method: " + request.getMethod());
        System.out.println("JWT Filter - All Headers: ");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("JWT Filter - Token: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            try {
                String username = tokenManager.validateTokenAndGetUsername(token);
                System.out.println("JWT Filter - Username from token: " + username);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("JWT Filter - UserDetails loaded: " + (userDetails != null));
                    System.out.println("JWT Filter - Authorities: " + (userDetails != null ? userDetails.getAuthorities() : "null"));
                    
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT Filter - Authentication set successfully");
                    }
                } else {
                    System.out.println("JWT Filter - Username is null or authentication already exists");
                    System.out.println("JWT Filter - Current Authentication: " + SecurityContextHolder.getContext().getAuthentication());
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Token validation failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("JWT Filter - No valid Authorization header found");
        }
        
        filterChain.doFilter(request, response);
    }
} 