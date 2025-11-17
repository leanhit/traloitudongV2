// dto/BotpressRegisterResponse.java
package com.chatbot.botpress.dto.auth;

import lombok.Data;

@Data
public class BotpressRegisterResponse {
    private String strategy;
    private String email;
    private boolean isSuperAdmin;
}
