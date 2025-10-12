package com.back.domain.payment.webhook.controller;

import com.back.domain.payment.cash.service.CashChargeService;
import com.back.domain.payment.webhook.dto.TossWebhookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("토스 Webhook Controller 테스트")
class TossWebhookControllerTest {

    @Mock
    private CashChargeService cashChargeService;

    @InjectMocks
    private TossWebhookController tossWebhookController;

    @BeforeEach
    void setUp() {
        // webhookSecret 필드 설정 (Reflection 사용)
        ReflectionTestUtils.setField(tossWebhookController, "webhookSecret", "test-webhook-secret");
    }

    @Test
    @DisplayName("결제 성공 Webhook 처리 성공")
    void handlePaymentSuccess_Success() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test123\",\"orderId\":\"CHARGE_1\",\"amount\":10000,\"status\":\"DONE\"}";
        String signature = generateSignature(rawBody);

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentSuccess(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(cashChargeService).completeCharge(eq(1L), eq("pay_test123"), eq("CHARGE_1"));
    }

    @Test
    @DisplayName("결제 성공 Webhook - orderId 파싱 실패 시에도 200 반환")
    void handlePaymentSuccess_InvalidOrderId() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test123\",\"orderId\":\"INVALID_FORMAT\",\"amount\":10000,\"status\":\"DONE\"}";
        String signature = null;

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentSuccess(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("결제 성공 Webhook - Service 오류 시에도 200 반환")
    void handlePaymentSuccess_ServiceError() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test123\",\"orderId\":\"CHARGE_1\",\"amount\":10000,\"status\":\"DONE\"}";
        String signature = null;

        doThrow(new RuntimeException("DB 오류"))
                .when(cashChargeService)
                .completeCharge(anyLong(), anyString(), anyString());

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentSuccess(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("결제 실패 Webhook 처리 성공")
    void handlePaymentFail_Success() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test456\",\"orderId\":\"CHARGE_2\",\"amount\":10000,\"status\":\"FAILED\",\"failureCode\":\"INVALID_CARD\",\"failureMessage\":\"유효하지 않은 카드\"}";
        String signature = null;

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentFail(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(cashChargeService).failCharge(eq(2L), eq("유효하지 않은 카드"));
    }

    @Test
    @DisplayName("결제 실패 Webhook - failureMessage null인 경우")
    void handlePaymentFail_NullFailureMessage() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test456\",\"orderId\":\"CHARGE_3\",\"amount\":10000,\"status\":\"FAILED\"}";
        String signature = null;

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentFail(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(cashChargeService).failCharge(eq(3L), eq("결제 실패"));
    }

    @Test
    @DisplayName("결제 실패 Webhook - orderId 파싱 실패 시에도 200 반환")
    void handlePaymentFail_InvalidOrderId() {
        // given
        String rawBody = "{\"paymentKey\":\"pay_test456\",\"orderId\":\"INVALID\",\"amount\":10000,\"status\":\"FAILED\",\"failureMessage\":\"결제 실패\"}";
        String signature = null;

        // when
        ResponseEntity<Void> response = tossWebhookController.handlePaymentFail(rawBody, signature);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    /**
     * 서명 생성 헬퍼 메서드 (Controller와 동일한 로직)
     */
    private String generateSignature(String rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    "test-webhook-secret".getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("서명 생성 실패", e);
        }
    }
}

