package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.request.MoriCashPaymentRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import com.back.domain.payment.moriCash.service.MoriCashPaymentService;
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
@DisplayName("모리캐시 결제 Controller 테스트")
class MoriCashPaymentControllerTest {

    @Mock
    private MoriCashPaymentService moriCashPaymentService;

    @InjectMocks
    private MoriCashPaymentController moriCashPaymentController;

    @Test
    @DisplayName("모리캐시 결제 성공")
    void createPayment_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        MoriCashPaymentRequestDto requestDto = new MoriCashPaymentRequestDto(1L, 10000, 10000);
        
        MoriCashPaymentResponseDto responseDto = MoriCashPaymentResponseDto.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .status(MoriCashPaymentStatus.COMPLETED)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .createdAt(LocalDateTime.now())
                .build();

        given(moriCashPaymentService.createPayment(any(MoriCashPaymentRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<MoriCashPaymentResponseDto> response = 
                moriCashPaymentController.createPayment(requestDto, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(MoriCashPaymentStatus.COMPLETED);
        assertThat(response.getBody().getUsedMoriCash()).isEqualTo(10000);
        
        verify(moriCashPaymentService).createPayment(any(MoriCashPaymentRequestDto.class), any(User.class));
    }

    @Test
    @DisplayName("모리캐시 결제 취소 성공")
    void cancelPayment_Success() {
        // given
        Long paymentId = 1L;
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");

        // when
        ResponseEntity<Void> response = 
                moriCashPaymentController.cancelPayment(paymentId, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        verify(moriCashPaymentService).cancelPayment(paymentId, user);
    }

    @Test
    @DisplayName("모리캐시 결제 상세 조회 성공")
    void getPayment_Success() {
        // given
        Long paymentId = 1L;
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        
        MoriCashPaymentResponseDto responseDto = MoriCashPaymentResponseDto.builder()
                .paymentId(paymentId)
                .orderId(1L)
                .userId(1L)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .status(MoriCashPaymentStatus.COMPLETED)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .createdAt(LocalDateTime.now())
                .build();

        given(moriCashPaymentService.getPayment(anyLong(), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<MoriCashPaymentResponseDto> response = 
                moriCashPaymentController.getPayment(paymentId, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentId()).isEqualTo(paymentId);
        
        verify(moriCashPaymentService).getPayment(paymentId, user);
    }

    @Test
    @DisplayName("모리캐시 결제 내역 조회 성공")
    void getMyPayments_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        Pageable pageable = PageRequest.of(0, 20);
        
        MoriCashPaymentResponseDto responseDto = MoriCashPaymentResponseDto.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .totalPrice(10000)
                .usedMoriCash(10000)
                .status(MoriCashPaymentStatus.COMPLETED)
                .transactionType(TransactionType.PURCHASE)
                .description("상품 구매")
                .balanceAfter(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        Page<MoriCashPaymentResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        given(moriCashPaymentService.getPayments(any(User.class), any(Pageable.class)))
                .willReturn(page);

        // when
        ResponseEntity<Page<MoriCashPaymentResponseDto>> response = 
                moriCashPaymentController.getMyPayments(user, pageable);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        
        verify(moriCashPaymentService).getPayments(user, pageable);
    }
}

