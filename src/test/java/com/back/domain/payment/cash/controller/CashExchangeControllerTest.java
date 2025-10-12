package com.back.domain.payment.cash.controller;

import com.back.domain.payment.cash.dto.request.CashExchangeRequestDto;
import com.back.domain.payment.cash.dto.response.CashExchangeResponseDto;
import com.back.domain.payment.cash.entity.CashTransactionStatus;
import com.back.domain.payment.cash.entity.CashTransactionType;
import com.back.domain.payment.cash.service.CashExchangeService;
import com.back.domain.user.entity.User;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("캐시 환전 Controller 테스트")
class CashExchangeControllerTest {

    @Mock
    private CashExchangeService cashExchangeService;

    @InjectMocks
    private CashExchangeController cashExchangeController;

    @Test
    @DisplayName("환전 신청 성공")
    void createExchangeRequest_Success() {
        // given
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "01012345678");
        CashExchangeRequestDto requestDto = new CashExchangeRequestDto(50000, "우리은행", "123-456-7890", "홍길동");
        
        CashExchangeResponseDto responseDto = CashExchangeResponseDto.builder()
                .transactionId(1L)
                .artistId(1L)
                .amount(50000)
                .status(CashTransactionStatus.PENDING)
                .pgProvider("TOSS")
                .createdAt(LocalDateTime.now())
                .build();

        given(cashExchangeService.createExchangeRequest(any(CashExchangeRequestDto.class), any(User.class)))
                .willReturn(responseDto);

        // when
        ResponseEntity<CashExchangeResponseDto> response = 
                cashExchangeController.createExchangeRequest(requestDto, artist);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(50000);
        assertThat(response.getBody().getStatus()).isEqualTo(CashTransactionStatus.PENDING);
        
        verify(cashExchangeService).createExchangeRequest(any(CashExchangeRequestDto.class), any(User.class));
    }

    @Test
    @DisplayName("환전 승인 성공")
    void approveExchange_Success() {
        // given
        Long transactionId = 1L;
        String pgTransactionId = "pg_test123";
        String pgApprovalNumber = "87654321";
        
        CashExchangeResponseDto responseDto = CashExchangeResponseDto.builder()
                .transactionId(transactionId)
                .artistId(1L)
                .amount(50000)
                .status(CashTransactionStatus.COMPLETED)
                .pgProvider("TOSS")
                .pgTransactionId(pgTransactionId)
                .pgApprovalNumber(pgApprovalNumber)
                .balanceAfter(0)
                .completedAt(LocalDateTime.now())
                .build();

        given(cashExchangeService.approveExchange(anyLong(), anyString(), anyString()))
                .willReturn(responseDto);

        // when
        ResponseEntity<CashExchangeResponseDto> response = 
                cashExchangeController.approveExchange(transactionId, pgTransactionId, pgApprovalNumber);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(CashTransactionStatus.COMPLETED);
        assertThat(response.getBody().getAmount()).isEqualTo(50000);
        
        verify(cashExchangeService).approveExchange(transactionId, pgTransactionId, pgApprovalNumber);
    }

    @Test
    @DisplayName("환전 거부 성공")
    void rejectExchange_Success() {
        // given
        Long transactionId = 1L;
        String reason = "출금 한도 초과";

        // when
        ResponseEntity<Void> response = 
                cashExchangeController.rejectExchange(transactionId, reason);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        verify(cashExchangeService).rejectExchange(transactionId, reason);
    }
}

