package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.response.MoriCashBalanceResponseDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus;
import com.back.domain.payment.moriCash.entity.TransactionType;
import com.back.domain.payment.moriCash.service.MoriCashBalanceService;
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
@DisplayName("모리캐시 잔액 Controller 테스트")
class MoriCashBalanceControllerTest {

    @Mock
    private MoriCashBalanceService moriCashBalanceService;

    @InjectMocks
    private MoriCashBalanceController moriCashBalanceController;

    @Test
    @DisplayName("모리캐시 잔액 조회 성공")
    void getMyBalance_Success() {
        // given
        User user = User.createLocalUser("test@test.com", "password", "테스트", "01012345678");
        
        MoriCashBalanceResponseDto responseDto = MoriCashBalanceResponseDto.builder()
                .balanceId(1L)
                .userId(1L)
                .totalBalance(50000)
                .availableBalance(45000)
                .frozenBalance(5000)
                .totalCharged(50000)
                .totalUsed(5000)
                .build();

        given(moriCashBalanceService.getBalance(any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<MoriCashBalanceResponseDto> response = 
                moriCashBalanceController.getMyBalance(user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalBalance()).isEqualTo(50000);
        assertThat(response.getBody().getAvailableBalance()).isEqualTo(45000);
        assertThat(response.getBody().getFrozenBalance()).isEqualTo(5000);
        
        verify(moriCashBalanceService).getBalance(user);
    }

    @Test
    @DisplayName("모리캐시 거래 내역 조회 성공")
    void getMyHistory_Success() {
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
                .balanceAfter(40000)
                .createdAt(LocalDateTime.now())
                .build();
        
        Page<MoriCashPaymentResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);

        given(moriCashBalanceService.getHistory(any(User.class), any(Pageable.class)))
                .willReturn(page);

        // when
        ResponseEntity<Page<MoriCashPaymentResponseDto>> response = 
                moriCashBalanceController.getMyHistory(user, pageable);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        
        verify(moriCashBalanceService).getHistory(user, pageable);
    }
}

