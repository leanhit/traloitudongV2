// src/main/java/com/chatbot/connection/service/FacebookConnectionService.java

package com.chatbot.chatHub.facebook.connection.service;

import com.chatbot.chatHub.facebook.connection.dto.CreateFacebookConnectionRequest;
import com.chatbot.chatHub.facebook.connection.dto.FacebookConnectionResponse;
import com.chatbot.chatHub.facebook.connection.dto.UpdateFacebookConnectionRequest;
import com.chatbot.chatHub.facebook.connection.model.FacebookConnection;
import com.chatbot.chatHub.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.botmanagement.service.BotService;
import com.chatbot.chatHub.facebook.connection.exception.*;
// 🚀 THÊM IMPORT CHATWOOT SERVICE
import com.chatbot.chatwoot.service.ChatwootApiService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // Cần thiết để xử lý Map trả về từ Chatwoot API
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FacebookConnectionService {

    private final FacebookConnectionRepository connectionRepository;
    private final BotService botService;// 🚀 KHAI BÁO CHATWOOT SERVICE
    private final ChatwootApiService chatwootApiService;

    public FacebookConnectionService(
        FacebookConnectionRepository connectionRepository, 
        BotService botService,
        ChatwootApiService chatwootApiService) {
        this.connectionRepository = connectionRepository;
        this.botService = botService;
        this.chatwootApiService = chatwootApiService; // Gán Service mới
    }

    public String createConnection(String ownerId, CreateFacebookConnectionRequest request) {
        
        // 1. KIỂM TRA QUYỀN SỞ HỮU BOT (BOT ID có thuộc về OWNER ID không?)
        // TODO: Mở comment và thay thế bằng logic thực tế của bạn
        if (!botService.doesBotBelongToOwner(request.getBotId(), ownerId)) {
            // Đảm bảo bot được kết nối là bot của chính người dùng
            throw new AccessDeniedException("Bot ID " + request.getBotId() + " does not belong to owner " + ownerId + ".");
        }

        // 2. KIỂM TRA TÍNH DUY NHẤT CỦA FANPAGE
        // Chỉ cho phép một kết nối (isActive=true) cho mỗi Page ID.
        if (connectionRepository.findByPageIdAndIsActiveTrue(request.getPageId()).isPresent()) {
            throw new ResourceAlreadyExistsException(
                "Fanpage with ID " + request.getPageId() + " is already connected to an active bot. Please disconnect the existing bot first."
            );
        }

        // 3. TẠO KẾT NỐI MỚI (Nếu không có lỗi)
        FacebookConnection newConnection = new FacebookConnection();
        newConnection.setId(UUID.randomUUID());
        newConnection.setBotId(request.getBotId());
        newConnection.setBotName(request.getBotName());
        newConnection.setPageId(request.getPageId());
        newConnection.setFanpageUrl(request.getFanpageUrl());
        newConnection.setPageAccessToken(request.getPageAccessToken());
        newConnection.setOwnerId(ownerId);
        newConnection.setFbUserId(""); // Cần xem xét cách lấy fbUserId thực tế nếu cần
        newConnection.setCreatedAt(LocalDateTime.now());
        newConnection.setUpdatedAt(LocalDateTime.now());
        newConnection.setEnabled(request.isEnabled());
        newConnection.setActive(true); // Luôn active khi tạo thành công
        
        // =====================================================================
        // 🚀 BƯỚC MỚI: TẠO INBOX TRONG CHATWOOT VÀ LƯU DỮ LIỆU
        // =====================================================================
        try {
            System.out.println("🚀 [Chatwoot Setup] Bắt đầu tạo API Inbox cho Fanpage: " + request.getBotName());
            
            // Lấy tên Fanpage từ botName hoặc một trường có sẵn
            String inboxName = request.getBotName() + " (" + request.getPageId().substring(0, 4) + "...)"; 

            // Gọi hàm tạo Inbox
            Map<String, Object> chatwootData = chatwootApiService.createApiInbox(inboxName);

            // Lưu Chatwoot ID và Key vào Entity
            newConnection.setChatwootInboxId((Integer) chatwootData.get("inbox_id"));
            newConnection.setChatwootChannelKey((String) chatwootData.get("channel_key"));
            
            System.out.println("✅ [Chatwoot Setup] Tạo Inbox ID " + newConnection.getChatwootInboxId() + " thành công.");

        } catch (Exception e) {
            System.err.println("❌ [Chatwoot Setup] Lỗi khi tạo Chatwoot Inbox: " + e.getMessage());
            // Tùy chọn: Nếu Chatwoot là bắt buộc, bạn có thể throw exception ở đây.
            // Nếu không bắt buộc, bạn có thể log lỗi và tiếp tục, để connection.isChatwootConnected() = false.
            // Hiện tại, ta log lỗi và tiếp tục (dẫn đến newConnection.isChatwootConnected() == false)
        }
        // =====================================================================

        connectionRepository.save(newConnection);
        
        return newConnection.getId().toString();
    }

    public List<FacebookConnectionResponse> getConnectionsByOwnerId(String ownerId) {
        List<FacebookConnection> connections = connectionRepository.findByOwnerIdAndIsActiveTrue(ownerId);
        return connections.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<FacebookConnectionResponse> getConnectionsByOwnerId(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FacebookConnection> connectionsPage = connectionRepository.findByOwnerIdAndIsActiveTrue(ownerId, pageable);
        List<FacebookConnectionResponse> dtoList = connectionsPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    private FacebookConnectionResponse convertToDto(FacebookConnection connection) {
        FacebookConnectionResponse dto = new FacebookConnectionResponse();
        dto.setId(connection.getId());
        dto.setBotId(connection.getBotId());
        dto.setBotName(connection.getBotName());
        dto.setPageId(connection.getPageId());
        dto.setPageAccessToken(connection.getPageAccessToken());
        dto.setFanpageUrl(connection.getFanpageUrl());
        dto.setEnabled(connection.isEnabled());
        dto.setCreatedAt(connection.getCreatedAt());
        dto.setUpdatedAt(connection.getUpdatedAt());
        return dto;
    }

    public void updateConnection(UUID connectionId, String ownerId, UpdateFacebookConnectionRequest request) {
        FacebookConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found."));
        if (!connection.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Access denied.");
        }
        if (request.getBotName() != null) {
            connection.setBotName(request.getBotName());
        }
        if (request.getBotId() != null) {
            connection.setBotId(request.getBotId());
        }
        if (request.getPageAccessToken() != null) {
            connection.setPageAccessToken(request.getPageAccessToken());
        }
        if (request.getFanpageUrl() != null) {
            connection.setFanpageUrl(request.getFanpageUrl());
        }
        if (request.getPageId() != null) {
            connection.setPageId(request.getPageId());
        }
        if (request.getIsEnabled() != null) {
            connection.setEnabled(request.getIsEnabled());
        }
        connection.setUpdatedAt(LocalDateTime.now());
        connectionRepository.save(connection);
    }

    // Sửa đổi phương thức deleteConnection trong FacebookConnectionService.java
    public void deleteConnection(String id) {
        UUID connectionId = UUID.fromString(id);
        
        FacebookConnection connectionToDelete = connectionRepository.findById(connectionId)
            .orElseThrow(() -> new RuntimeException("Connection not found."));

        // 🚀 BƯỚC MỚI: XÓA INBOX KHỎI CHATWOOT
        if (connectionToDelete.isChatwootConnected()) {
            try {
                System.out.println("🗑️ [Chatwoot Cleanup] Xóa Inbox ID: " + connectionToDelete.getChatwootInboxId());
                chatwootApiService.deleteInbox(connectionToDelete.getChatwootInboxId());
                System.out.println("✅ [Chatwoot Cleanup] Xóa Inbox thành công.");
            } catch (Exception e) {
                // Log lỗi nhưng vẫn tiếp tục xóa trong DB của mình
                System.err.println("❌ [Chatwoot Cleanup] Lỗi khi xóa Chatwoot Inbox: " + e.getMessage());
            }
        }
        
        // TODO: BƯỚC CẦN THIẾT KHÁC: GỌI FACEBOOK API HỦY ĐĂNG KÝ WEBHOOK TẠI ĐÂY

        // Xóa khỏi Database của Middleware
        connectionRepository.deleteById(connectionId);
    }
}
