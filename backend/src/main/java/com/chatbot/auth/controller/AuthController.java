// AuthController.java
package com.chatbot.auth.controller;

import com.chatbot.auth.dto.*;
import com.chatbot.auth.security.CustomUserDetails;
import com.chatbot.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<UserResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody ChangePasswordRequest request) {
        
        // Kiểm tra khớp mật khẩu mới ở tầng Controller để giảm tải cho Service
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp");
        }

        UserResponse response = authService.changePassword(currentUser.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')") // Chốt chặn bảo mật quan trọng nhất
    public ResponseEntity<UserDto> changeRole(@RequestBody ChangeRoleRequest request) {
        UserDto updatedUser = authService.changeRole(request.getUserId(), request.getNewRole());
        return ResponseEntity.ok(updatedUser);
    }
}
