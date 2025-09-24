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
        return ArtistCashResponse.Balance.builder()
                .currentBalance(72000)
                .pendingSettlement(15000)
                .pendingWithdrawal(0)
                .withdrawable(72000)
                .currency("KRW")
                .updatedAt(LocalDateTime.of(2025, 9, 24, 10, 0))
                .build();
    }

    private ArtistProductResponse.Product createMockProduct() {
        return ArtistProductResponse.Product.builder()
                .productNumber("0123157")
                .productName("상품명입니다 상품명입니다")
                .price(90000)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025. 09. 18")
                .build();
    }

    private ArtistCashHistoryResponse.Transaction createMockTransaction(String type, String txId, int amount) {
        return ArtistCashHistoryResponse.Transaction.builder()
                .txId(txId)
                .transactedAt("2025-09-24T09:12:00+09:00")
                .type(type)
                .typeText(type.equals("DEPOSIT") ? "정산금 입금" : "모리캐시 환전")
                .depositAmount(type.equals("DEPOSIT") ? amount : 0)
                .withdrawalAmount(type.equals("WITHDRAWAL") ? amount : 0)
                .balanceAfter(amount)
                .method(type.equals("DEPOSIT") ? "WALLET" : "BANK_TRANSFER")
                .methodText(type.equals("DEPOSIT") ? "모리캐시" : "계좌이체")
                .status("COMPLETED")
                .note(type.equals("WITHDRAWAL") ? "우리은행 ****-**-***** 홍길동" : null)
                .build();
    }

    private ArtistOrderResponse.Order createMockOrder(String orderId, String orderNumber, String status) {
        return ArtistOrderResponse.Order.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .orderDate("2025-09-18")
                .status(status)
                .statusText(status.equals("PENDING") ? "발주 전" : "배송 중")
                .totalAmount(47500)
                .productSummary("상품명입니다 상품명입니다")
                .itemCount(2)
                .buyer(ArtistOrderResponse.Buyer.builder()
                        .id(201L)
                        .nickname("heroeson02")
                        .name("손경호")
                        .build())
                .shipment(ArtistOrderResponse.Shipment.builder()
                        .status("READY")
                        .trackingNo(null)
                        .shippingCompany(null)
                        .build())
                .permissions(ArtistOrderResponse.Permissions.builder()
                        .canChangeStatus(true)
                        .canCancel(true)
                        .build())
                .build();
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
                () -> assertThat(data.getCurrentBalance()).isEqualTo(72000),
                () -> assertThat(data.getPendingSettlement()).isEqualTo(15000),
                () -> assertThat(data.getPendingWithdrawal()).isEqualTo(0),
                () -> assertThat(data.getWithdrawable()).isEqualTo(72000),
                () -> assertThat(data.getCurrency()).isEqualTo("KRW")
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
                () -> assertThat(firstProduct.getProductNumber()).isEqualTo("0123157"),
                () -> assertThat(firstProduct.getProductName()).isEqualTo("상품명입니다 상품명입니다"),
                () -> assertThat(firstProduct.getPrice()).isEqualTo(90000),
                () -> assertThat(firstProduct.getSellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(firstProduct.getStatusText()).isEqualTo("판매중"),
                () -> assertThat(data.getPage()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.getSize()).isEqualTo(10),
                () -> assertThat(data.getTotalElements()).isEqualTo(28)
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

        List<ArtistCashHistoryResponse.Transaction> content = List.of(
                createMockTransaction("DEPOSIT", "TX-20250924-0001", 10000),
                createMockTransaction("WITHDRAWAL", "TX-20250923-0004", 64000)
        );

        ArtistCashHistoryResponse.List mockResponse = ArtistCashHistoryResponse.List.builder()
                .summary(summary)
                .content(content)
                .page(DEFAULT_PAGE)
                .size(DEFAULT_SIZE)
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
                artistDashboardController.getCashHistory(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, "transactedAt", "DESC");

        // Then
        ArtistCashHistoryResponse.List data = assertSuccessResponse(response, "입금/환전 내역 조회 성공");
        ArtistCashHistoryResponse.Transaction firstTransaction = data.getContent().getFirst();

        assertAll(
                // 요약 정보 검증
                () -> assertThat(data.getSummary().getPeriodDepositTotal()).isEqualTo(74000),
                () -> assertThat(data.getSummary().getPeriodWithdrawalTotal()).isEqualTo(64000),
                () -> assertThat(data.getSummary().getPeriodNet()).isEqualTo(10000),
                // 거래 내역 검증
                () -> assertThat(data.getContent()).hasSize(2),
                () -> assertThat(firstTransaction.getTxId()).isEqualTo("TX-20250924-0001"),
                () -> assertThat(firstTransaction.getType()).isEqualTo("DEPOSIT"),
                () -> assertThat(firstTransaction.getDepositAmount()).isEqualTo(10000),
                () -> assertThat(firstTransaction.getBalanceAfter()).isEqualTo(10000),
                // 페이징 정보 검증
                () -> assertThat(data.getPage()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.getSize()).isEqualTo(DEFAULT_SIZE),
                () -> assertThat(data.getTotalElements()).isEqualTo(2),
                () -> assertThat(data.isHasNext()).isFalse()
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
                .content(List.of(createMockTransaction("DEPOSIT", "TX-20250924-0001", 10000)))
                .page(DEFAULT_PAGE)
                .size(DEFAULT_SIZE)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

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
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getType()).isEqualTo("DEPOSIT"),
                () -> assertThat(data.getSummary().getPeriodWithdrawalTotal()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 주문 내역 조회 성공")
    void getOrders_Success() {
        // Given
        ArtistOrderResponse.Summary summary = ArtistOrderResponse.Summary.builder()
                .total(156).pending(8).preparing(12)
                .shipped(142).delivered(136).canceled(5)
                .build();

        List<ArtistOrderResponse.Order> content = List.of(
                createMockOrder("550e84...000", "0123157", "PENDING"),
                createMockOrder("550e84...001", "0123156", "SHIPPED")
        );

        ArtistOrderResponse.List mockResponse = ArtistOrderResponse.List.builder()
                .summary(summary)
                .content(content)
                .page(DEFAULT_PAGE)
                .size(DEFAULT_SIZE)
                .totalElements(156)
                .totalPages(8)
                .hasNext(true)
                .hasPrevious(false)
                .build();

        given(artistDashboardService.getOrders(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistOrderResponse.List>> response =
                artistDashboardController.getOrders(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, "orderDate", "DESC");

        // Then
        ArtistOrderResponse.List data = assertSuccessResponse(response, "주문 목록 조회 성공");
        ArtistOrderResponse.Order firstOrder = data.getContent().getFirst();

        assertAll(
                // 요약 정보 검증
                () -> assertThat(data.getSummary().getTotal()).isEqualTo(156),
                () -> assertThat(data.getSummary().getPending()).isEqualTo(8),
                () -> assertThat(data.getSummary().getDelivered()).isEqualTo(136),
                // 주문 내역 검증
                () -> assertThat(data.getContent()).hasSize(2),
                () -> assertThat(firstOrder.getOrderNumber()).isEqualTo("0123157"),
                () -> assertThat(firstOrder.getStatus()).isEqualTo("PENDING"),
                () -> assertThat(firstOrder.getTotalAmount()).isEqualTo(47500),
                () -> assertThat(firstOrder.getBuyer().getNickname()).isEqualTo("heroeson02"),
                () -> assertThat(firstOrder.getPermissions().isCanChangeStatus()).isTrue(),
                // 페이징 정보 검증
                () -> assertThat(data.getPage()).isEqualTo(DEFAULT_PAGE),
                () -> assertThat(data.getSize()).isEqualTo(DEFAULT_SIZE),
                () -> assertThat(data.getTotalElements()).isEqualTo(156),
                () -> assertThat(data.isHasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 주문 내역 조회 - 필터 파라미터 포함")
    void getOrders_WithFilters() {
        // Given
        ArtistOrderResponse.List mockResponse = ArtistOrderResponse.List.builder()
                .summary(ArtistOrderResponse.Summary.builder()
                        .total(8).pending(8).preparing(0)
                        .shipped(0).delivered(0).canceled(0)
                        .build())
                .content(List.of(createMockOrder("550e84...000", "0123157", "PENDING")))
                .page(DEFAULT_PAGE).size(DEFAULT_SIZE).totalElements(8).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

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
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("PENDING"),
                () -> assertThat(data.getSummary().getPending()).isEqualTo(8),
                () -> assertThat(data.getSummary().getDelivered()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 성공")
    void getCancellationRequests_Success() {
        // Given
        ArtistCancellationResponse.CancellationRequest mockRequest =
                ArtistCancellationResponse.CancellationRequest.builder()
                        .requestId(1L)
                        .orderNumber("ORD-20241225-001")
                        .status("PENDING")
                        .refundAmount(25000)
                        .build();

        ArtistCancellationResponse.List mockResponse = ArtistCancellationResponse.List.builder()
                .summary(ArtistCancellationResponse.Summary.builder().total(8).pending(5).build())
                .content(List.of(mockRequest))
                .bulkActions(List.of())
                .page(DEFAULT_PAGE)
                .size(DEFAULT_SIZE)
                .totalElements(8)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        given(artistDashboardService.getCancellationRequests(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCancellationResponse.List>> response =
                artistDashboardController.getCancellationRequests(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, "requestDate", "DESC");

        // Then
        ArtistCancellationResponse.List data = assertSuccessResponse(response, "취소 요청 목록 조회 성공");
        assertAll(
                () -> assertThat(data.getSummary().getTotal()).isEqualTo(8),
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getRequestId()).isEqualTo(1L),
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("PENDING")
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 - 필터 파라미터 포함")
    void getCancellationRequests_WithFilters() {
        // Given - 간소화된 Mock 데이터
        ArtistCancellationResponse.CancellationRequest simplifiedRequest =
                ArtistCancellationResponse.CancellationRequest.builder()
                        .requestId(1L)
                        .orderNumber("ORD-20241225-001")
                        .status("PENDING")
                        .refundAmount(25000)
                        .permissions(ArtistCancellationResponse.Permissions.builder()
                                .canApprove(true).canReject(true)
                                .build())
                        .build();

        ArtistCancellationResponse.List mockResponse = ArtistCancellationResponse.List.builder()
                .summary(ArtistCancellationResponse.Summary.builder()
                        .total(5).pending(5).approved(0).rejected(0)
                        .build())
                .content(List.of(simplifiedRequest))
                .bulkActions(List.of())
                .page(DEFAULT_PAGE).size(DEFAULT_SIZE).totalElements(5).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

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
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("PENDING"),
                () -> assertThat(data.getSummary().getPending()).isEqualTo(5),
                () -> assertThat(data.getSummary().getApproved()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 교환 요청 목록 조회 성공")
    void getExchangeRequests_Success() {
        // Given
        ArtistExchangeResponse.ExchangeRequest mockRequest = ArtistExchangeResponse.ExchangeRequest.builder()
                .requestId(21L)
                .orderNumber("ORD-20241226-003")
                .status("PENDING")
                .build();

        ArtistExchangeResponse.List mockResponse = ArtistExchangeResponse.List.builder()
                .summary(ArtistExchangeResponse.Summary.builder().total(5).pending(3).build())
                .content(List.of(mockRequest))
                .bulkActions(List.of())
                .page(DEFAULT_PAGE).size(DEFAULT_SIZE).totalElements(5).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        given(artistDashboardService.getExchangeRequests(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistExchangeResponse.List>> response =
                artistDashboardController.getExchangeRequests(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, "requestDate", "DESC");

        // Then
        ArtistExchangeResponse.List data = assertSuccessResponse(response, "교환 요청 목록 조회 성공");
        assertAll(
                () -> assertThat(data.getSummary().getTotal()).isEqualTo(5),
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getRequestId()).isEqualTo(21L),
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("PENDING")
        );
    }

    @Test
    @DisplayName("작가 설정 정보 조회 성공")
    void getSettings_Success() {
        // Given
        ArtistSettingsResponse mockResponse = ArtistSettingsResponse.builder()
                .profile(ArtistSettingsResponse.Profile.builder()
                        .nickname("작가명입니다")
                        .bio("자신을 소개하는 글을 입력해주세요.")
                        .sns(List.of())
                        .build())
                .business(ArtistSettingsResponse.Business.builder()
                        .address("서울특별시 강남구 테헤란로 123 2층")
                        .verified(true)
                        .build())
                .payout(ArtistSettingsResponse.Payout.builder()
                        .bankName("신한")
                        .status("VERIFIED")
                        .build())
                .permissions(ArtistSettingsResponse.Permissions.builder()
                        .canEditProfile(true)
                        .build())
                .build();

        given(artistDashboardService.getSettings(anyString())).willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistSettingsResponse>> response =
                artistDashboardController.getSettings(BEARER_TOKEN);

        // Then
        ArtistSettingsResponse data = assertSuccessResponse(response, "판매자 설정 조회 성공");
        assertAll(
                () -> assertThat(data.getProfile().getNickname()).isEqualTo("작가명입니다"),
                () -> assertThat(data.getBusiness().isVerified()).isTrue(),
                () -> assertThat(data.getPayout().getStatus()).isEqualTo("VERIFIED"),
                () -> assertThat(data.getPermissions().isCanEditProfile()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 펀딩 목록 조회 성공")
    void getFundings_Success() {
        // Given
        ArtistFundingResponse.Funding mockFunding = ArtistFundingResponse.Funding.builder()
                .fundingId(456789L)
                .title("펀딩 제목입니다")
                .status("ACTIVE")
                .targetAmount(900000)
                .currentAmount(900000)
                .achievementRate(100)
                .build();

        ArtistFundingResponse.List mockResponse = new ArtistFundingResponse.List(
                ArtistFundingResponse.Summary.builder().totalFundings(15).build(),
                List.of(mockFunding), List.of(), DEFAULT_PAGE, DEFAULT_SIZE, 15, 1, false, false);

        given(artistDashboardService.getFundings(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistFundingResponse.List>> response =
                artistDashboardController.getFundings(BEARER_TOKEN, DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null, null, null, "endDate", "ASC");

        // Then
        ArtistFundingResponse.List data = assertSuccessResponse(response, "내 펀딩 모니터링 조회 성공");
        assertAll(
                () -> assertThat(data.getSummary().getTotalFundings()).isEqualTo(15),
                () -> assertThat(data.getContent()).hasSize(1),
                () -> assertThat(data.getContent().getFirst().getFundingId()).isEqualTo(456789L),
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("ACTIVE")
        );
    }

    @Test
    @DisplayName("작가 펀딩 목록 조회 - 필터 파라미터 포함")
    void getFundings_WithFilters() {
        // Given
        ArtistFundingResponse.Funding mockFunding = ArtistFundingResponse.Funding.builder()
                .fundingId(456789L)
                .status("ACTIVE")
                .category(ArtistFundingResponse.Category.builder().name("스티커").build())
                .build();

        ArtistFundingResponse.List mockResponse = new ArtistFundingResponse.List(
                ArtistFundingResponse.Summary.builder().activeFundings(3).build(),
                List.of(mockFunding), List.of(), DEFAULT_PAGE, DEFAULT_SIZE, 3, 1, false, false);

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
                () -> assertThat(data.getContent().getFirst().getStatus()).isEqualTo("ACTIVE"),
                () -> assertThat(data.getSummary().getActiveFundings()).isEqualTo(3)
        );
    }
}
