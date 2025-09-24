package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistOrderResponse DTO 테스트
 * 핵심 비즈니스 로직에 집중
 * 2025.09.24 생성
 */
@DisplayName("ArtistOrderResponse DTO 테스트")
public class ArtistOrderResponseTest {

    @Test
    @DisplayName("주문 목록 구조 생성 및 검증")
    void createOrderListStructure_Success() {
        // When
        ArtistOrderResponse.List orderList = createSampleOrderList();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(orderList).isNotNull(),
                () -> assertThat(orderList.getSummary()).isNotNull(),
                () -> assertThat(orderList.getContent()).isNotNull(),
                () -> assertThat(orderList.getPage()).isNotNegative(),
                () -> assertThat(orderList.getSize()).isPositive(),
                () -> assertThat(orderList.getTotalElements()).isNotNegative()
        );
    }

    @Test
    @DisplayName("주문 통계 비즈니스 로직 검증")
    void validateOrderSummaryBusinessLogic_Success() {
        // When
        ArtistOrderResponse.List orderList = createSampleOrderList();
        ArtistOrderResponse.Summary summary = orderList.getSummary();

        // Then - 핵심 비즈니스 규칙 검증
        assertAll(
                // 통계 정보 기본 검증
                () -> assertThat(summary.getTotal()).isEqualTo(156),
                () -> assertThat(summary.getPending()).isEqualTo(8),
                () -> assertThat(summary.getDelivered()).isEqualTo(136),
                // 각 상태별 수량은 음수가 아니어야 함
                () -> assertThat(summary.getPending()).isNotNegative(),
                () -> assertThat(summary.getDelivered()).isNotNegative(),
                () -> assertThat(summary.getCanceled()).isNotNegative(),
                // 주문 내역 기본 검증
                () -> assertThat(orderList.getContent()).isNotEmpty(),
                () -> assertThat(orderList.getContent().get(0).getOrderNumber()).isNotBlank(),
                () -> assertThat(orderList.getContent().get(0).getTotalAmount()).isPositive()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistOrderResponse.List response = ArtistOrderResponse.List.builder()
                .summary(ArtistOrderResponse.Summary.builder()
                        .total(156).pending(8).preparing(12)
                        .shipped(142).delivered(136).canceled(5)
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
                .page(0).size(10).totalElements(156).totalPages(8)
                .hasNext(true).hasPrevious(false)
                .build();

        // Then - API 명세 호환성 검증
        assertAll(
                () -> assertThat(response.getSummary().getTotal()).isEqualTo(156),
                () -> assertThat(response.getContent().get(0).getOrderNumber()).isEqualTo("0123157"),
                () -> assertThat(response.getContent().get(0).getBuyer().getNickname()).isEqualTo("heroeson02"),
                () -> assertThat(response.getContent().get(0).getPermissions().isCanChangeStatus()).isTrue(),
                () -> assertThat(response.getTotalElements()).isEqualTo(156)
        );
    }

    //----------------- 헬퍼 메서드------------------

    private ArtistOrderResponse.List createSampleOrderList() {
        return ArtistOrderResponse.List.builder()
                .summary(ArtistOrderResponse.Summary.builder()
                        .total(156).pending(8).preparing(12)
                        .shipped(142).delivered(136).canceled(5)
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
                                .build()
                ))
                .page(0).size(20).totalElements(156).totalPages(8)
                .hasNext(true).hasPrevious(false)
                .build();
    }
}
