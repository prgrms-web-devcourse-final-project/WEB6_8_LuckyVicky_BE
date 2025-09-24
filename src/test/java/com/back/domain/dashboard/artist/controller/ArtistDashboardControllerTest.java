package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistOrderResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistExchangeResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse;
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

    @Test
    @DisplayName("작가 주문 내역 조회 성공")
    void getOrders_Success() {
        // Given
        ArtistOrderResponse.Summary summary = ArtistOrderResponse.Summary.builder()
                .total(156).pending(8).preparing(12)
                .shipped(142).delivered(136).canceled(5)
                .build();

        List<ArtistOrderResponse.Order> content = Arrays.asList(
                ArtistOrderResponse.Order.builder()
                        .orderId("550e84...000")
                        .orderNumber("0123157")
                        .orderDate("2025-09-18")
                        .status("PENDING")
                        .statusText("발주 전")
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
                        .build(),
                ArtistOrderResponse.Order.builder()
                        .orderId("550e84...001")
                        .orderNumber("0123156")
                        .orderDate("2025-09-17")
                        .status("SHIPPED")
                        .statusText("배송 중")
                        .totalAmount(25000)
                        .productSummary("아트 프린트 모음집")
                        .itemCount(1)
                        .buyer(ArtistOrderResponse.Buyer.builder()
                                .id(202L)
                                .nickname("artlover")
                                .name("김아트")
                                .build())
                        .shipment(ArtistOrderResponse.Shipment.builder()
                                .status("SHIPPED")
                                .trackingNo("123456789012")
                                .shippingCompany("한진택배")
                                .build())
                        .permissions(ArtistOrderResponse.Permissions.builder()
                                .canChangeStatus(false)
                                .canCancel(false)
                                .build())
                        .build()
        );

        ArtistOrderResponse.List mockResponse = ArtistOrderResponse.List.builder()
                .summary(summary)
                .content(content)
                .page(0)
                .size(20)
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
                artistDashboardController.getOrders(BEARER_TOKEN, 0, 20, null, null, null, null, "orderDate", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistOrderResponse.List> body = response.getBody();
        ArtistOrderResponse.Order firstOrder = body.data().getContent().get(0);

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("주문 목록 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                // 요약 정보 검증
                () -> assertThat(body.data().getSummary().getTotal()).isEqualTo(156),
                () -> assertThat(body.data().getSummary().getPending()).isEqualTo(8),
                () -> assertThat(body.data().getSummary().getDelivered()).isEqualTo(136),
                // 주문 내역 검증
                () -> assertThat(body.data().getContent()).hasSize(2),
                () -> assertThat(firstOrder.getOrderNumber()).isEqualTo("0123157"),
                () -> assertThat(firstOrder.getStatus()).isEqualTo("PENDING"),
                () -> assertThat(firstOrder.getTotalAmount()).isEqualTo(47500),
                () -> assertThat(firstOrder.getBuyer().getNickname()).isEqualTo("heroeson02"),
                () -> assertThat(firstOrder.getPermissions().isCanChangeStatus()).isTrue(),
                // 페이징 정보 검증
                () -> assertThat(body.data().getPage()).isEqualTo(0),
                () -> assertThat(body.data().getSize()).isEqualTo(20),
                () -> assertThat(body.data().getTotalElements()).isEqualTo(156),
                () -> assertThat(body.data().isHasNext()).isTrue()
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
                .content(Arrays.asList(
                        ArtistOrderResponse.Order.builder()
                                .orderId("550e84...000")
                                .orderNumber("0123157")
                                .status("PENDING")
                                .totalAmount(47500)
                                .buyer(ArtistOrderResponse.Buyer.builder()
                                        .id(201L).nickname("heroeson02").name("손경호")
                                        .build())
                                .permissions(ArtistOrderResponse.Permissions.builder()
                                        .canChangeStatus(true).canCancel(true)
                                        .build())
                                .build()
                ))
                .page(0).size(20).totalElements(8).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        given(artistDashboardService.getOrders(
                eq(BEARER_TOKEN), eq(0), eq(20), eq("PENDING"), eq("0123157"),
                eq("2025-09-01"), eq("2025-09-30"), eq("orderDate"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistOrderResponse.List>> response =
                artistDashboardController.getOrders(BEARER_TOKEN, 0, 20, "PENDING", "0123157",
                        "2025-09-01", "2025-09-30", "orderDate", "DESC");

        // Then - 필터링된 결과 검증
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().getContent()).hasSize(1),
                () -> assertThat(response.getBody().data().getContent().get(0).getStatus()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().getSummary().getPending()).isEqualTo(8),
                () -> assertThat(response.getBody().data().getSummary().getDelivered()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 성공")
    void getCancellationRequests_Success() {
        // Given
        ArtistCancellationResponse.Summary summary = ArtistCancellationResponse.Summary.builder()
                .total(8).pending(5).approved(2).rejected(1)
                .build();

        List<ArtistCancellationResponse.CancellationRequest> content = Arrays.asList(
                ArtistCancellationResponse.CancellationRequest.builder()
                        .requestId(1L)
                        .orderId("550e84...000")
                        .orderNumber("ORD-20241225-001")
                        .type("CANCEL")
                        .status("PENDING")
                        .statusText("처리대기")
                        .requestDate("2024-12-26T09:00:00+09:00")
                        .reason("단순 변심")
                        .customerMessage("사이즈가 맞지 않아서 취소하고 싶습니다.")
                        .customer(ArtistCancellationResponse.Customer.builder()
                                .id(201L)
                                .nickname("고객명")
                                .build())
                        .orderItem(ArtistCancellationResponse.OrderItem.builder()
                                .productId(101L)
                                .productName("귀여운 고양이 스티커")
                                .quantity(2)
                                .price(12500)
                                .build())
                        .refundAmount(25000)
                        .permissions(ArtistCancellationResponse.Permissions.builder()
                                .canApprove(true)
                                .canReject(true)
                                .build())
                        .build()
        );

        List<ArtistCancellationResponse.BulkAction> bulkActions = Arrays.asList(
                ArtistCancellationResponse.BulkAction.builder()
                        .action("CANCEL_APPROVE")
                        .label("취소 승인")
                        .requiresConfirmation(true)
                        .build(),
                ArtistCancellationResponse.BulkAction.builder()
                        .action("CANCEL_REJECT")
                        .label("취소 거절")
                        .requiresConfirmation(true)
                        .build()
        );

        ArtistCancellationResponse.List mockResponse = ArtistCancellationResponse.List.builder()
                .summary(summary)
                .content(content)
                .bulkActions(bulkActions)
                .page(0)
                .size(20)
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
                artistDashboardController.getCancellationRequests(BEARER_TOKEN, 0, 20, null, null, null, null, null, "requestDate", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistCancellationResponse.List> body = response.getBody();
        ArtistCancellationResponse.CancellationRequest firstRequest = body.data().getContent().get(0);

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("취소 요청 목록 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                // 요약 정보 검증
                () -> assertThat(body.data().getSummary().getTotal()).isEqualTo(8),
                () -> assertThat(body.data().getSummary().getPending()).isEqualTo(5),
                () -> assertThat(body.data().getSummary().getApproved()).isEqualTo(2),
                () -> assertThat(body.data().getSummary().getRejected()).isEqualTo(1),
                // 취소 요청 검증
                () -> assertThat(body.data().getContent()).hasSize(1),
                () -> assertThat(firstRequest.getRequestId()).isEqualTo(1L),
                () -> assertThat(firstRequest.getOrderNumber()).isEqualTo("ORD-20241225-001"),
                () -> assertThat(firstRequest.getStatus()).isEqualTo("PENDING"),
                () -> assertThat(firstRequest.getRefundAmount()).isEqualTo(25000),
                () -> assertThat(firstRequest.getCustomer().getNickname()).isEqualTo("고객명"),
                () -> assertThat(firstRequest.getOrderItem().getProductName()).isEqualTo("귀여운 고양이 스티커"),
                () -> assertThat(firstRequest.getPermissions().isCanApprove()).isTrue(),
                // 일괄 작업 검증
                () -> assertThat(body.data().getBulkActions()).hasSize(2),
                () -> assertThat(body.data().getBulkActions().get(0).getAction()).isEqualTo("CANCEL_APPROVE"),
                () -> assertThat(body.data().getBulkActions().get(1).getAction()).isEqualTo("CANCEL_REJECT"),
                // 페이징 정보 검증
                () -> assertThat(body.data().getPage()).isEqualTo(0),
                () -> assertThat(body.data().getTotalElements()).isEqualTo(8),
                () -> assertThat(body.data().isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("작가 취소 요청 목록 조회 - 필터 파라미터 포함")
    void getCancellationRequests_WithFilters() {
        // Given
        ArtistCancellationResponse.List mockResponse = ArtistCancellationResponse.List.builder()
                .summary(ArtistCancellationResponse.Summary.builder()
                        .total(5).pending(5).approved(0).rejected(0)
                        .build())
                .content(Arrays.asList(
                        ArtistCancellationResponse.CancellationRequest.builder()
                                .requestId(1L)
                                .orderNumber("ORD-20241225-001")
                                .status("PENDING")
                                .refundAmount(25000)
                                .permissions(ArtistCancellationResponse.Permissions.builder()
                                        .canApprove(true).canReject(true)
                                        .build())
                                .build()
                ))
                .bulkActions(Arrays.asList())
                .page(0).size(20).totalElements(5).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        given(artistDashboardService.getCancellationRequests(
                eq(BEARER_TOKEN), eq(0), eq(20), eq("PENDING"), eq("ORD-20241225-001"),
                eq("2024-12-01"), eq("2024-12-31"), eq(101L), eq("requestDate"), eq("DESC")))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistCancellationResponse.List>> response =
                artistDashboardController.getCancellationRequests(BEARER_TOKEN, 0, 20, "PENDING", "ORD-20241225-001",
                        "2024-12-01", "2024-12-31", 101L, "requestDate", "DESC");

        // Then - 필터링된 결과 검증
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().getContent()).hasSize(1),
                () -> assertThat(response.getBody().data().getContent().get(0).getStatus()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().getSummary().getPending()).isEqualTo(5),
                () -> assertThat(response.getBody().data().getSummary().getApproved()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("작가 교환 요청 목록 조회 성공")
    void getExchangeRequests_Success() {
        // Given
        ArtistExchangeResponse.Summary summary = ArtistExchangeResponse.Summary.builder()
                .total(5).pending(3).approved(1).rejected(1)
                .build();

        List<ArtistExchangeResponse.ExchangeRequest> content = Arrays.asList(
                ArtistExchangeResponse.ExchangeRequest.builder()
                        .requestId(21L)
                        .orderId("550e84...111")
                        .orderNumber("ORD-20241226-003")
                        .type("EXCHANGE")
                        .status("PENDING")
                        .statusText("처리대기")
                        .requestDate("2024-12-26T11:10:00+09:00")
                        .reason("불량 의심")
                        .customerMessage("찍힘 자국이 있어요.")
                        .customer(ArtistExchangeResponse.Customer.builder()
                                .id(204L)
                                .nickname("honggildong")
                                .build())
                        .orderItem(ArtistExchangeResponse.OrderItem.builder()
                                .productId(103L)
                                .productName("상품명입니다")
                                .quantity(1)
                                .price(28500)
                                .build())
                        .exchangeRequested(ArtistExchangeResponse.ExchangeRequested.builder()
                                .option("색상=그린")
                                .quantity(1)
                                .build())
                        .permissions(ArtistExchangeResponse.Permissions.builder()
                                .canApprove(true)
                                .canReject(true)
                                .build())
                        .build()
        );

        List<ArtistExchangeResponse.BulkAction> bulkActions = Arrays.asList(
                ArtistExchangeResponse.BulkAction.builder()
                        .action("EXCHANGE_APPROVE")
                        .label("교환 승인")
                        .requiresConfirmation(true)
                        .build(),
                ArtistExchangeResponse.BulkAction.builder()
                        .action("EXCHANGE_REJECT")
                        .label("교환 거절")
                        .requiresConfirmation(true)
                        .build()
        );

        ArtistExchangeResponse.List mockResponse = ArtistExchangeResponse.List.builder()
                .summary(summary)
                .content(content)
                .bulkActions(bulkActions)
                .page(0)
                .size(20)
                .totalElements(5)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        given(artistDashboardService.getExchangeRequests(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistExchangeResponse.List>> response =
                artistDashboardController.getExchangeRequests(BEARER_TOKEN, 0, 20, null, null, null, null, null, "requestDate", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistExchangeResponse.List> body = response.getBody();
        ArtistExchangeResponse.ExchangeRequest firstRequest = body.data().getContent().get(0);

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("교환 요청 목록 조회 성공"),
                () -> assertThat(body.data()).isNotNull(),
                // 요약 정보 검증
                () -> assertThat(body.data().getSummary().getTotal()).isEqualTo(5),
                () -> assertThat(body.data().getSummary().getPending()).isEqualTo(3),
                () -> assertThat(body.data().getSummary().getApproved()).isEqualTo(1),
                () -> assertThat(body.data().getSummary().getRejected()).isEqualTo(1),
                // 교환 요청 검증
                () -> assertThat(body.data().getContent()).hasSize(1),
                () -> assertThat(firstRequest.getRequestId()).isEqualTo(21L),
                () -> assertThat(firstRequest.getOrderNumber()).isEqualTo("ORD-20241226-003"),
                () -> assertThat(firstRequest.getStatus()).isEqualTo("PENDING"),
                () -> assertThat(firstRequest.getCustomer().getNickname()).isEqualTo("honggildong"),
                () -> assertThat(firstRequest.getOrderItem().getProductName()).isEqualTo("상품명입니다"),
                () -> assertThat(firstRequest.getExchangeRequested().getOption()).isEqualTo("색상=그린"),
                () -> assertThat(firstRequest.getPermissions().isCanApprove()).isTrue(),
                // 일괄 작업 검증
                () -> assertThat(body.data().getBulkActions()).hasSize(2),
                () -> assertThat(body.data().getBulkActions().get(0).getAction()).isEqualTo("EXCHANGE_APPROVE"),
                () -> assertThat(body.data().getBulkActions().get(1).getAction()).isEqualTo("EXCHANGE_REJECT"),
                // 페이징 정보 검증
                () -> assertThat(body.data().getPage()).isEqualTo(0),
                () -> assertThat(body.data().getTotalElements()).isEqualTo(5),
                () -> assertThat(body.data().isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("작가 설정 정보 조회 성공")
    void getSettings_Success() {
        // Given
        ArtistSettingsResponse.Profile profile = ArtistSettingsResponse.Profile.builder()
                .nickname("작가명입니다")
                .bio("자신을 소개하는 글을 입력해주세요.")
                .sns(Arrays.asList(
                        ArtistSettingsResponse.Sns.builder()
                                .platform("Instagram")
                                .handle("@mori_official")
                                .build()
                ))
                .profileImageUrl("https://cdn.example.com/u/5/profile.jpg")
                .build();

        ArtistSettingsResponse.Business business = ArtistSettingsResponse.Business.builder()
                .address("서울특별시 강남구 테헤란로 123 2층")
                .businessRegistrationNo("123-45-67890")
                .telemarketingReportNo("2025-서울강남-1234")
                .verified(true)
                .build();

        ArtistSettingsResponse.Payout payout = ArtistSettingsResponse.Payout.builder()
                .bankCode("088")
                .bankName("신한")
                .accountHolder("홍길동")
                .accountMasked("****-****-**3456")
                .status("VERIFIED")
                .build();

        ArtistSettingsResponse.Permissions permissions = ArtistSettingsResponse.Permissions.builder()
                .canEditProfile(true)
                .canEditBusiness(true)
                .canEditPayout(true)
                .build();

        ArtistSettingsResponse mockResponse = ArtistSettingsResponse.builder()
                .profile(profile)
                .business(business)
                .payout(payout)
                .permissions(permissions)
                .build();

        given(artistDashboardService.getSettings(anyString()))
                .willReturn(mockResponse);

        // When
        ResponseEntity<RsData<ArtistSettingsResponse>> response =
                artistDashboardController.getSettings(BEARER_TOKEN);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        RsData<ArtistSettingsResponse> body = response.getBody();
        ArtistSettingsResponse data = body.data();

        assertAll(
                () -> assertThat(body.resultCode()).isEqualTo("200-OK"),
                () -> assertThat(body.msg()).isEqualTo("판매자 설정 조회 성공"),
                () -> assertThat(data).isNotNull(),
                // 프로필 정보 검증
                () -> assertThat(data.getProfile().getNickname()).isEqualTo("작가명입니다"),
                () -> assertThat(data.getProfile().getBio()).isEqualTo("자신을 소개하는 글을 입력해주세요."),
                () -> assertThat(data.getProfile().getSns()).hasSize(1),
                () -> assertThat(data.getProfile().getSns().get(0).getPlatform()).isEqualTo("Instagram"),
                () -> assertThat(data.getProfile().getSns().get(0).getHandle()).isEqualTo("@mori_official"),
                () -> assertThat(data.getProfile().getProfileImageUrl()).isEqualTo("https://cdn.example.com/u/5/profile.jpg"),
                // 사업자 정보 검증
                () -> assertThat(data.getBusiness().getAddress()).isEqualTo("서울특별시 강남구 테헤란로 123 2층"),
                () -> assertThat(data.getBusiness().getBusinessRegistrationNo()).isEqualTo("123-45-67890"),
                () -> assertThat(data.getBusiness().getTelemarketingReportNo()).isEqualTo("2025-서울강남-1234"),
                () -> assertThat(data.getBusiness().isVerified()).isTrue(),
                // 정산 계좌 정보 검증
                () -> assertThat(data.getPayout().getBankCode()).isEqualTo("088"),
                () -> assertThat(data.getPayout().getBankName()).isEqualTo("신한"),
                () -> assertThat(data.getPayout().getAccountHolder()).isEqualTo("홍길동"),
                () -> assertThat(data.getPayout().getAccountMasked()).isEqualTo("****-****-**3456"),
                () -> assertThat(data.getPayout().getStatus()).isEqualTo("VERIFIED"),
                // 권한 정보 검증
                () -> assertThat(data.getPermissions().isCanEditProfile()).isTrue(),
                () -> assertThat(data.getPermissions().isCanEditBusiness()).isTrue(),
                () -> assertThat(data.getPermissions().isCanEditPayout()).isTrue()
        );
    }
}
