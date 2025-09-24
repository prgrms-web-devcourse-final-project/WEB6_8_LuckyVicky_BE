package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistCancellationResponse DTO 테스트
 * 취소 요청과 페이징 로직에 집중
 * 2025.09.23 생성
 */
@DisplayName("ArtistCancellationResponse DTO 테스트")
public class ArtistCancellationResponseTest {

    @Test
    @DisplayName("취소 요청 정보 구조 생성 및 검증")
    void createCancellationRequest_Success() {
        // When
        ArtistCancellationResponse.CancellationRequest request = createSampleCancellationRequest();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(request).isNotNull(),
                () -> assertThat(request.getRequestId()).isPositive(),
                () -> assertThat(request.getOrderNumber()).isNotBlank(),
                () -> assertThat(request.getStatus()).isNotBlank(),
                () -> assertThat(request.getRefundAmount()).isNotNegative(),
                () -> assertThat(request.getCustomer()).isNotNull(),
                () -> assertThat(request.getOrderItem()).isNotNull(),
                () -> assertThat(request.getPermissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("취소 요청 상태별 권한 검증")
    void validateCancellationStatuses_Success() {
        // Given
        List<ArtistCancellationResponse.CancellationRequest> requests = Arrays.asList(
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
    @DisplayName("취소 요청 목록 페이징 구조 검증")
    void validatePagination_Success() {
        // Given
        List<ArtistCancellationResponse.CancellationRequest> requests = Arrays.asList(
                createSampleCancellationRequest(),
                createSampleCancellationRequest()
        );

        // When
        ArtistCancellationResponse.List response = ArtistCancellationResponse.List.builder()
                .summary(createSampleSummary())
                .content(requests)
                .bulkActions(createSampleBulkActions())
                .page(0).size(20).totalElements(8).totalPages(1)
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
    @DisplayName("취소 요청 통계 비즈니스 로직 검증")
    void validateSummaryBusinessLogic_Success() {
        // When
        ArtistCancellationResponse.Summary summary = createSampleSummary();

        // Then - 핵심 비즈니스 규칙 검증
        assertAll(
                // 통계 일관성: 각 상태별 합계가 전체와 일치
                () -> assertThat(summary.getTotal()).isEqualTo(8),
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
        ArtistCancellationResponse.List response = ArtistCancellationResponse.List.builder()
                .summary(ArtistCancellationResponse.Summary.builder()
                        .total(8).pending(5).approved(2).rejected(1)
                        .build())
                .content(Arrays.asList(
                        ArtistCancellationResponse.CancellationRequest.builder()
                                .requestId(1L)
                                .orderNumber("ORD-20241225-001")
                                .status("PENDING")
                                .refundAmount(25000)
                                .customer(ArtistCancellationResponse.Customer.builder()
                                        .id(201L).nickname("고객명")
                                        .build())
                                .orderItem(ArtistCancellationResponse.OrderItem.builder()
                                        .productId(101L).productName("귀여운 고양이 스티커")
                                        .quantity(2).price(12500)
                                        .build())
                                .permissions(ArtistCancellationResponse.Permissions.builder()
                                        .canApprove(true).canReject(true)
                                        .build())
                                .build()
                ))
                .bulkActions(Arrays.asList(
                        ArtistCancellationResponse.BulkAction.builder()
                                .action("CANCEL_APPROVE").label("취소 승인")
                                .build()
                ))
                .page(0).size(10).totalElements(8).totalPages(1)
                .hasNext(false).hasPrevious(false)
                .build();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.getSummary().getTotal()).isEqualTo(8),
                () -> assertThat(response.getContent().get(0).getOrderNumber()).isEqualTo("ORD-20241225-001"),
                () -> assertThat(response.getContent().get(0).getRefundAmount()).isEqualTo(25000),
                () -> assertThat(response.getContent().get(0).getCustomer().getNickname()).isEqualTo("고객명"),
                () -> assertThat(response.getContent().get(0).getPermissions().isCanApprove()).isTrue(),
                () -> assertThat(response.getBulkActions().get(0).getAction()).isEqualTo("CANCEL_APPROVE")
        );
    }

    // -------------- 헬퍼 메서드들 ----------------

    private ArtistCancellationResponse.CancellationRequest createSampleCancellationRequest() {
        return ArtistCancellationResponse.CancellationRequest.builder()
                .requestId(1L)
                .orderNumber("ORD-20241225-001")
                .status("PENDING")
                .refundAmount(25000)
                .customer(ArtistCancellationResponse.Customer.builder()
                        .id(201L).nickname("고객명")
                        .build())
                .orderItem(ArtistCancellationResponse.OrderItem.builder()
                        .productId(101L).productName("귀여운 고양이 스티커")
                        .quantity(2).price(12500)
                        .build())
                .permissions(ArtistCancellationResponse.Permissions.builder()
                        .canApprove(true).canReject(true)
                        .build())
                .build();
    }

    private ArtistCancellationResponse.CancellationRequest createRequestWithStatus(String status, boolean canApprove, boolean canReject) {
        return ArtistCancellationResponse.CancellationRequest.builder()
                .requestId(1L)
                .orderNumber("ORD-" + status)
                .status(status)
                .refundAmount(25000)
                .customer(ArtistCancellationResponse.Customer.builder()
                        .id(201L).nickname("고객명")
                        .build())
                .orderItem(ArtistCancellationResponse.OrderItem.builder()
                        .productId(101L).productName("상품명")
                        .quantity(1).price(25000)
                        .build())
                .permissions(ArtistCancellationResponse.Permissions.builder()
                        .canApprove(canApprove).canReject(canReject)
                        .build())
                .build();
    }

    private ArtistCancellationResponse.Summary createSampleSummary() {
        return ArtistCancellationResponse.Summary.builder()
                .total(8).pending(5).approved(2).rejected(1)
                .build();
    }

    private List<ArtistCancellationResponse.BulkAction> createSampleBulkActions() {
        return Arrays.asList(
                ArtistCancellationResponse.BulkAction.builder()
                        .action("CANCEL_APPROVE").label("취소 승인").requiresConfirmation(true)
                        .build(),
                ArtistCancellationResponse.BulkAction.builder()
                        .action("CANCEL_REJECT").label("취소 거절").requiresConfirmation(true)
                        .build()
        );
    }
}
