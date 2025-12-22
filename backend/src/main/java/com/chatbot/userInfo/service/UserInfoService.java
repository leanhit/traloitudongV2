package com.chatbot.userInfo.service;

import com.chatbot.userInfo.model.UserInfo;
import com.chatbot.userInfo.dto.UserInfoRequest;
import com.chatbot.userInfo.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;

    public UserInfo getProfile(Long userId) {
        return userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    @Transactional
    public UserInfo updateProfile(Long userId, UserInfoRequest request) {
        UserInfo profile = getProfile(userId);
        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAvatar(request.getAvatar());
        profile.setBio(request.getBio());
        return userInfoRepository.save(profile);
    }
}