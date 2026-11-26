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

    private final String FIREBASE_URL = "https://travel-52457-default-rtdb.firebaseio.com/Payments";
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/sepay")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Webhook nhận được: " + payload);

        String transferType = String.valueOf(payload.get("transferType"));
        String content = payload.get("content") != null ? payload.get("content").toString() : "";

        // Kiểm tra giao dịch tiền vào, có nội dung và có số tiền
        if ("in".equalsIgnoreCase(transferType) && !content.isEmpty() && payload.get("transferAmount") != null) {
            Matcher matcher = Pattern.compile("SEVQR\\s*(\\d+)").matcher(content.toUpperCase());

            if (matcher.find()) {
                String orderId = "SEVQR" + matcher.group(1);
                long transferAmount = (long) Double.parseDouble(String.valueOf(payload.get("transferAmount")));

                System.out.println("Đơn hàng: " + orderId + " | Tiền nhận: " + transferAmount);
                verifyAndUpdateStatus(orderId, transferAmount);
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    private void verifyAndUpdateStatus(String orderId, long transferAmount) {
        try {
            // 1. Lấy thông tin đơn hàng từ Firebase để xem số tiền cần thanh toán
            String orderUrl = FIREBASE_URL + "/" + orderId + ".json";
            Map<String, Object> orderData = restTemplate.getForObject(orderUrl, Map.class);

            if (orderData == null || orderData.get("amount") == null) {
                System.err.println("Không tìm thấy đơn hàng hoặc thiếu trường amount trên Firebase!");
                return;
            }

            // 2. So sánh số tiền thực nhận với giá vé
            long expectedAmount = Long.parseLong(String.valueOf(orderData.get("amount")));
            String newStatus = (transferAmount == expectedAmount) ? "\"Đã thanh toán\"" : "\"Sai số tiền\"";

            // 3. Cập nhật vào nhánh con /status.json
            String statusUrl = FIREBASE_URL + "/" + orderId + "/status.json";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(newStatus, headers);

            ResponseEntity<String> response = restTemplate.exchange(statusUrl, HttpMethod.PUT, request, String.class);
            System.out.println("Cập nhật trạng thái thành: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi đối chiếu thanh toán: " + e.getMessage());
        }
    }
}
