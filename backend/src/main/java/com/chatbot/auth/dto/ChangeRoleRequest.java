package com.chatbot.auth.dto;

import com.chatbot.auth.model.SystemRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {
    private Long userId;
    private SystemRole newRole;
}