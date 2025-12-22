package com.chatbot.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Ánh xạ cho REST API (/api/**)
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*") 
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);
                        
                // 💥 Ánh xạ cho WebSocket Handshake (/ws/takeover) và các tài nguyên gốc
                // WebSocket handshake sử dụng HTTP GET/OPTIONS ban đầu, nên cần CORS.
                registry.addMapping("/**")// Bao gồm /ws/takeover và các đường dẫn khác
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "OPTIONS") // WS Handshake chủ yếu dùng GET
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}