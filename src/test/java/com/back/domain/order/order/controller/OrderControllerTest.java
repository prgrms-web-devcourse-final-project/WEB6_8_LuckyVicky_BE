package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.request.OrderCancelRequestDto;
import com.back.domain.order.order.dto.request.OrderRequestDto;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.service.OrderService;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

class OrderControllerTest {

    private OrderController orderController;
    private OrderService orderService;
    private ObjectMapper objectMapper;

    private User testUser;
    private CustomUserDetails customUserDetails;
    private OrderRequestDto orderRequestDto;
    private OrderCancelRequestDto orderCancelRequestDto;
    private OrderResponseDto orderResponseDto;
    
    private static final UUID TEST_PRODUCT_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() throws Exception {
        // Mock 서비스 설정
        orderService = mock(OrderService.class);
        objectMapper = new ObjectMapper();
        orderController = new OrderController(orderService);

        // 테스트 사용자 설정 (reflection으로 ID 설정)
        testUser = User.createLocalUser("test@example.com", "password123", "테스트사용자", "010-1234-5678");
        java.lang.reflect.Field idField = testUser.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1L);
        
        // CustomUserDetails 생성
        customUserDetails = new CustomUserDetails(testUser, com.back.domain.user.entity.Role.USER);

        // 주문 요청 DTO
        orderRequestDto = new OrderRequestDto(
                List.of( // orderItems
                        new OrderRequestDto.OrderItemRequestDto(
                                TEST_PRODUCT_UUID, // productUuid
                                2, // quantity
                                "빨강, L" // optionInfo
                        )
                ),
                "서울시 강남구", // shippingAddress1
                "테헤란로 123", // shippingAddress2
                "12345", // shippingZip
                "홍길동", // recipientName
                "010-1234-5678", // recipientPhone
                "문 앞에 놓아주세요", // deliveryRequest
                PaymentMethod.CARD // paymentMethod
        );

        // 주문 취소 요청 DTO
        orderCancelRequestDto = new OrderCancelRequestDto(
                "단순변심", // cancelReason
                List.of(1L, 2L) // orderItemIds
        );

        // 주문 응답 DTO
        orderResponseDto = new OrderResponseDto(
                1L, // orderId
                "ORD123456789", // orderNumber
                OrderStatus.PAYMENT_COMPLETED, // status
                2, // totalQuantity
                BigDecimal.valueOf(50000), // totalAmount
                BigDecimal.valueOf(3000), // shippingFee
                BigDecimal.valueOf(53000), // finalAmount
                "서울시 강남구", // shippingAddress1
                "테헤란로 123", // shippingAddress2
                "12345", // shippingZip
                "홍길동", // recipientName
                "010-1234-5678", // recipientPhone
                "문 앞에 놓아주세요", // deliveryRequest
                PaymentMethod.CARD, // paymentMethod
                LocalDateTime.now(), // orderDate
                List.of( // orderItems
                        new OrderResponseDto.OrderItemResponseDto(
                                1L, // orderItemId
                                TEST_PRODUCT_UUID, // productUuid
                                "테스트 상품", // productName
                                "http://example.com/image.jpg", // productThumbnailUrl
                                2, // quantity
                                BigDecimal.valueOf(25000), // price
                                BigDecimal.valueOf(50000), // totalPrice
                                "빨강, L" // optionInfo
                        )
                )
        );
    }

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder_Success() {
        // Given
        given(orderService.createOrder(any(User.class), any(OrderRequestDto.class)))
                .willReturn(orderResponseDto);

        // When
        var result = orderController.createOrder(orderRequestDto, customUserDetails);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().orderId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
        assertThat(result.getBody().status()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(result.getBody().totalQuantity()).isEqualTo(2);
        assertThat(result.getBody().totalAmount()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(result.getBody().shippingFee()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getBody().finalAmount()).isEqualTo(BigDecimal.valueOf(53000));
        assertThat(result.getBody().recipientName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("주문 목록 조회 - 성공")
    void getOrderList_Success() {
        // Given
        List<OrderResponseDto> orders = List.of(orderResponseDto);
        Page<OrderResponseDto> orderPage = new PageImpl<>(orders, PageRequest.of(0, 8), 1);
        
        given(orderService.getOrderList(any(User.class), any(Pageable.class)))
                .willReturn(orderPage);

        // When
        var result = orderController.getOrderList(PageRequest.of(0, 8), customUserDetails);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1);
        assertThat(result.getBody().getContent().get(0).orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문 상세 조회 - 성공")
    void getOrderDetail_Success() {
        // Given
        given(orderService.getOrderDetail(eq(1L), any(User.class)))
                .willReturn(orderResponseDto);

        // When
        var result = orderController.getOrderDetail(1L, customUserDetails);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().orderId()).isEqualTo(1L);
        assertThat(result.getBody().orderNumber()).isEqualTo("ORD123456789");
    }

    @Test
    @DisplayName("주문 취소 - 성공")
    void cancelOrder_Success() {
        // Given
        willDoNothing().given(orderService).cancelOrder(eq(1L), any(User.class), any(OrderCancelRequestDto.class));

        // When
        var result = orderController.cancelOrder(1L, orderCancelRequestDto, customUserDetails);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("주문 취소 승인 - 성공")
    void approveOrderCancellation_Success() {
        // Given
        willDoNothing().given(orderService).approveOrderCancellation(eq(1L), any(User.class));

        // When
        var result = orderController.approveOrderCancellation(1L, customUserDetails);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("주문 상세 조회 - 존재하지 않는 주문")
    void getOrderDetail_NotFound() {
        // Given
        given(orderService.getOrderDetail(eq(999L), any(User.class)))
                .willThrow(new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> orderController.getOrderDetail(999L, customUserDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }
}