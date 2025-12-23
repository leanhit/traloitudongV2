package com.chatbot.userInfo.controller;

import com.chatbot.userInfo.dto.UserInfoRequest;
import com.chatbot.userInfo.dto.UserInfoResponse;
import com.chatbot.userInfo.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/me/{userId}")
    public ResponseEntity<UserInfoResponse> getMyProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userInfoService.getProfile(userId));
    }

    @PutMapping("/me/{userId}")
    public ResponseEntity<UserInfoResponse> updateProfile(
            @PathVariable Long userId, 
            @RequestBody UserInfoRequest request) {
        return ResponseEntity.ok(userInfoService.updateProfile(userId, request));
    }
}