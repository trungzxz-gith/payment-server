package com.payment_server.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/webhook")
public class Webhook {

    private final String FIREBASE_URL = "https://tour-paymenttest-default-rtdb.firebaseio.com";
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/sepay")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Webhook nhận được: " + payload);

        String transferType = String.valueOf(payload.get("transferType"));
        String content = payload.get("content") != null ? payload.get("content").toString() : "";

        if ("in".equalsIgnoreCase(transferType) && !content.isEmpty()) {
            Matcher matcher = Pattern.compile("SEVQR\\s*(\\d+)").matcher(content.toUpperCase());

            if (matcher.find()) {
                String orderId = "SEVQR" + matcher.group(1);
                System.out.println("Tìm thấy mã đơn hàng: " + orderId);
                updateFirebaseStatus(orderId);
            }
        }

        // Luôn trả về HTTP 200 OK để SePay không gửi lại webhook
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void updateFirebaseStatus(String orderId) {
        try {
            String url = FIREBASE_URL + "/Transactions/" + orderId + ".json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("\"Đã thanh toán\"", headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            System.out.println("Cập nhật Firebase thành công: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Firebase: " + e.getMessage());
        }
    }
}
