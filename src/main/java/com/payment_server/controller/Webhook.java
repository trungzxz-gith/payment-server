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

    @PostMapping("/sepay")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody Map<String, Object> payload) {
        String transferType = (String) payload.get("transferType");
        String content = (String) payload.get("content");

        if ("in".equals(transferType) && content != null) {
            Matcher matcher = Pattern.compile("SEVQR\\s*(\\d+)").matcher(content.toUpperCase());

            if (matcher.find()) {
                String orderId = "SEVQR" + matcher.group(1);
                updateFirebaseStatus(orderId);
            }
        }

        // Luôn trả về HTTP 200 OK để SePay báo xanh
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void updateFirebaseStatus(String orderId) {
        String url = FIREBASE_URL + "/Transactions/" + orderId + ".json";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("\"Đã thanh toán\"", headers);

        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }
}