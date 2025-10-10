package com.back.domain.payment.gateway.service;

import com.back.domain.payment.gateway.dto.TossPaymentApproveRequest;
import com.back.domain.payment.gateway.dto.TossPaymentApproveResponse;
import com.back.domain.payment.gateway.dto.TossPaymentCancelRequest;
import com.back.domain.payment.gateway.dto.TossPaymentCancelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 토스페이먼츠 PG 연동 서비스
 * PortOne을 통한 토스페이먼츠 API 호출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final RestTemplate restTemplate;

    @Value("${payment.portone.imp-key}")
    private String impKey;

    @Value("${payment.portone.imp-secret}")
    private String impSecret;

    @Value("${payment.toss.api-url:https://api.tosspayments.com/v1}")
    private String tossApiUrl;

    /**
     * 토스페이먼츠 결제 승인
     * 
     * @param paymentKey 토스페이먼츠에서 발급한 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 결제 승인 응답
     */
    public TossPaymentApproveResponse approvePayment(String paymentKey, String orderId, Integer amount) {
        log.info("토스페이먼츠 결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}", 
                paymentKey, orderId, amount);

        try {
            // 1. 요청 URL
            String url = tossApiUrl + "/payments/confirm";

            // 2. 요청 Body
            TossPaymentApproveRequest request = TossPaymentApproveRequest.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            // 3. 요청 Header (Basic Auth)
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<TossPaymentApproveRequest> entity = new HttpEntity<>(request, headers);

            // 4. API 호출
            ResponseEntity<TossPaymentApproveResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentApproveResponse.class
            );

            log.info("토스페이먼츠 결제 승인 성공 - paymentKey: {}, status: {}", 
                    paymentKey, response.getBody().getStatus());

            return response.getBody();

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
            throw new IllegalStateException("결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스페이먼츠 결제 취소
     * 
     * @param paymentKey 토스페이먼츠에서 발급한 결제 키
     * @param cancelReason 취소 사유
     * @param cancelAmount 취소 금액 (null이면 전액 취소)
     * @return 결제 취소 응답
     */
    public TossPaymentCancelResponse cancelPayment(String paymentKey, String cancelReason, Integer cancelAmount) {
        log.info("토스페이먼츠 결제 취소 요청 - paymentKey: {}, reason: {}, amount: {}", 
                paymentKey, cancelReason, cancelAmount);

        try {
            // 1. 요청 URL
            String url = tossApiUrl + "/payments/" + paymentKey + "/cancel";

            // 2. 요청 Body
            TossPaymentCancelRequest request = TossPaymentCancelRequest.builder()
                    .cancelReason(cancelReason)
                    .cancelAmount(cancelAmount)
                    .build();

            // 3. 요청 Header (Basic Auth)
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<TossPaymentCancelRequest> entity = new HttpEntity<>(request, headers);

            // 4. API 호출
            ResponseEntity<TossPaymentCancelResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentCancelResponse.class
            );

            log.info("토스페이먼츠 결제 취소 성공 - paymentKey: {}, cancelAmount: {}", 
                    paymentKey, response.getBody().getCancelAmount());

            return response.getBody();

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 취소 실패 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
            throw new IllegalStateException("결제 취소에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 토스페이먼츠 결제 조회
     * 
     * @param paymentKey 토스페이먼츠에서 발급한 결제 키
     * @return 결제 조회 응답
     */
    public TossPaymentApproveResponse getPayment(String paymentKey) {
        log.info("토스페이먼츠 결제 조회 - paymentKey: {}", paymentKey);

        try {
            // 1. 요청 URL
            String url = tossApiUrl + "/payments/" + paymentKey;

            // 2. 요청 Header (Basic Auth)
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 3. API 호출
            ResponseEntity<TossPaymentApproveResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    TossPaymentApproveResponse.class
            );

            log.info("토스페이먼츠 결제 조회 성공 - paymentKey: {}, status: {}", 
                    paymentKey, response.getBody().getStatus());

            return response.getBody();

        } catch (Exception e) {
            log.error("토스페이먼츠 결제 조회 실패 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
            throw new IllegalStateException("결제 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Basic Authentication 헤더 생성
     * PortOne Secret Key를 Base64 인코딩
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // PortOne Secret Key를 Base64로 인코딩 (토스페이먼츠 요구사항)
        String auth = impSecret + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }

    /**
     * 테스트 모드 여부 확인
     */
    public boolean isTestMode() {
        return impKey != null && impKey.startsWith("imp");
    }
}

