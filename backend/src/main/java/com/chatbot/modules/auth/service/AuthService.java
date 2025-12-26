package com.chatbot.modules.auth.service;

import com.chatbot.modules.address.service.AddressService;
import com.chatbot.modules.auth.dto.*;
import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.auth.model.SystemRole;
import com.chatbot.modules.auth.repository.AuthRepository;
import com.chatbot.modules.auth.security.CustomUserDetails;
import com.chatbot.modules.userInfo.model.UserInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressService addressService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse register(RegisterRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        boolean isFirstUser = authRepository.count() == 0;

        Auth user = Auth.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .systemRole(isFirstUser ? SystemRole.ADMIN : SystemRole.USER)
                .build();

        // tạo UserInfo đúng chuẩn MapsId
        UserInfo userInfo = new UserInfo();
        userInfo.setAuth(user);
        user.setUserInfo(userInfo);

        // CHỈ save Auth – UserInfo sẽ được cascade
        Auth savedUser = authRepository.save(user);

        // tạo address (không ảnh hưởng transaction chính)
        try {
            addressService.createEmptyAddressForUser(savedUser.getId());
        } catch (Exception e) {
            log.error("Không thể tạo địa chỉ trống cho user {}: {}", savedUser.getId(), e.getMessage());
        }

        String token = jwtService.generateToken(savedUser.getEmail());

        UserDto userDto = new UserDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getSystemRole().name()
        );

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
