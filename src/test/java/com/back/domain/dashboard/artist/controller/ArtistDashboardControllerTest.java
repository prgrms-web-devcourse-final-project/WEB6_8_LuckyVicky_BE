package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistOrderResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistExchangeResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistFundingResponse;
import com.back.domain.dashboard.artist.service.ArtistDashboardService;
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
 * 2025.09.25 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 컨트롤러 테스트")
class ArtistDashboardControllerTest {

    @Mock
    private ArtistDashboardService artistDashboardService;

    @InjectMocks
    private ArtistDashboardController artistDashboardController;

    private static final String BEARER_TOKEN = "Bearer test-artist-token-123";

    // 공통 상수들
    private static final String SUCCESS_CODE = "200-OK";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    /**
     * 성공 응답 검증 및 데이터 추출 헬퍼 메서드
     */
    private <T> T assertSuccessResponse(ResponseEntity<RsData<T>> response, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<T> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.resultCode()).isEqualTo(SUCCESS_CODE);
        assertThat(body.msg()).isEqualTo(expectedMessage);

        T data = body.data();
        assertThat(data).isNotNull();
        return data;
    }

    // Mock 데이터 생성 헬퍼 메서드들
    private ArtistCashResponse.Balance createMockBalance() {
        return new ArtistCashResponse.Balance(
                72000,
                15000,
                0,
                72000,
                "KRW",
                LocalDateTime.of(2025, 9, 24, 10, 0)
        );
    }

    private ArtistProductResponse.Product createMockProduct() {
        return new ArtistProductResponse.Product(
                "0123157",
                "상품명입니다 상품명입니다",
                90000,
                "SELLING",
                "판매중",
                "2025. 09. 18"
        );
    }

    private ArtistCashHistoryResponse.Transaction createMockTransaction(String type, String txId, int amount) {
        return new ArtistCashHistoryResponse.Transaction(
                txId,
                "2025-09-24T09:12:00+09:00",
                type,
                type.equals("DEPOSIT") ? "정산금 입금" : "모리캐시 환전",
                type.equals("DEPOSIT") ? amount : 0,
                type.equals("WITHDRAWAL") ? amount : 0,
                amount,
                type.equals("DEPOSIT") ? "WALLET" : "BANK_TRANSFER",
                type.equals("DEPOSIT") ? "모리캐시" : "계좌이체",
                "COMPLETED",
                type.equals("WITHDRAWAL") ? "우리은행 ****-**-***** 홍길동" : null
        );
    }

    private ArtistOrderResponse.Order createMockOrder(String orderId, String orderNumber, String status) {
        return new ArtistOrderResponse.Order(
                orderId,
                orderNumber,
                "2025-09-18",
                status,
                status.equals("PENDING") ? "발주 전" : "배송 중",
                47500,
                "상품명입니다 상품명입니다",
                2,
                new ArtistOrderResponse.Buyer(201L, "heroeson02", "손경호"),
                new ArtistOrderResponse.Shipment("READY", null, null),
                new ArtistOrderResponse.Permissions(true, true)
        );
    }

    @Test
    @DisplayName("작가 지갑 잔액 조회 성공")
    void getCashBalance_Success() {
        // Given
        ArtistCashResponse.Balance mockBalance = createMockBalance();
        given(artistDashboardService.getCashBalance(anyString())).willReturn(mockBalance);

        // When
        ResponseEntity<RsData<ArtistCashResponse.Balance>> response =
                artistDashboardController.getCashBalance(BEARER_TOKEN);

        // Then
        ArtistCashResponse.Balance data = assertSuccessResponse(response, "작가지갑 요약 조회 성공");
        assertAll(
                () -> assertThat(data.currentBalance()).isEqualTo(72000),
                () -> assertThat(data.pendingSettlement()).isEqualTo(15000),
                () -> assertThat(data.pendingWithdrawal()).isEqualTo(0),
                () -> assertThat(data.withdrawable()).isEqualTo(72000),
                () -> assertThat(data.currency()).isEqualTo("KRW")
        );
    }

    @Test
    @DisplayName("작가 상품 목록 조회 성공")
    void getProducts_Success() {
        // Given
        List<ArtistProductResponse.Product> content = List.of(createMockProduct());
        ArtistProductResponse.List mockResponse = new ArtistProductResponse.List(
                content, DEFAULT_PAGE, 10, 28, 3, true, false);

        given(artistDashboardService.getProducts(
                anyString(), anyInt(), anyInt(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistProductResponse.List>> response =
                artistDashboardController.getProducts(BEARER_TOKEN, DEFAULT_PAGE, 10, null, null, "registrationDate", "DESC");

        // Then
        ArtistProductResponse.List data = assertSuccessResponse(response, "내 상품 목록 조회 성공");
        ArtistProductResponse.Product firstProduct = data.getContent().getFirst();

        assertAll(
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(firstProduct.productNumber()).isNotBlank(),
                () -> assertThat(firstProduct.price()).isPositive(),
                () -> assertThat(firstProduct.sellingStatus()).isNotBlank(),
                () -> assertThat(data.getPage()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.getSize()).isEqualTo(10),
                () -> assertThat(data.getTotalElements()).isPositive()
        );
    }

    @Test
    @DisplayName("작가 캐시 내역 조회 성공")
    void getCashHistory_Success() {
        // Given
        ArtistCashHistoryResponse.Summary summary = new ArtistCashHistoryResponse.Summary(74000, 64000, 10000);

        List<ArtistCashHistoryResponse.Transaction> content = List.of(
                createMockTransaction("DEPOSIT", "TX-20250924-0001", 10000),
                createMockTransaction("WITHDRAWAL", "TX-20250923-0004", 64000)
        );

        ArtistCashHistoryResponse.List mockResponse = new ArtistCashHistoryResponse.List(
                summary, content, DEFAULT_PAGE, DEFAULT_SIZE, 2, 1, false, false
        );

        given(artistDashboardService.getCashHistory(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCashHistoryResponse.List>> response =
                artistDashboardController.getCashHistory(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, "transactedAt", "DESC");

        // Then
        ArtistCashHistoryResponse.List data = assertSuccessResponse(response, "입금/환전 내역 조회 성공");
        ArtistCashHistoryResponse.Transaction firstTransaction = data.content().getFirst();

        assertAll(
                // 요약 정보 검증
                () -> assertThat(data.summary()).isNotNull(),
                () -> assertThat(data.summary().periodDepositTotal()).isNotNegative(),
                () -> assertThat(data.summary().periodWithdrawalTotal()).isNotNegative(),
                // 거래 내역 검증
                () -> assertThat(data.content()).hasSize(2),
                () -> assertThat(firstTransaction.txId()).isNotBlank(),
                () -> assertThat(firstTransaction.type()).isNotBlank(),
                () -> assertThat(firstTransaction.balanceAfter()).isNotNegative(),
                // 페이징 정보 검증
                () -> assertThat(data.page()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.size()).isEqualTo(DEFAULT_SIZE),
                () -> assertThat(data.totalElements()).isPositive()
        );
    }

    @Test
    @DisplayName("작가 캐시 내역 조회 - 필터 파라미터 포함")
    void getCashHistory_WithFilters() {
        // Given
        ArtistCashHistoryResponse.List mockResponse = new ArtistCashHistoryResponse.List(
                new ArtistCashHistoryResponse.Summary(10000, 0, 10000),
                List.of(createMockTransaction("DEPOSIT", "TX-20250924-0001", 10000)),
                DEFAULT_PAGE, DEFAULT_SIZE, 1, 1, false, false
        );

        given(artistDashboardService.getCashHistory(
                eq(BEARER_TOKEN), eq(DEFAULT_PAGE), eq(DEFAULT_SIZE), eq("DEPOSIT"), eq("COMPLETED"),
                eq("2025-09-01"), eq("2025-09-30"), eq("transactedAt"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCashHistoryResponse.List>> response =
                artistDashboardController.getCashHistory(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, "DEPOSIT", "COMPLETED",
                        "2025-09-01", "2025-09-30", "transactedAt", "DESC");

        // Then
        ArtistCashHistoryResponse.List data = assertSuccessResponse(response, "입금/환전 내역 조회 성공");
        assertAll(
                () -> assertThat(data.content()).hasSize(1),
                () -> assertThat(data.content().getFirst().type()).isEqualTo("DEPOSIT"),
                () -> assertThat(data.summary().periodWithdrawalTotal()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 주문 내역 조회 성공")
    void getOrders_Success() {
        // Given
        ArtistOrderResponse.Summary summary = new ArtistOrderResponse.Summary(156, 8, 12, 142, 136, 5);

        List<ArtistOrderResponse.Order> content = List.of(
                createMockOrder("550e84...000", "0123157", "PENDING"),
                createMockOrder("550e84...001", "0123156", "SHIPPED")
        );

        ArtistOrderResponse.List mockResponse = new ArtistOrderResponse.List(
                summary, content, DEFAULT_PAGE, DEFAULT_SIZE, 156, 8, true, false
        );

        given(artistDashboardService.getOrders(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistOrderResponse.List>> response =
                artistDashboardController.getOrders(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, "orderDate", "DESC");

        // Then
        ArtistOrderResponse.List data = assertSuccessResponse(response, "주문 목록 조회 성공");
        ArtistOrderResponse.Order firstOrder = data.content().getFirst();

        assertAll(
                // 요약 정보 검증
                () -> assertThat(data.summary()).isNotNull(),
                () -> assertThat(data.summary().total()).isPositive(),
                () -> assertThat(data.summary().pending()).isNotNegative(),
                // 주문 내역 검증
                () -> assertThat(data.content()).hasSize(2),
                () -> assertThat(firstOrder.orderNumber()).isNotBlank(),
                () -> assertThat(firstOrder.status()).isNotBlank(),
                () -> assertThat(firstOrder.totalAmount()).isPositive(),
                () -> assertThat(firstOrder.buyer()).isNotNull(),
                () -> assertThat(firstOrder.permissions()).isNotNull(),
                // 페이징 정보 검증
                () -> assertThat(data.page()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.size()).isEqualTo(DEFAULT_SIZE),
                () -> assertThat(data.totalElements()).isPositive(),
                () -> assertThat(data.hasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 주문 내역 조회 - 필터 파라미터 포함")
    void getOrders_WithFilters() {
        // Given
        ArtistOrderResponse.List mockResponse = new ArtistOrderResponse.List(
                new ArtistOrderResponse.Summary(8, 8, 0, 0, 0, 0),
                List.of(createMockOrder("550e84...000", "0123157", "PENDING")),
                DEFAULT_PAGE, DEFAULT_SIZE, 8, 1, false, false
        );

        given(artistDashboardService.getOrders(
                eq(BEARER_TOKEN), eq(DEFAULT_PAGE), eq(DEFAULT_SIZE), eq("PENDING"), eq("0123157"),
                eq("2025-09-01"), eq("2025-09-30"), eq("orderDate"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistOrderResponse.List>> response =
                artistDashboardController.getOrders(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, "PENDING", "0123157",
                        "2025-09-01", "2025-09-30", "orderDate", "DESC");

        // Then
        ArtistOrderResponse.List data = assertSuccessResponse(response, "주문 목록 조회 성공");
        assertAll(
                () -> assertThat(data.content()).hasSize(1),
                () -> assertThat(data.content().getFirst().status()).isEqualTo("PENDING"),
                () -> assertThat(data.summary().pending()).isEqualTo(8),
                () -> assertThat(data.summary().delivered()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 성공")
    void getCancellationRequests_Success() {
        // Given
        ArtistCancellationResponse.CancellationRequest mockRequest = new ArtistCancellationResponse.CancellationRequest(
                1L, "ORD-20241225-001", "0123157", "CANCEL", "PENDING", "처리대기",
                "2024-12-26T09:00:00+09:00", "단순 변심", "취소 요청합니다.",
                new ArtistCancellationResponse.Customer(201L, "고객A"),
                new ArtistCancellationResponse.OrderItem(101L, "귀여운 스티커", 1, 15000),
                15000,
                new ArtistCancellationResponse.Permissions(true, true)
        );

        ArtistCancellationResponse.List mockResponse = new ArtistCancellationResponse.List(
                new ArtistCancellationResponse.Summary(8, 5, 2, 1),
                List.of(mockRequest),
                List.of(),
                DEFAULT_PAGE, DEFAULT_SIZE, 8, 1, false, false
        );

        given(artistDashboardService.getCancellationRequests(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCancellationResponse.List>> response =
                artistDashboardController.getCancellationRequests(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, "requestDate", "DESC");

        // Then
        ArtistCancellationResponse.List data = assertSuccessResponse(response, "취소 요청 목록 조회 성공");
        assertAll(
                () -> assertThat(data.summary().total()).isEqualTo(8),
                () -> assertThat(data.content()).hasSize(1),
                () -> assertThat(data.content().getFirst().requestId()).isEqualTo(1L),
                () -> assertThat(data.content().getFirst().status()).isEqualTo("PENDING")
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 - 필터 파라미터 포함")
    void getCancellationRequests_WithFilters() {
        // Given - 간소화된 Mock 데이터
        ArtistCancellationResponse.CancellationRequest simplifiedRequest =
                new ArtistCancellationResponse.CancellationRequest(
                        1L, null, "ORD-20241225-001", null, "PENDING", null, null, null, null, null, null, 25000,
                        new ArtistCancellationResponse.Permissions(true, true)
                );

        ArtistCancellationResponse.List mockResponse = new ArtistCancellationResponse.List(
                new ArtistCancellationResponse.Summary(5, 5, 0, 0),
                List.of(simplifiedRequest), List.of(),
                DEFAULT_PAGE, DEFAULT_SIZE, 5, 1, false, false
        );

        given(artistDashboardService.getCancellationRequests(
                eq(BEARER_TOKEN), eq(DEFAULT_PAGE), eq(DEFAULT_SIZE), eq("PENDING"), eq("ORD-20241225-001"),
                eq("2024-12-01"), eq("2024-12-31"), eq(101L), eq("requestDate"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCancellationResponse.List>> response =
                artistDashboardController.getCancellationRequests(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, "PENDING", "ORD-20241225-001",
                        "2024-12-01", "2024-12-31", 101L, "requestDate", "DESC");

        // Then
        ArtistCancellationResponse.List data = assertSuccessResponse(response, "취소 요청 목록 조회 성공");
        assertAll(
                () -> assertThat(data.content()).hasSize(1),
                () -> assertThat(data.content().getFirst().status()).isEqualTo("PENDING"),
                () -> assertThat(data.summary().pending()).isEqualTo(5),
                () -> assertThat(data.summary().approved()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 교환 요청 목록 조회 성공")
    void getExchangeRequests_Success() {
        // Given
        ArtistExchangeResponse.ExchangeRequest mockRequest = new ArtistExchangeResponse.ExchangeRequest(
                21L, null, "ORD-20241226-003", null, "PENDING", null, null, null, null, null, null, null, null
        );

        ArtistExchangeResponse.List mockResponse = new ArtistExchangeResponse.List(
                new ArtistExchangeResponse.Summary(5, 3, 0, 0),
                List.of(mockRequest), List.of(),
                DEFAULT_PAGE, DEFAULT_SIZE, 5, 1, false, false
        );

        given(artistDashboardService.getExchangeRequests(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistExchangeResponse.List>> response =
                artistDashboardController.getExchangeRequests(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, "requestDate", "DESC");

        // Then
        ArtistExchangeResponse.List data = assertSuccessResponse(response, "교환 요청 목록 조회 성공");
        assertAll(
                () -> assertThat(data.summary().total()).isEqualTo(5),
                () -> assertThat(data.content()).hasSize(1),
                () -> assertThat(data.content().getFirst().requestId()).isEqualTo(21L),
                () -> assertThat(data.content().getFirst().status()).isEqualTo("PENDING")
        );
    }

    @Test
    @DisplayName("작가 설정 정보 조회 성공")
    void getSettings_Success() {
        // Given
        ArtistSettingsResponse mockResponse = new ArtistSettingsResponse(
                new ArtistSettingsResponse.Profile("작가명입니다", "자신을 소개하는 글을 입력해주세요.", List.of(), null),
                new ArtistSettingsResponse.Business("서울특별시 강남구 테헤란로 123 2층", null, null, true),
                new ArtistSettingsResponse.Payout(null, "신한", null, null, "VERIFIED"),
                new ArtistSettingsResponse.Permissions(true, false, false)
        );

        given(artistDashboardService.getSettings(anyString())).willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistSettingsResponse>> response =
                artistDashboardController.getSettings(BEARER_TOKEN);

        // Then
        ArtistSettingsResponse data = assertSuccessResponse(response, "판매자 설정 조회 성공");
        assertAll(
                () -> assertThat(data.profile().nickname()).isEqualTo("작가명입니다"),
                () -> assertThat(data.business().verified()).isTrue(),
                () -> assertThat(data.payout().status()).isEqualTo("VERIFIED"),
                () -> assertThat(data.permissions().canEditProfile()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 펀딩 목록 조회 성공")
    void getFundings_Success() {
        // Given
        ArtistFundingResponse.Funding mockFunding = new ArtistFundingResponse.Funding(
                456789L, "펀딩 제목입니다", "ACTIVE", 900000, 900000, 100, 0,
                null, null, null, null, null, null, null
        );

        ArtistFundingResponse.List mockResponse = new ArtistFundingResponse.List(
                new ArtistFundingResponse.Summary(15, 0, 0, 0),
                List.of(mockFunding), List.of(), DEFAULT_PAGE, DEFAULT_SIZE, 15, 1, false, false
        );

        given(artistDashboardService.getFundings(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistFundingResponse.List>> response =
                artistDashboardController.getFundings(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then
        ArtistFundingResponse.List data = assertSuccessResponse(response, "내 펀딩 모니터링 조회 성공");
        assertAll(
                () -> assertThat(data.getSummary().totalFundings()).isEqualTo(15),
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().fundingId()).isEqualTo(456789L),
                () -> assertThat(data.getContent().getFirst().status()).isEqualTo("ACTIVE")
        );
    }

    @Test
    @DisplayName("작가 펀딩 목록 조회 - 필터 파라미터 포함")
    void getFundings_WithFilters() {
        // Given
        ArtistFundingResponse.Funding mockFunding = new ArtistFundingResponse.Funding(
                456789L, null, "ACTIVE", 0, 0, 0, 0, null, null, null, null,
                new ArtistFundingResponse.Category(null, "스티커"), null, null
        );

        ArtistFundingResponse.List mockResponse = new ArtistFundingResponse.List(
                new ArtistFundingResponse.Summary(0, 3, 0, 0),
                List.of(mockFunding), List.of(), DEFAULT_PAGE, DEFAULT_SIZE, 3, 1, false, false
        );

        given(artistDashboardService.getFundings(
                eq(BEARER_TOKEN), eq(DEFAULT_PAGE), eq(DEFAULT_SIZE), eq("스티커"), eq("ACTIVE"), eq(1L),
                eq(100), eq(200), eq("2025-08-01"), eq("2025-09-30"), eq("endDate"), eq("ASC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistFundingResponse.List>> response =
                artistDashboardController.getFundings(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, "스티커", "ACTIVE", 1L,
                        100, 200, "2025-08-01", "2025-09-30", "endDate", "ASC");

        // Then
        ArtistFundingResponse.List data = assertSuccessResponse(response, "내 펀딩 모니터링 조회 성공");
        assertAll(
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().status()).isEqualTo("ACTIVE"),
                () -> assertThat(data.getSummary().activeFundings()).isEqualTo(3)
        );
    }
}
