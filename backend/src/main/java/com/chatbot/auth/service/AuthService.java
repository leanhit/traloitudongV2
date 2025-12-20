package com.chatbot.auth.service;

import com.chatbot.auth.dto.*;
import com.chatbot.auth.model.Auth;
import com.chatbot.auth.model.SystemRole; // <-- Import chính xác
import com.chatbot.auth.repository.AuthRepository;
import com.chatbot.auth.security.CustomUserDetails;

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
                .systemRole(SystemRole.USER) // Using the correct field name with lowercase 's'
                .build();

        authRepository.save(user);

        // Sinh token trả về cho middleware
        String token = jwtService.generateToken(user.getEmail());
        
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        return new UserResponse(token, userDto);
    }

    public UserResponse login(LoginRequest request) {
        Auth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        return new UserResponse(token, userDto);
    }

    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        // --- TẠO LẠI TOKEN MỚI VÀ TRẢ VỀ GIỐNG LOGIN ---
        String newToken = jwtService.generateToken(user.getEmail());
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
        
        return new UserResponse(newToken, userDto);
    }

    public UserDto changeRole(Long userId, SystemRole newRole) {
        // 1. Tìm người dùng cần đổi role
        Auth user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // 2. Cập nhật role mới
        user.setSystemRole(newRole);
        authRepository.save(user);

        log.info("Admin đã thay đổi quyền của user {} thành {}", user.getEmail(), newRole);

        // 3. Trả về thông tin user sau khi cập nhật
        return new UserDto(user.getId(), user.getEmail(), user.getSystemRole().name());
    }
}
