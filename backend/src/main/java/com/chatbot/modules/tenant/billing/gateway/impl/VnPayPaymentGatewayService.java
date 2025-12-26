package com.chatbot.modules.tenant.billing.gateway.impl;

import com.chatbot.modules.tenant.billing.gateway.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayPaymentGatewayService implements PaymentGatewayService {

    @Value("${vnp.payUrl}")
    private String vnp_PayUrl;
    
    @Value("${vnp.returnUrl}")
    private String vnp_ReturnUrl;
    
    @Value("${vnp.tmnCode}")
    private String vnp_TmnCode;
    
    @Value("${vnp.secretKey}")
    private String vnp_HashSecret;
    
    private final String vnp_Version = "2.1.0";
    private final String vnp_Command = "pay";
    private final String vnp_OrderType = "billpayment";

    @Override
    public String charge(Long tenantId, long amount, String currency, String description) {
        try {
            String vnp_TxnRef = "ORDER" + System.currentTimeMillis();
            String vnp_IpAddr = "127.0.0.1"; // Thay bằng IP thực tế của client
            
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", this.vnp_Version);
            vnp_Params.put("vnp_Command", this.vnp_Command);
            vnp_Params.put("vnp_TmnCode", this.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY yêu cầu số tiền nhân 100
            vnp_Params.put("vnp_CurrCode", currency);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", description);
            vnp_Params.put("vnp_OrderType", this.vnp_OrderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", this.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", getCurrentDateTime());
            
            // Sắp xếp tham số theo thứ tự alphabet
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            
            // Tạo chuỗi dữ liệu để tạo hash
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                         .append('=')
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }
            
            // Tạo chữ ký
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append('&')
                 .append(URLEncoder.encode("vnp_SecureHash", StandardCharsets.US_ASCII.toString()))
                 .append('=')
                 .append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.US_ASCII.toString()));
            
            return vnp_PayUrl + "?" + query.toString();
            
        } catch (Exception ex) {
            throw new RuntimeException("Error creating VNPay payment URL", ex);
        }
    }

    @Override
    public String createPaymentIntent(double amount, String currency, String description) {
        // Tạo một payment intent đơn giản, có thể mở rộng sau
        Map<String, Object> intent = new HashMap<>();
        intent.put("id", "intent_" + System.currentTimeMillis());
        intent.put("amount", amount);
        intent.put("currency", currency);
        intent.put("description", description);
        intent.put("status", "created");
        
        // Trong thực tế, bạn nên lưu intent này vào database
        return "Payment intent created: " + intent.toString();
    }
    
    private String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(new Date());
    }
    
    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            
            final javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
            
        } catch (Exception ex) {
            throw new RuntimeException("Error generating HMAC-SHA512", ex);
        }
    }
}
