package com.back.domain.payment.gateway.service;

import com.back.domain.payment.gateway.dto.TossPaymentApproveRequest;
import com.back.domain.payment.gateway.dto.TossPaymentApproveResponse;
import com.back.domain.payment.gateway.dto.TossPaymentCancelRequest;
import com.back.domain.payment.gateway.dto.TossPaymentCancelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentGatewayService paymentGatewayService;

    @BeforeEach
    void setUp() {
        // 환경변수 설정 (Reflection 사용)
        ReflectionTestUtils.setField(paymentGatewayService, "impKey", "test_ck_123456");
        ReflectionTestUtils.setField(paymentGatewayService, "impSecret", "test_sk_789012");
        ReflectionTestUtils.setField(paymentGatewayService, "tossApiUrl", "https://api.tosspayments.com/v1");
    }

    @Test
    @DisplayName("결제 승인 - 성공")
    void approvePayment_Success() {
        // Given
        String paymentKey = "payment_key_123";
        String orderId = "order_123";
        Integer amount = 10000;

        TossPaymentApproveResponse mockResponse = TossPaymentApproveResponse.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .status("DONE")
                .totalAmount(amount)
                .balanceAmount(amount)
                .method("카드")
                .approvedAt(LocalDateTime.now())
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        TossPaymentApproveResponse result = paymentGatewayService.approvePayment(paymentKey, orderId, amount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo("DONE");
        assertThat(result.getTotalAmount()).isEqualTo(amount);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        );
    }

    @Test
    @DisplayName("결제 승인 - 실패 (PG 오류)")
    void approvePayment_Failure() {
        // Given
        String paymentKey = "invalid_key";
        String orderId = "order_123";
        Integer amount = 10000;

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        )).thenThrow(new RestClientException("PG 서버 오류"));

        // When & Then
        assertThatThrownBy(() -> paymentGatewayService.approvePayment(paymentKey, orderId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 승인에 실패했습니다");

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        );
    }

    @Test
    @DisplayName("결제 취소 - 성공")
    void cancelPayment_Success() {
        // Given
        String paymentKey = "payment_key_123";
        String cancelReason = "단순 변심";
        Integer cancelAmount = 10000;

        TossPaymentCancelResponse mockResponse = TossPaymentCancelResponse.builder()
                .paymentKey(paymentKey)
                .orderId("order_123")
                .status("CANCELED")
                .totalAmount(10000)
                .cancelAmount(cancelAmount)
                .balanceAmount(0)
                .cancelReason(cancelReason)
                .canceledAt(LocalDateTime.now())
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        TossPaymentCancelResponse result = paymentGatewayService.cancelPayment(paymentKey, cancelReason, cancelAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(result.getCancelAmount()).isEqualTo(cancelAmount);
        assertThat(result.getCancelReason()).isEqualTo(cancelReason);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        );
    }

    @Test
    @DisplayName("결제 취소 - 전액 취소")
    void cancelPayment_FullAmount() {
        // Given
        String paymentKey = "payment_key_123";
        String cancelReason = "구매 취소";
        Integer cancelAmount = null; // 전액 취소

        TossPaymentCancelResponse mockResponse = TossPaymentCancelResponse.builder()
                .paymentKey(paymentKey)
                .orderId("order_123")
                .status("CANCELED")
                .totalAmount(10000)
                .cancelAmount(10000)
                .balanceAmount(0)
                .cancelReason(cancelReason)
                .canceledAt(LocalDateTime.now())
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        TossPaymentCancelResponse result = paymentGatewayService.cancelPayment(paymentKey, cancelReason, cancelAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(result.getBalanceAmount()).isEqualTo(0);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        );
    }

    @Test
    @DisplayName("결제 취소 - 실패 (PG 오류)")
    void cancelPayment_Failure() {
        // Given
        String paymentKey = "invalid_key";
        String cancelReason = "취소";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        )).thenThrow(new RestClientException("PG 서버 오류"));

        // When & Then
        assertThatThrownBy(() -> paymentGatewayService.cancelPayment(paymentKey, cancelReason, 10000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 취소에 실패했습니다");

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentCancelResponse.class)
        );
    }

    @Test
    @DisplayName("결제 조회 - 성공")
    void getPayment_Success() {
        // Given
        String paymentKey = "payment_key_123";

        TossPaymentApproveResponse mockResponse = TossPaymentApproveResponse.builder()
                .paymentKey(paymentKey)
                .orderId("order_123")
                .status("DONE")
                .totalAmount(10000)
                .balanceAmount(10000)
                .method("카드")
                .approvedAt(LocalDateTime.now())
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        TossPaymentApproveResponse result = paymentGatewayService.getPayment(paymentKey);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(result.getStatus()).isEqualTo("DONE");

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        );
    }

    @Test
    @DisplayName("결제 조회 - 실패 (존재하지 않는 결제)")
    void getPayment_NotFound() {
        // Given
        String paymentKey = "invalid_key";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        )).thenThrow(new RestClientException("결제 정보를 찾을 수 없습니다"));

        // When & Then
        assertThatThrownBy(() -> paymentGatewayService.getPayment(paymentKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 조회에 실패했습니다");

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        );
    }

    @Test
    @DisplayName("테스트 모드 확인")
    void isTestMode_True() {
        // Given
        ReflectionTestUtils.setField(paymentGatewayService, "impKey", "imp_test_123");

        // When
        boolean result = paymentGatewayService.isTestMode();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인증 헤더 생성 검증")
    void createAuthHeaders_Validation() {
        // Given
        String paymentKey = "test_key";
        String orderId = "order_123";
        Integer amount = 10000;

        TossPaymentApproveResponse mockResponse = TossPaymentApproveResponse.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .status("DONE")
                .totalAmount(amount)
                .build();

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        )).thenAnswer(invocation -> {
            HttpEntity<?> entity = invocation.getArgument(2);
            HttpHeaders headers = entity.getHeaders();
            
            // Authorization 헤더 검증
            assertThat(headers.getFirst("Authorization")).startsWith("Basic ");
            assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            
            return new ResponseEntity<>(mockResponse, HttpStatus.OK);
        });

        // When
        paymentGatewayService.approvePayment(paymentKey, orderId, amount);

        // Then
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(TossPaymentApproveResponse.class)
        );
    }
}

