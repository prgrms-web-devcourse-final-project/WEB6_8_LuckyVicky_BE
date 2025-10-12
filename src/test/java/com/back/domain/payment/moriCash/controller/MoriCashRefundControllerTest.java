package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.request.MoriCashRefundRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashRefundResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import com.back.domain.payment.moriCash.service.MoriCashRefundService;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("모리캐시 환불 Controller 테스트")
class MoriCashRefundControllerTest {

    @Mock
    private MoriCashRefundService moriCashRefundService;

    @InjectMocks
    private MoriCashRefundController moriCashRefundController;

    @Test
    @DisplayName("모리캐시 환불 처리 성공")
    void processRefund_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        MoriCashRefundRequestDto requestDto = new MoriCashRefundRequestDto(1L, 5000, "단순 변심");
        
        MoriCashRefundResponseDto responseDto = MoriCashRefundResponseDto.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .refundId("REFUND_123")
                .refundPrice(5000)
                .balanceAfter(5000)
                .refundedAt(LocalDateTime.now())
                .build();

        given(moriCashRefundService.processRefund(any(MoriCashRefundRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<MoriCashRefundResponseDto> response = 
                moriCashRefundController.processRefund(requestDto, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRefundPrice()).isEqualTo(5000);
        
        verify(moriCashRefundService).processRefund(any(MoriCashRefundRequestDto.class), any(User.class));
    }

    @Test
    @DisplayName("모리캐시 환불 취소 성공")
    void cancelRefund_Success() {
        // given
        Long paymentId = 1L;
        String cancellationReason = "고객 요청";

        // when
        ResponseEntity<Void> response = 
                moriCashRefundController.cancelRefund(paymentId, cancellationReason);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        verify(moriCashRefundService).cancelRefund(paymentId, cancellationReason);
    }

    @Test
    @DisplayName("모리캐시 환불 상세 조회 성공")
    void getRefund_Success() {
        // given
        Long paymentId = 1L;
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        
        MoriCashRefundResponseDto responseDto = MoriCashRefundResponseDto.builder()
                .paymentId(paymentId)
                .orderId(1L)
                .userId(1L)
                .refundId("REFUND_456")
                .refundPrice(5000)
                .balanceAfter(5000)
                .refundedAt(LocalDateTime.now())
                .build();

        given(moriCashRefundService.getRefund(anyLong(), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<MoriCashRefundResponseDto> response = 
                moriCashRefundController.getRefund(paymentId, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentId()).isEqualTo(paymentId);
        
        verify(moriCashRefundService).getRefund(paymentId, user);
    }

    @Test
    @DisplayName("모리캐시 환불 내역 조회 성공")
    void getMyRefunds_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        Pageable pageable = PageRequest.of(0, 20);
        
        MoriCashRefundResponseDto responseDto = MoriCashRefundResponseDto.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .refundId("REFUND_123")
                .refundPrice(5000)
                .balanceAfter(5000)
                .refundedAt(LocalDateTime.now())
                .build();
        
        Page<MoriCashRefundResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        given(moriCashRefundService.getRefunds(any(User.class), any(Pageable.class)))
                .willReturn(page);

        // when
        ResponseEntity<Page<MoriCashRefundResponseDto>> response = 
                moriCashRefundController.getMyRefunds(user, pageable);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        
        verify(moriCashRefundService).getRefunds(user, pageable);
    }
}

