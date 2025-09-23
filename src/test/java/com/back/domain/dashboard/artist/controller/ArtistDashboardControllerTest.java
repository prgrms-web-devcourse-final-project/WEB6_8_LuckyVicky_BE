package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * ArtistDashboardController 테스트
 * 2025.09.22 생성
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
}
