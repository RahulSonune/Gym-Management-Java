package com.fitlife.gym.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.gym.config.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Razorpay REST integration (orders + payment fetch + signature verification).
 * Uses JDK HttpClient — no extra Maven dependency.
 */
@Service
@RequiredArgsConstructor
public class RazorpayService {

    private static final String API_BASE = "https://api.razorpay.com/v1";

    private final RazorpayProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isConfigured() {
        return props.isEnabled()
                && !props.getKeyId().isBlank()
                && !props.getKeySecret().isBlank();
    }

    /** Safe to send to the browser (Checkout). */
    public String getPublishableKeyId() {
        return props.getKeyId();
    }

    public JsonNode createOrder(long amountMinor, String receipt) {
        requireConfigured();
        if (amountMinor <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        if (receipt.length() > 40) {
            receipt = receipt.substring(0, 40);
        }
        String json = String.format(
                "{\"amount\":%d,\"currency\":\"INR\",\"receipt\":\"%s\",\"payment_capture\":%d}",
                amountMinor,
                escapeJson(receipt),
                1);
        String auth = Base64.getEncoder().encodeToString(
                (props.getKeyId() + ":" + props.getKeySecret()).getBytes(StandardCharsets.UTF_8));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/orders"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Razorpay order failed: " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Razorpay unreachable: " + e.getMessage());
        }
    }

    public JsonNode fetchPayment(String paymentId) {
        requireConfigured();
        String auth = Base64.getEncoder().encodeToString(
                (props.getKeyId() + ":" + props.getKeySecret()).getBytes(StandardCharsets.UTF_8));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/payments/" + paymentId))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + auth)
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Razorpay payment fetch failed");
            }
            return objectMapper.readTree(response.body());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String razorpaySignature) {
        requireConfigured();
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return constantTimeEquals(expected, razorpaySignature.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Razorpay is not configured on the server");
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
