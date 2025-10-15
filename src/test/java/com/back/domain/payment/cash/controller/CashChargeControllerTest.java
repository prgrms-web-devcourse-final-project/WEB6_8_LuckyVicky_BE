package com.back.domain.payment.cash.controller;

import com.back.domain.payment.cash.dto.request.CashChargeRequestDto;
import com.back.domain.payment.cash.dto.response.CashChargeResponseDto;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.payment.cash.service.CashChargeService;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("캐시 충전 Controller 테스트")
class CashChargeControllerTest {

    @Mock
    private CashChargeService cashChargeService;

    @InjectMocks
    private CashChargeController cashChargeController;

    @Test
    @DisplayName("캐시 충전 신청 성공")
    void createChargeRequest_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        CustomUserDetails customUserDetails = new CustomUserDetails(user, com.back.domain.user.entity.Role.USER);
        CashChargeRequestDto requestDto = new CashChargeRequestDto(10000, "토스페이", "TOSS");
        
        CashChargeResponseDto responseDto = CashChargeResponseDto.builder()
                .transactionId(1L)
                .userId(1L)
                .amount(10000)
                .status(CashTransactionStatus.PENDING)
                .paymentMethod("토스페이")
                .pgProvider("TOSS")
                .createdAt(LocalDateTime.now())
                .build();

        given(cashChargeService.createChargeRequest(any(CashChargeRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<CashChargeResponseDto> response = 
                cashChargeController.createChargeRequest(requestDto, customUserDetails);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(10000);
        assertThat(response.getBody().getStatus()).isEqualTo(CashTransactionStatus.PENDING);
        
        verify(cashChargeService).createChargeRequest(any(CashChargeRequestDto.class), any(User.class));
    }

    @Test
    @DisplayName("캐시 충전 완료 성공")
    void completeCharge_Success() {
        // given
        Long transactionId = 1L;
        String paymentKey = "pay_test123";
        String orderId = "order_test123";
        
        CashChargeResponseDto responseDto = CashChargeResponseDto.builder()
                .transactionId(transactionId)
                .userId(1L)
                .amount(10000)
                .status(CashTransactionStatus.COMPLETED)
                .paymentMethod("토스페이")
                .pgProvider("TOSS")
                .pgTransactionId(paymentKey)
                .pgApprovalNumber("12345678")
                .balanceAfter(10000)
                .completedAt(LocalDateTime.now())
                .build();

        given(cashChargeService.completeCharge(anyLong(), anyString(), anyString()))
                .willReturn(responseDto);

        // when
        ResponseEntity<CashChargeResponseDto> response = 
                cashChargeController.completeCharge(transactionId, paymentKey, orderId);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(CashTransactionStatus.COMPLETED);
        assertThat(response.getBody().getPgTransactionId()).isEqualTo(paymentKey);
        
        verify(cashChargeService).completeCharge(transactionId, paymentKey, orderId);
    }

    @Test
    @DisplayName("캐시 충전 실패 처리")
    void failCharge_Success() {
        // given
        Long transactionId = 1L;
        String failureReason = "카드 한도 초과";

        // when
        ResponseEntity<Void> response = 
                cashChargeController.failCharge(transactionId, failureReason);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        verify(cashChargeService).failCharge(eq(transactionId), eq(failureReason));
    }

    @Test
    @DisplayName("캐시 충전 취소 성공")
    void cancelCharge_Success() {
        // given
        Long transactionId = 1L;
        String paymentKey = "pay_test123";
        String cancellationReason = "고객 변심";

        // when
        ResponseEntity<Void> response = 
                cashChargeController.cancelCharge(transactionId, paymentKey, cancellationReason);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        verify(cashChargeService).cancelCharge(eq(transactionId), eq(paymentKey), eq(cancellationReason));
    }
}

