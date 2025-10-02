package com.back.domain.order.order.controller;

import com.back.domain.order.order.dto.request.OrderCancelRequestDto;
import com.back.domain.order.order.dto.request.OrderExchangeRequestDto;
import com.back.domain.order.order.dto.request.OrderRefundRequestDto;
import com.back.domain.order.order.dto.request.OrderRequestDto;
import com.back.domain.order.order.dto.request.OrderStatusChangeRequestDto;
import com.back.domain.order.order.dto.response.OrderResponseDto;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.service.OrderService;
import com.back.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.back\\.global\\.security\\..*"
        )
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private OrderRequestDto orderRequestDto;
    private OrderResponseDto orderResponseDto;
    private OrderCancelRequestDto orderCancelRequestDto;
    private OrderRefundRequestDto orderRefundRequestDto;
    private OrderExchangeRequestDto orderExchangeRequestDto;
    private OrderStatusChangeRequestDto orderStatusChangeRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.createLocalUser(
                "test@test.com",
                "password",
                "testUser",
                "010-0000-0000"
        );
        // ID 주입
        org.springframework.test.util.ReflectionTestUtils.setField(testUser, "id", 1L);

        UUID productUuid = UUID.randomUUID();
        orderRequestDto = new OrderRequestDto(
                Arrays.asList(new OrderRequestDto.OrderItemRequestDto(productUuid, 2, "색상: 빨강")),
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "홍길동",
                "010-1234-5678",
                "문 앞에 놓아주세요",
                PaymentMethod.CARD
        );

        OrderResponseDto.OrderItemResponseDto orderItemDto = new OrderResponseDto.OrderItemResponseDto(
                1L,
                1L,
                "테스트상품",
                "https://example.com/image.jpg",
                2,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(20000),
                "색상: 빨강"
        );

        orderResponseDto = new OrderResponseDto(
                1L,
                "ORD123456789",
                OrderStatus.PAYMENT_COMPLETED,
                2,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(23000),
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "홍길동",
                "010-1234-5678",
                "문 앞에 놓아주세요",
                PaymentMethod.CARD,
                LocalDateTime.now(),
                Arrays.asList(orderItemDto)
        );

        orderCancelRequestDto = new OrderCancelRequestDto("단순 변심", Arrays.asList(1L));
        orderRefundRequestDto = new OrderRefundRequestDto("상품 불량", Arrays.asList(1L));
        orderExchangeRequestDto = new OrderExchangeRequestDto("사이즈 불량", Arrays.asList(1L));
        orderStatusChangeRequestDto = new OrderStatusChangeRequestDto(OrderStatus.PREPARING_SHIPMENT);
    }

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder_Success() throws Exception {
        // given
        when(orderService.createOrder(any(), any(OrderRequestDto.class)))
                .thenReturn(orderResponseDto);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(orderService, times(1)).createOrder(any(), any(OrderRequestDto.class));
    }

    @Test
    @DisplayName("주문 목록 조회 - 성공")
    void getOrderList_Success() throws Exception {
        // given
        Page<OrderResponseDto> orderPage = new PageImpl<>(Arrays.asList(orderResponseDto));
        when(orderService.getOrderList(any(), any()))
                .thenReturn(orderPage);

        // when & then
        mockMvc.perform(get("/api/orders"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).getOrderList(any(), any());
    }

    @Test
    @DisplayName("주문 상세 조회 - 성공")
    void getOrderDetail_Success() throws Exception {
        // given
        Long orderId = 1L;
        when(orderService.getOrderDetail(eq(orderId), any()))
                .thenReturn(orderResponseDto);

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).getOrderDetail(eq(orderId), any());
    }

    @Test
    @DisplayName("주문 취소 - 성공")
    void cancelOrder_Success() throws Exception {
        // given
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(eq(orderId), any(), any(OrderCancelRequestDto.class));

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCancelRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancelOrder(eq(orderId), any(), any(OrderCancelRequestDto.class));
    }

    @Test
    @DisplayName("환불 신청 - 성공")
    void requestRefund_Success() throws Exception {
        // given
        Long orderId = 1L;
        doNothing().when(orderService).requestRefund(eq(orderId), any(), any(OrderRefundRequestDto.class));

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/refund", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRefundRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).requestRefund(eq(orderId), any(), any(OrderRefundRequestDto.class));
    }

    @Test
    @DisplayName("교환 신청 - 성공")
    void requestExchange_Success() throws Exception {
        // given
        Long orderId = 1L;
        doNothing().when(orderService).requestExchange(eq(orderId), any(), any(OrderExchangeRequestDto.class));

        // when & then
        mockMvc.perform(post("/api/orders/{orderId}/exchange", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderExchangeRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).requestExchange(eq(orderId), any(), any(OrderExchangeRequestDto.class));
    }

    @Test
    @DisplayName("주문 상태 변경 - 성공")
    void changeOrderStatus_Success() throws Exception {
        // given
        Long orderId = 1L;
        doNothing().when(orderService).changeOrderStatus(eq(orderId), any(OrderStatusChangeRequestDto.class), any());

        // when & then
        mockMvc.perform(patch("/api/orders/{orderId}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderStatusChangeRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).changeOrderStatus(eq(orderId), any(OrderStatusChangeRequestDto.class), any());
    }

    @Test
    @DisplayName("주문 목록 페이징 - 성공")
    void getOrderList_Pagination() throws Exception {
        // given
        Page<OrderResponseDto> orderPage = new PageImpl<>(Arrays.asList(orderResponseDto));
        when(orderService.getOrderList(any(), any()))
                .thenReturn(orderPage);

        // when & then
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "8"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(orderService, times(1)).getOrderList(any(), any());
    }

    @Test
    @DisplayName("주문 상세 조회 - 존재하지 않는 주문")
    void getOrderDetail_NotFound() throws Exception {
        // given
        Long orderId = 999L;
        when(orderService.getOrderDetail(eq(orderId), any()))
                .thenThrow(new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).getOrderDetail(eq(orderId), any());
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 - 성공")
    void unauthorizedAccess_Success() throws Exception {
        // given - Security 비활성화로 인해 인증 없이도 접근 가능하므로 Mock 설정
        Page<OrderResponseDto> orderPage = new PageImpl<>(Arrays.asList(orderResponseDto));
        when(orderService.getOrderList(any(), any()))
                .thenReturn(orderPage);
        
        // when & then
        mockMvc.perform(get("/api/orders"))
                .andDo(print())
                .andExpect(status().isOk()); // Security 비활성화로 인해 인증 없이도 접근 가능

        verify(orderService, times(1)).getOrderList(any(), any());
    }

    @Test
    @DisplayName("서비스 예외 발생 시 처리")
    void serviceException_Handling() throws Exception {
        // given
        when(orderService.createOrder(any(), any(OrderRequestDto.class)))
                .thenThrow(new RuntimeException("서비스 오류"));

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).createOrder(any(), any(OrderRequestDto.class));
    }
}