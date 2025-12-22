package com.chatbot.userInfo.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    // Có thể bao gồm danh sách Address ở đây nếu cần
}