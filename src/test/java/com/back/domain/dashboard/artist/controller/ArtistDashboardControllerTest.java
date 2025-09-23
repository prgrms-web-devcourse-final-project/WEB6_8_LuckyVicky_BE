package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.sevice.ArtistDashboardService;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * ArtistDashboardController 테스트
 * 2025.09.23 생성
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 컨트롤러 테스트")
class ArtistDashboardControllerTest {

    @Mock
    private ArtistDashboardService artistDashboardService;

    @InjectMocks
    private ArtistDashboardController artistDashboardController;

    private static final String BEARER_TOKEN = "Bearer test-artist-token-123";

    @Test
    @DisplayName("작가 지갑 잔액 조회 성공")
    void getCashBalance_Success() {
        // Given
        ArtistCashResponse.Balance mockBalance = ArtistCashResponse.Balance.builder()
                .currentBalance(72000)
                .pendingSettlement(15000)
                .pendingWithdrawal(0)
                .withdrawable(72000)
                .currency("KRW")
                .updatedAt(LocalDateTime.of(2025, 9, 24, 10, 0))
                .build();

        given(artistDashboardService.getCashBalance(anyString()))
                .willReturn(mockBalance);

        // When
        ResponseEntity<RsData<ArtistCashResponse.Balance>> response =
                artistDashboardController.getCashBalance(BEARER_TOKEN);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistCashResponse.Balance> body = response.getBody();
        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("작가지갑 요약 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                () -> assertThat(body.data().getCurrentBalance()).isEqualTo(72000),
                () -> assertThat(body.data().getPendingSettlement()).isEqualTo(15000),
                () -> assertThat(body.data().getPendingWithdrawal()).isEqualTo(0),
                () -> assertThat(body.data().getWithdrawable()).isEqualTo(72000),
                () -> assertThat(body.data().getCurrency()).isEqualTo("KRW")
        );
    }

