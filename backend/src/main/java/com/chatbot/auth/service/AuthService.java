package com.chatbot.auth.service;

import com.chatbot.auth.dto.LoginRequest;
import com.chatbot.auth.dto.RegisterRequest;
import com.chatbot.auth.dto.UserDto;
import com.chatbot.auth.dto.UserResponse;
import com.chatbot.auth.model.Auth;
import com.chatbot.auth.model.Role; // <-- Import chính xác
import com.chatbot.auth.repository.AuthRepository;
import com.chatbot.auth.security.CustomUserDetails;

import com.chatbot.botpress.service.BotpressAuthApiService;
import com.chatbot.botpress.dto.auth.BotpressRegisterResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BotpressAuthApiService botpressAuthApiService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(user);
    }

    public UserResponse register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Auth user = Auth.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // <-- Sử dụng đúng Role enum
                .build();

        authRepository.save(user);
        
        // Đồng bộ user sang Botpress
        try {
            BotpressRegisterResponse bpRes = botpressAuthApiService.registerUser(request.getEmail(), request.getPassword());
            if (bpRes == null || bpRes.getEmail() == null) {
                log.warn("⚠️ Lỗi đăng ký user trên Botpress");
            } else {
                log.info("✅ User đã sync sang Botpress: {}", bpRes.getEmail());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Exception khi gọi Botpress: " + e.getMessage());
        }

        // Sinh token trả về cho middleware
        String token = jwtService.generateToken(user.getEmail());
        
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole().name());
        return new UserResponse(token, userDto);
    }

    public UserResponse login(LoginRequest request) {
        Auth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getRole().name());
        return new UserResponse(token, userDto);
    }
}
