package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistExchangeResponse DTO 테스트
 * 교환 요청과 페이징 로직에 집중
 * 2025.09.24 생성
 */
@DisplayName("ArtistExchangeResponse DTO 테스트")
public class ArtistExchangeResponseTest {

    @Test
    @DisplayName("교환 요청 정보 구조 생성 및 검증")
    void createExchangeRequest_Success() {
        // When
        ArtistExchangeResponse.ExchangeRequest request = createSampleExchangeRequest();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(request).isNotNull(),
                () -> assertThat(request.getRequestId()).isPositive(),
                () -> assertThat(request.getOrderNumber()).isNotBlank(),
                () -> assertThat(request.getStatus()).isNotBlank(),
                () -> assertThat(request.getCustomer()).isNotNull(),
                () -> assertThat(request.getOrderItem()).isNotNull(),
                () -> assertThat(request.getExchangeRequested()).isNotNull(),
                () -> assertThat(request.getPermissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("교환 요청 상태별 권한 검증")
    void validateExchangeStatuses_Success() {
        // Given
        List<ArtistExchangeResponse.ExchangeRequest> requests = Arrays.asList(
                createRequestWithStatus("PENDING", true, true),
                createRequestWithStatus("APPROVED", false, false),
                createRequestWithStatus("REJECTED", false, false)
        );

        // Then - 상태별 권한 검증
        assertAll(
                () -> assertThat(requests.get(0).getStatus()).isEqualTo("PENDING"),
                () -> assertThat(requests.get(0).getPermissions().isCanApprove()).isTrue(),
                () -> assertThat(requests.get(0).getPermissions().isCanReject()).isTrue(),
                () -> assertThat(requests.get(1).getStatus()).isEqualTo("APPROVED"),
                () -> assertThat(requests.get(1).getPermissions().isCanApprove()).isFalse(),
                () -> assertThat(requests.get(2).getStatus()).isEqualTo("REJECTED"),
                () -> assertThat(requests.get(2).getPermissions().isCanReject()).isFalse()
        );
    }

    @Test
    @DisplayName("교환 요청 목록 페이징 구조 검증")
    void validatePagination_Success() {
        // Given
        List<ArtistExchangeResponse.ExchangeRequest> requests = Arrays.asList(
                createSampleExchangeRequest(),
                createSampleExchangeRequest()
        );

        // When
        ArtistExchangeResponse.List response = ArtistExchangeResponse.List.builder()
                .summary(createSampleSummary())
                .content(requests)
                .bulkActions(createSampleBulkActions())
                .page(0).size(20).totalElements(5).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        // Then - 페이징 로직 검증
        assertAll(
                () -> assertThat(response.getContent()).hasSize(2),
                () -> assertThat(response.getPage()).isNotNegative(),
                () -> assertThat(response.getSize()).isPositive(),
                () -> assertThat(response.getTotalElements()).isNotNegative(),
                () -> assertThat(response.getTotalPages()).isPositive(),
                () -> assertThat(response.isHasNext()).isFalse(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("교환 요청 통계 비즈니스 로직 검증")
    void validateSummaryBusinessLogic_Success() {
        // When
        ArtistExchangeResponse.Summary summary = createSampleSummary();

        // Then - 핵심 비즈니스 규칙 검증
        assertAll(
                // 통계 일관성: 각 상태별 합계가 전체와 일치
                () -> assertThat(summary.getTotal()).isEqualTo(5),
                () -> assertThat(summary.getPending() + summary.getApproved() + summary.getRejected())
                        .isEqualTo(summary.getTotal()),
                // 각 상태별 수량은 음수가 아니어야 함
                () -> assertThat(summary.getPending()).isNotNegative(),
                () -> assertThat(summary.getApproved()).isNotNegative(),
                () -> assertThat(summary.getRejected()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistExchangeResponse.List response = ArtistExchangeResponse.List.builder()
                .summary(ArtistExchangeResponse.Summary.builder()
                        .total(5).pending(3).approved(1).rejected(1)
                        .build())
                .content(Arrays.asList(
                        ArtistExchangeResponse.ExchangeRequest.builder()
                                .requestId(21L)
                                .orderNumber("ORD-20241226-003")
                                .status("PENDING")
                                .customer(ArtistExchangeResponse.Customer.builder()
                                        .id(204L).nickname("honggildong")
                                        .build())
                                .orderItem(ArtistExchangeResponse.OrderItem.builder()
                                        .productId(103L).productName("상품명입니다")
                                        .quantity(1).price(28500)
                                        .build())
                                .exchangeRequested(ArtistExchangeResponse.ExchangeRequested.builder()
                                        .option("색상=그린").quantity(1)
                                        .build())
                                .permissions(ArtistExchangeResponse.Permissions.builder()
                                        .canApprove(true).canReject(true)
                                        .build())
                                .build()
                ))
                .bulkActions(Arrays.asList(
                        ArtistExchangeResponse.BulkAction.builder()
                                .action("EXCHANGE_APPROVE").label("교환 승인")
                                .build()
                ))
                .page(0).size(10).totalElements(5).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.getSummary().getTotal()).isEqualTo(5),
                () -> assertThat(response.getContent().get(0).getOrderNumber()).isEqualTo("ORD-20241226-003"),
                () -> assertThat(response.getContent().get(0).getCustomer().getNickname()).isEqualTo("honggildong"),
                () -> assertThat(response.getContent().get(0).getExchangeRequested().getOption()).isEqualTo("색상=그린"),
                () -> assertThat(response.getContent().get(0).getPermissions().isCanApprove()).isTrue(),
                () -> assertThat(response.getBulkActions().get(0).getAction()).isEqualTo("EXCHANGE_APPROVE")
        );
    }

    // -------------- 헬퍼 메서드들 ----------------

    private ArtistExchangeResponse.ExchangeRequest createSampleExchangeRequest() {
        return ArtistExchangeResponse.ExchangeRequest.builder()
                .requestId(21L)
                .orderNumber("ORD-20241226-003")
                .status("PENDING")
                .customer(ArtistExchangeResponse.Customer.builder()
                        .id(204L).nickname("honggildong")
                        .build())
                .orderItem(ArtistExchangeResponse.OrderItem.builder()
                        .productId(103L).productName("상품명입니다")
                        .quantity(1).price(28500)
                        .build())
                .exchangeRequested(ArtistExchangeResponse.ExchangeRequested.builder()
                        .option("색상=그린").quantity(1)
                        .build())
                .permissions(ArtistExchangeResponse.Permissions.builder()
                        .canApprove(true).canReject(true)
                        .build())
                .build();
    }

    private ArtistExchangeResponse.ExchangeRequest createRequestWithStatus(String status, boolean canApprove, boolean canReject) {
        return ArtistExchangeResponse.ExchangeRequest.builder()
                .requestId(21L)
                .orderNumber("ORD-" + status)
                .status(status)
                .customer(ArtistExchangeResponse.Customer.builder()
                        .id(204L).nickname("고객명")
                        .build())
                .orderItem(ArtistExchangeResponse.OrderItem.builder()
                        .productId(103L).productName("상품명")
                        .quantity(1).price(28500)
                        .build())
                .exchangeRequested(ArtistExchangeResponse.ExchangeRequested.builder()
                        .option("색상=그린").quantity(1)
                        .build())
                .permissions(ArtistExchangeResponse.Permissions.builder()
                        .canApprove(canApprove).canReject(canReject)
                        .build())
                .build();
    }

    private ArtistExchangeResponse.Summary createSampleSummary() {
        return ArtistExchangeResponse.Summary.builder()
                .total(5).pending(3).approved(1).rejected(1)
                .build();
    }

    private List<ArtistExchangeResponse.BulkAction> createSampleBulkActions() {
        return Arrays.asList(
                ArtistExchangeResponse.BulkAction.builder()
                        .action("EXCHANGE_APPROVE").label("교환 승인").requiresConfirmation(true)
                        .build(),
                ArtistExchangeResponse.BulkAction.builder()
                        .action("EXCHANGE_REJECT").label("교환 거절").requiresConfirmation(true)
                        .build()
        );
    }
}