    @Test
    @DisplayName("작가 상품 목록 조회 성공")
    void getProducts_Success() {
        // Given
        List<ArtistProductResponse.Product> content = List.of(
                ArtistProductResponse.Product.builder()
                        .productNumber("0123157")
                        .productName("상품명입니다 상품명입니다")
                        .price(90000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025. 09. 18")
                        .build()
        );

        ArtistProductResponse.List mockResponse = new ArtistProductResponse.List(
                content, 0, 10, 28, 3, true, false);

        given(artistDashboardService.getProducts(
                anyString(), anyInt(), anyInt(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistProductResponse.List>> response =
                artistDashboardController.getProducts(BEARER_TOKEN, 0, 10, null, null, "registrationDate", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistProductResponse.List> body = response.getBody();
        ArtistProductResponse.Product firstProduct = body.data().getContent().get(0);

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("내 상품 목록 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                () -> assertThat(body.data().getContent()).hasSize(1),
                () -> assertThat(firstProduct.getProductNumber()).isEqualTo("0123157"),
                () -> assertThat(firstProduct.getProductName()).isEqualTo("상품명입니다 상품명입니다"),
                () -> assertThat(firstProduct.getPrice()).isEqualTo(90000),
                () -> assertThat(firstProduct.getSellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(firstProduct.getStatusText()).isEqualTo("판매중"),
                () -> assertThat(body.data().getPage()).isEqualTo(0),
                () -> assertThat(body.data().getSize()).isEqualTo(10),
                () -> assertThat(body.data().getTotalElements()).isEqualTo(28)
        );
    }

    @Test
    @DisplayName("작가 캐시 내역 조회 성공")
    void getCashHistory_Success() {
        // Given
        ArtistCashHistoryResponse.Summary summary = ArtistCashHistoryResponse.Summary.builder()
                .periodDepositTotal(74000)
                .periodWithdrawalTotal(64000)
                .periodNet(10000)
                .build();

        List<ArtistCashHistoryResponse.Transaction> content = Arrays.asList(
                ArtistCashHistoryResponse.Transaction.builder()
                        .txId("TX-20250924-0001")
                        .transactedAt("2025-09-24T09:12:00+09:00")
                        .type("DEPOSIT")
                        .typeText("정산금 입금")
                        .depositAmount(10000)
                        .withdrawalAmount(0)
                        .balanceAfter(10000)
                        .method("WALLET")
                        .methodText("모리캐시")
                        .status("COMPLETED")
                        .note(null)
                        .build(),
                ArtistCashHistoryResponse.Transaction.builder()
                        .txId("TX-20250923-0004")
                        .transactedAt("2025-09-23T16:20:00+09:00")
                        .type("WITHDRAWAL")
                        .typeText("모리캐시 환전")
                        .depositAmount(0)
                        .withdrawalAmount(64000)
                        .balanceAfter(0)
                        .method("BANK_TRANSFER")
                        .methodText("계좌이체")
                        .status("COMPLETED")
                        .note("우리은행 ****-**-***** 홍길동")
                        .build()
        );

        ArtistCashHistoryResponse.List mockResponse = ArtistCashHistoryResponse.List.builder()
                .summary(summary)
                .content(content)
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        given(artistDashboardService.getCashHistory(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCashHistoryResponse.List>> response =
                artistDashboardController.getCashHistory(BEARER_TOKEN, 0, 20, null, null, null, null, "transactedAt", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistCashHistoryResponse.List> body = response.getBody();
        ArtistCashHistoryResponse.Transaction firstTransaction = body.data().getContent().get(0);

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("입금/환전 내역 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                // 요약 정보 검증
                () -> assertThat(body.data().getSummary().getPeriodDepositTotal()).isEqualTo(74000),
                () -> assertThat(body.data().getSummary().getPeriodWithdrawalTotal()).isEqualTo(64000),
                () -> assertThat(body.data().getSummary().getPeriodNet()).isEqualTo(10000),
                // 거래 내역 검증
                () -> assertThat(body.data().getContent()).hasSize(2),
                () -> assertThat(firstTransaction.getTxId()).isEqualTo("TX-20250924-0001"),
                () -> assertThat(firstTransaction.getType()).isEqualTo("DEPOSIT"),
                () -> assertThat(firstTransaction.getDepositAmount()).isEqualTo(10000),
                () -> assertThat(firstTransaction.getBalanceAfter()).isEqualTo(10000),
                // 페이징 정보 검증
                () -> assertThat(body.data().getPage()).isEqualTo(0),
                () -> assertThat(body.data().getSize()).isEqualTo(20),
                () -> assertThat(body.data().getTotalElements()).isEqualTo(2),
                () -> assertThat(body.data().isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("작가 캐시 내역 조회 - 필터 파라미터 포함")
    void getCashHistory_WithFilters() {
        // Given
        ArtistCashHistoryResponse.List mockResponse = ArtistCashHistoryResponse.List.builder()
                .summary(ArtistCashHistoryResponse.Summary.builder()
                        .periodDepositTotal(10000)
                        .periodWithdrawalTotal(0)
                        .periodNet(10000)
                        .build())
                .content(Arrays.asList(
                        ArtistCashHistoryResponse.Transaction.builder()
                                .txId("TX-20250924-0001")
                                .type("DEPOSIT")
                                .status("COMPLETED")
                                .depositAmount(10000)
                                .withdrawalAmount(0)
                                .build()
                ))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        given(artistDashboardService.getCashHistory(
                eq(BEARER_TOKEN), eq(0), eq(20), eq("DEPOSIT"), eq("COMPLETED"), 
                eq("2025-09-01"), eq("2025-09-30"), eq("transactedAt"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCashHistoryResponse.List>> response =
                artistDashboardController.getCashHistory(BEARER_TOKEN, 0, 20, "DEPOSIT", "COMPLETED", 
                        "2025-09-01", "2025-09-30", "transactedAt", "DESC");

        // Then - 필터링된 결과 검증
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().getContent()).hasSize(1),
                () -> assertThat(response.getBody().data().getContent().get(0).getType()).isEqualTo("DEPOSIT"),
                () -> assertThat(response.getBody().data().getSummary().getPeriodWithdrawalTotal()).isEqualTo(0)
        );
    }
}
