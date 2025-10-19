package com.back.domain.payment.webhook.controller;

import com.back.domain.payment.cash.service.CashChargeService;
import com.back.domain.payment.webhook.dto.TossWebhookRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 토스페이먼츠 Webhook Controller
 * 토스페이먼츠가 결제 완료/실패 시 자동으로 호출하는 API
 */
@Tag(name = "토스 Webhook", description = "토스페이먼츠 Webhook 처리 API")
@RestController
@RequestMapping("/api/webhooks/toss")
@RequiredArgsConstructor
@Slf4j
public class TossWebhookController {

    private final CashChargeService cashChargeService;

    @Value("${payment.toss.webhook-secret}")
    private String webhookSecret;

    /**
     * 토스페이먼츠 결제 성공 Webhook
     * 토스가 결제 완료 시 자동으로 이 API 호출
     */
    @PostMapping("/success")
    public ResponseEntity<Void> handlePaymentSuccess(
            @RequestBody String rawBody,
            @RequestHeader(value = "toss-signature", required = false) String signature) {
        
        log.info("토스 Webhook 수신 (성공) - signature: {}", signature != null ? "있음" : "없음");

        try {
            // 1. 서명 검증 (보안)
            if (signature != null && !verifySignature(rawBody, signature)) {
                log.error("토스 Webhook 서명 검증 실패 - 위조된 요청 가능성!");
                return ResponseEntity.status(403).build();
            }
            
            // 2. JSON 파싱
            TossWebhookRequest request = parseWebhookRequest(rawBody);
            log.info("토스 Webhook 파싱 완료 - paymentKey: {}, orderId: {}, amount: {}", 
                    request.getPaymentKey(), request.getOrderId(), request.getAmount());
            
            // 3. orderId에서 transactionId 추출 (format: "CHARGE_{transactionId}")
            Long transactionId = extractTransactionId(request.getOrderId());
            
            // 4. 충전 완료 처리
            cashChargeService.completeCharge(
                    transactionId, 
                    request.getPaymentKey(), 
                    request.getOrderId()
            );
            
            log.info("토스 Webhook 처리 완료 (성공) - transactionId: {}", transactionId);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("토스 Webhook 처리 실패 - error: {}", e.getMessage());
            
            // 토스에게 200 반환 (재시도 방지)
            return ResponseEntity.ok().build();
        }
    }

    /**
     * 토스페이먼츠 결제 실패 Webhook
     * 토스가 결제 실패 시 자동으로 이 API 호출
     */
    @PostMapping("/fail")
    public ResponseEntity<Void> handlePaymentFail(
            @RequestBody String rawBody,
            @RequestHeader(value = "toss-signature", required = false) String signature) {
        
        log.info("토스 Webhook 수신 (실패)");

        try {
            // 1. 서명 검증 (보안)
            if (signature != null && !verifySignature(rawBody, signature)) {
                log.error("토스 Webhook 서명 검증 실패 - 위조된 요청 가능성!");
                return ResponseEntity.status(403).build();
            }
            
            // 2. JSON 파싱
            TossWebhookRequest request = parseWebhookRequest(rawBody);
            log.info("토스 Webhook 파싱 완료 (실패) - paymentKey: {}, orderId: {}, failureMessage: {}", 
                    request.getPaymentKey(), request.getOrderId(), request.getFailureMessage());
            
            // 3. orderId에서 transactionId 추출
            Long transactionId = extractTransactionId(request.getOrderId());
            
            // 4. 충전 실패 처리
            cashChargeService.failCharge(
                    transactionId,
                    request.getFailureMessage() != null ? request.getFailureMessage() : "결제 실패"
            );
            
            log.info("토스 Webhook 처리 완료 (실패) - transactionId: {}", transactionId);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("토스 Webhook 처리 실패 - error: {}", e.getMessage());
            
            // 토스에게 200 반환 (재시도 방지)
            return ResponseEntity.ok().build();
        }
    }

    /**
     * orderId에서 transactionId 추출
     * orderId format: "CHARGE_{transactionId}"
     */
    private Long extractTransactionId(String orderId) {
        try {
            String[] parts = orderId.split("_");
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 orderId 형식입니다: " + orderId);
        }
    }

    /**
     * Webhook 서명 검증 (HMAC SHA-256)
     */
    private boolean verifySignature(String rawBody, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), 
                    "HmacSHA256"
            );
            mac.init(secretKey);
            
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getEncoder().encodeToString(hash);
            
            boolean isValid = expectedSignature.equals(signature);
            
            if (!isValid) {
                log.warn("서명 불일치 - expected: {}, actual: {}", expectedSignature, signature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("서명 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JSON 문자열을 TossWebhookRequest로 파싱
     */
    private TossWebhookRequest parseWebhookRequest(String rawBody) {
        try {
            // 간단한 JSON 파싱 (Jackson 사용)
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(rawBody, TossWebhookRequest.class);
        } catch (Exception e) {
            log.error("Webhook 요청 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("Webhook 요청 파싱에 실패했습니다.");
        }
    }
}

