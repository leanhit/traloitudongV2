package com.chatbot.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/api/auth/register", "/api/auth/login", "/error")
                // 💥 FIX LỖI: Bỏ qua kiểm tra bảo mật cho endpoint WebSocket
                .requestMatchers("/ws/takeover","/ws/takeover/**"); 
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://truyenthongviet.vn"); // Thêm URL frontend của bạn
        config.addAllowedMethod("*"); // Cho phép tất cả các phương thức
        config.addAllowedHeader("*"); // Cho phép tất cả các header
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter, AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/webhooks/facebook/botpress/**").permitAll() 
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // --- CÁC API CẤP MASTER (CẦN AUTHENTICATED) ---
                // Yêu cầu đăng nhập để biết ai là chủ sở hữu
                .requestMatchers(HttpMethod.POST, "/api/tenant/create").authenticated() 
                
                // Lấy danh sách tenants của người dùng hiện tại (Cấp Master)
                .requestMatchers(HttpMethod.GET, "/api/tenant").authenticated()      
                
                // Loại bỏ hoặc làm rõ mục này nếu nó trùng với /api/tenant/create
                .requestMatchers(HttpMethod.POST, "/api/tenant").authenticated()      
                
                // --- CÁC API CẤP TENANT (CẦN AUTHENTICATED VÀ X-Tenant-ID) ---
                .requestMatchers("/api/tenant/**").authenticated() // GET, PUT, DELETE chi tiết tenant
                
                // Mọi API khác (được cho là truy cập dữ liệu bên trong Tenant)
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
