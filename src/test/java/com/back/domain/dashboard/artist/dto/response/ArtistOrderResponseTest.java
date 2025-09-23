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
                () -> assertThat(summary.getPreparing()).isNotNegative(),
                () -> assertThat(summary.getShipped()).isNotNegative(),
                () -> assertThat(summary.getDelivered()).isNotNegative(),
                () -> assertThat(summary.getCanceled()).isNotNegative(),
                // 주문 내역 기본 검증
                () -> assertThat(orderList.getContent()).isNotEmpty(),
                () -> assertThat(orderList.getContent().get(0).getOrderNumber()).isNotBlank(),
                () -> assertThat(orderList.getContent().get(0).getTotalAmount()).isPositive()
        );
    }

    @Test
    @DisplayName("주문 권한 및 배송 상태 검증")
    void validateOrderPermissionsAndShipment_Success() {
        // When
        ArtistOrderResponse.Order pendingOrder = createPendingOrder();
        ArtistOrderResponse.Order deliveredOrder = createDeliveredOrder();

        // Then - 상태별 권한 로직 검증
        assertAll(
                // 발주 전 주문은 상태 변경과 취소가 가능해야 함
                () -> assertThat(pendingOrder.getPermissions().isCanChangeStatus()).isTrue(),
                () -> assertThat(pendingOrder.getPermissions().isCanCancel()).isTrue(),
                () -> assertThat(pendingOrder.getShipment().getStatus()).isEqualTo("READY"),
                // 배송 완료 주문은 변경 불가능해야 함
                () -> assertThat(deliveredOrder.getPermissions().isCanChangeStatus()).isFalse(),
                () -> assertThat(deliveredOrder.getPermissions().isCanCancel()).isFalse(),
                () -> assertThat(deliveredOrder.getShipment().getTrackingNo()).isNotBlank()
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
                () -> assertThat(response.getTotalElements()).isEqualTo(156),
                () -> assertThat(response.isHasNext()).isTrue()
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private ArtistOrderResponse.List createSampleOrderList() {
        return ArtistOrderResponse.List.builder()
                .summary(ArtistOrderResponse.Summary.builder()
                        .total(156).pending(8).preparing(12)
                        .shipped(142).delivered(136).canceled(5)
                        .build())
                .content(Arrays.asList(createPendingOrder(), createDeliveredOrder()))
                .page(0).size(20).totalElements(156).totalPages(8)
                .hasNext(true).hasPrevious(false)
                .build();
    }

    private ArtistOrderResponse.Order createPendingOrder() {
        return ArtistOrderResponse.Order.builder()
                .orderId("550e84...000")
                .orderNumber("0123157")
                .status("PENDING")
                .totalAmount(47500)
                .itemCount(2)
                .buyer(ArtistOrderResponse.Buyer.builder()
                        .id(201L).nickname("heroeson02").name("손경호")
                        .build())
                .shipment(ArtistOrderResponse.Shipment.builder()
                        .status("READY").trackingNo(null).shippingCompany(null)
                        .build())
                .permissions(ArtistOrderResponse.Permissions.builder()
                        .canChangeStatus(true).canCancel(true)
                        .build())
                .build();
    }

    private ArtistOrderResponse.Order createDeliveredOrder() {
        return ArtistOrderResponse.Order.builder()
                .orderId("550e84...002")
                .orderNumber("0123155")
                .status("DELIVERED")
                .totalAmount(35000)
                .itemCount(3)
                .buyer(ArtistOrderResponse.Buyer.builder()
                        .id(203L).nickname("stickerfan").name("이스티")
                        .build())
                .shipment(ArtistOrderResponse.Shipment.builder()
                        .status("DELIVERED").trackingNo("987654321098").shippingCompany("CJ대한통운")
                        .build())
                .permissions(ArtistOrderResponse.Permissions.builder()
                        .canChangeStatus(false).canCancel(false)
                        .build())
                .build();
    }
}
