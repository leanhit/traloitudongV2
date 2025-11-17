package com.chatbot.chatHub.facebook.autoConnect.service;

import com.chatbot.chatHub.facebook.connection.model.FacebookConnection;
import com.chatbot.chatHub.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.chatHub.facebook.webhook.service.FacebookApiGraphService;
import com.chatbot.chatHub.facebook.autoConnect.dto.AutoConnectResponse;
import com.chatbot.chatHub.facebook.autoConnect.dto.ConnectionError;
import com.chatbot.chatwoot.service.ChatwootApiService; // 🚀 IMPORT MỚI
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FbAutoConnectService {

    private final FacebookConnectionRepository connectionRepository;
    private final FacebookApiGraphService facebookApiGraphService;
    private final FbConnectionPersistenceService persistenceService; 
    private final ChatwootApiService chatwootApiService; // 🚀 FIELD MỚI

    private static class ConnectionToProcess {
        FacebookConnection connection;
        boolean needsWebhookSubscription;
        boolean needsWebhookUnsubscription;
        boolean needsChatwootInboxCreation; // 🚀 TRƯỜNG MỚI
        boolean needsChatwootInboxDeletion; // 🚀 TRƯỜNG MỚI

        public ConnectionToProcess(FacebookConnection connection, boolean needsSub, boolean needsUnsub, boolean needsChatwootCreate, boolean needsChatwootDelete) {
            this.connection = connection;
            this.needsWebhookSubscription = needsSub;
            this.needsWebhookUnsubscription = needsUnsub;
            this.needsChatwootInboxCreation = needsChatwootCreate;
            this.needsChatwootInboxDeletion = needsChatwootDelete;
        }
    }

    public FbAutoConnectService(FacebookConnectionRepository connectionRepository,
                                FacebookApiGraphService facebookApiGraphService,
                                FbConnectionPersistenceService persistenceService,
                                ChatwootApiService chatwootApiService) { // 🚀 THAM SỐ MỚI
        this.connectionRepository = connectionRepository;
        this.facebookApiGraphService = facebookApiGraphService;
        this.persistenceService = persistenceService;
        this.chatwootApiService = chatwootApiService; // 🚀 GÁN FIELD MỚI
    }

    /**
     * Tự động kết nối fanpage
     */
    public synchronized AutoConnectResponse autoConnect(String ownerId, String botId, String userAccessToken) {
        log.info("🔹 Bắt đầu auto connect fanpage cho ownerId={}", ownerId);

        List<String> connectedPages = new ArrayList<>();
        List<String> reactivatedPages = new ArrayList<>();
        List<String> inactivePages = new ArrayList<>();
        List<ConnectionError> errors = new ArrayList<>();
        List<ConnectionToProcess> webhookQueue = new ArrayList<>();

        String fbUserId = facebookApiGraphService.getUserIdFromToken(userAccessToken);

        // 1️⃣ Lấy danh sách page từ Facebook
        List<Map<String, Object>> fbPages = facebookApiGraphService.getUserPages(userAccessToken);
        if (fbPages == null || fbPages.isEmpty()) {
            log.warn("⚠️ Không có fanpage nào hoặc không lấy được danh sách page.");
            return new AutoConnectResponse(true, "Không có fanpage nào để kết nối.",
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Set<String> fbPageIds = fbPages.stream()
                .map(p -> (String) p.get("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2️⃣ Lấy connection hiện tại
        List<FacebookConnection> existingConnections = connectionRepository.findByOwnerId(ownerId);
        Map<String, FacebookConnection> existingMap = existingConnections.stream()
                .collect(Collectors.toMap(FacebookConnection::getPageId, c -> c));

        List<FacebookConnection> toSave = new ArrayList<>();

        // 3️⃣ Xử lý từng page
        for (Map<String, Object> page : fbPages) {
            String pageId = (String) page.get("id");
            String pageName = (String) page.get("name");
            String pageToken = (String) page.get("access_token");

            if (pageId == null || pageToken == null) {
                log.warn("⚠️ Bỏ qua page {} vì thiếu access_token hoặc id.", pageName);
                errors.add(new ConnectionError(pageName, "Trang không có access_token hoặc id"));
                continue;
            }

            FacebookConnection conn = existingMap.get(pageId);
            boolean isNew = (conn == null);
            boolean wasInactive = false;
            boolean needsChatwootCreate = false; // Mặc định là False

            if (isNew) {
                conn = new FacebookConnection();
                conn.setId(UUID.randomUUID());
                conn.setBotId(botId);
                conn.setOwnerId(ownerId);
                conn.setFbUserId(fbUserId);
                conn.setPageId(pageId);
                conn.setFanpageUrl("https://www.facebook.com/" + pageId);
                conn.setCreatedAt(LocalDateTime.now());
                
                // 🚀 Nếu là mới, cần tạo Chatwoot Inbox
                needsChatwootCreate = true;

                connectedPages.add(pageName);
                log.info("➡️ Tạo mới kết nối cho trang: {} ({})", pageName, pageId);
            } else {
                wasInactive = !conn.isActive();
                if (wasInactive) {
                    reactivatedPages.add(pageName);
                    log.info("♻️ Kích hoạt lại trang: {} ({})", pageName, pageId);
                } else {
                    connectedPages.add(pageName);
                    log.debug("🔄 Trang {} đã active, chỉ cập nhật token.", pageName);
                }
                // Nếu trang đã tồn tại nhưng CHƯA có Chatwoot ID (do cấu hình cũ), vẫn tạo mới
                if (conn.getChatwootInboxId() == null) {
                    needsChatwootCreate = true;
                    log.info("🟡 Trang {} chưa có Chatwoot ID, sẽ tạo mới.", pageName);
                }
            }
            
            // Cập nhật các trường Facebook
            conn.setPageAccessToken(pageToken);
            conn.setBotName(pageName);
            conn.setEnabled(true);
            conn.setActive(true);
            conn.setUpdatedAt(LocalDateTime.now());
            toSave.add(conn);

            // Xếp hàng đợi xử lý Webhook và Chatwoot
            // Chatwoot Inbox Creation phải xảy ra TRƯỚC khi lưu Transaction
            if (needsChatwootCreate) {
                try {
                    Map<String, Object> chatwootData = chatwootApiService.createApiInbox(pageName);
                    conn.setChatwootInboxId((Integer) chatwootData.get("inbox_id"));
                    conn.setChatwootChannelKey((String) chatwootData.get("channel_key"));
                    log.info("✅ Chatwoot Inbox ID {} đã được tạo và gán.", conn.getChatwootInboxId());
                } catch (Exception e) {
                    log.error("❌ Lỗi tạo Chatwoot Inbox cho trang {}: {}", pageName, e.getMessage());
                    errors.add(new ConnectionError(pageName, "Lỗi tạo Chatwoot Inbox: " + e.getMessage()));
                    // Đánh dấu inactive và không đăng ký webhook nếu không tạo được Chatwoot
                    conn.setActive(false);
                    conn.setEnabled(false);
                }
            }

            // Chỉ đăng ký webhook nếu Active/Enabled và không có lỗi Chatwoot
            boolean needsSub = (isNew || wasInactive) && conn.isActive();
            webhookQueue.add(new ConnectionToProcess(conn, needsSub, false, false, false));
        }

        // 4️⃣ Trang bị gỡ quyền (Inactive)
        for (FacebookConnection conn : existingConnections.stream().filter(c -> fbUserId.equals(c.getFbUserId())).collect(Collectors.toList())) {
            if (!fbPageIds.contains(conn.getPageId()) && conn.isActive()) {
                conn.setActive(false);
                conn.setUpdatedAt(LocalDateTime.now());
                toSave.add(conn);

                inactivePages.add(conn.getBotName());
                // 🚀 Cần hủy đăng ký webhook VÀ xóa Chatwoot Inbox
                webhookQueue.add(new ConnectionToProcess(conn, false, true, false, true)); 
                log.info("❌ Đánh dấu trang {} ({}) là inactive.", conn.getBotName(), conn.getPageId());
            }
        }

        // 5️⃣ Lưu thay đổi (Tất cả logic Chatwoot đã được xử lý TRƯỚC transaction này)
        if (!toSave.isEmpty()) {
            persistenceService.saveConnectionsTransactional(toSave, ownerId);
        }

        // 6️⃣ Xử lý webhook và Chatwoot deletion ngoài transaction
        processWebhooksAndChatwoot(webhookQueue, errors);

        // ✅ Tạo message kết quả
        String message = String.format(
                "Xử lý xong: %d trang mới, %d trang kích hoạt lại, %d trang vô hiệu hóa.",
                connectedPages.size() - reactivatedPages.size(), reactivatedPages.size(), inactivePages.size()
        );

        if (!errors.isEmpty()) {
            message += " Có lỗi khi xử lý webhook/Chatwoot.";
        }

        log.info("✅ Auto connect hoàn tất cho ownerId={}", ownerId);
        return new AutoConnectResponse(errors.isEmpty(), message, connectedPages, reactivatedPages, inactivePages, errors);
    }

    /**
     * Đăng ký / hủy đăng ký webhook và Xóa Chatwoot Inbox ngoài transaction DB
     */
    protected void processWebhooksAndChatwoot(List<ConnectionToProcess> queue, List<ConnectionError> errors) {
        for (ConnectionToProcess task : queue) {
            FacebookConnection conn = task.connection;

            // Xử lý Hủy đăng ký webhook (Facebook)
            if (task.needsWebhookUnsubscription) {
                try {
                    facebookApiGraphService.unsubscribePageFromWebhook(conn.getPageId(), conn.getPageAccessToken());
                    log.debug("🪓 Hủy đăng ký webhook thành công cho {}", conn.getPageId());
                } catch (Exception e) {
                    log.error("❌ Lỗi hủy đăng ký webhook cho trang {}: {}", conn.getPageId(), e.getMessage());
                    errors.add(new ConnectionError(conn.getBotName(), "Lỗi hủy webhook: " + e.getMessage()));
                }
            }
            
            // 🚀 Xử lý Xóa Chatwoot Inbox
            if (task.needsChatwootInboxDeletion && conn.isChatwootConnected()) {
                try {
                    chatwootApiService.deleteInbox(conn.getChatwootInboxId());
                    log.info("🗑️ Xóa Chatwoot Inbox {} thành công.", conn.getChatwootInboxId());
                    // KHÔNG cần xóa ID khỏi Entity vì Entity đã được đánh dấu inactive và lưu rồi
                } catch (Exception e) {
                    log.error("❌ Lỗi xóa Chatwoot Inbox cho trang {}: {}", conn.getBotName(), e.getMessage());
                    errors.add(new ConnectionError(conn.getBotName(), "Lỗi xóa Chatwoot Inbox: " + e.getMessage()));
                }
            }

            // Xử lý Đăng ký webhook (Facebook)
            if (task.needsWebhookSubscription) {
                try {
                    facebookApiGraphService.subscribePageToWebhook(conn.getPageId(), conn.getPageAccessToken());
                    log.debug("📡 Đăng ký webhook thành công cho {}", conn.getPageId());
                } catch (Exception e) {
                    log.error("❌ Lỗi đăng ký webhook cho trang {}: {}", conn.getPageId(), e.getMessage());
                    errors.add(new ConnectionError(conn.getBotName(), "Lỗi đăng ký webhook: " + e.getMessage()));

                    // ⚠️ Nếu lỗi đăng ký webhook, đánh dấu lại là inactive và lưu (phải dùng repository)
                    conn.setActive(false);
                    conn.setEnabled(false);
                    conn.setUpdatedAt(LocalDateTime.now());
                    connectionRepository.save(conn);
                    log.warn("⚠️ Đã đánh dấu trang {} là inactive do lỗi webhook.", conn.getPageId());
                }
            }
        }
    }
}