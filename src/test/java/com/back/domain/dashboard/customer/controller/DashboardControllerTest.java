package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.response.AccountResponse;
import com.back.domain.dashboard.customer.dto.response.ArtistApplicationResponse;
import com.back.domain.dashboard.customer.dto.response.OrderResponse;
import com.back.domain.dashboard.customer.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DashboardController 테스트
 * 2025.09.20 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 컨트롤러 테스트")
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private static final String BEARER_TOKEN = "Bearer test-token-123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    @DisplayName("정상적인 계정 설정 조회 - 전체 정보")
    void getAccountSettings_Success_AllInfo() throws Exception {
        // Given
        AccountResponse.Settings mockResponse = AccountResponse.Settings.builder()
                .profile(AccountResponse.Profile.builder()
                        .userId(10025L)
                        .nickname("테스트유저")
                        .profileImageUrl("https://cdn.example.com/profile.jpg")
                        .build())
                .contact(AccountResponse.Contact.builder()
                        .email("test@example.com")
                        .emailVerified(true)
                        .phone("+821012345678")
                        .address("서울특별시 강남구 테헤란로 123")
                        .build())
                .security(AccountResponse.Security.builder()
                        .lastPasswordChangedAt(LocalDateTime.of(2025, 8, 10, 11, 0))
                        .build())
                .build();

        given(dashboardService.getAccountSettings(anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/account")
                        .header("Authorization", BEARER_TOKEN)
                        .param("include", "profile,contact,security"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
                // JSON 경로 검증은 RsData 구조를 완전히 구현한 후에 추가
    }

    @Test
    @DisplayName("계정 설정 조회 - 프로필만")
    void getAccountSettings_Success_ProfileOnly() throws Exception {
        // Given
        AccountResponse.Settings mockResponse = AccountResponse.Settings.builder()
                .profile(AccountResponse.Profile.builder()
                        .userId(10025L)
                        .nickname("테스트유저")
                        .profileImageUrl("https://cdn.example.com/profile.jpg")
                        .build())
                .build();

        given(dashboardService.getAccountSettings(anyString(), eq("profile")))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/account")
                        .header("Authorization", BEARER_TOKEN)
                        .param("include", "profile"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정상적인 작가 신청 목록 조회")
    void getArtistApplications_Success() throws Exception {
        // Given
        ArtistApplicationResponse.List mockResponse = createMockArtistApplicationList();

        given(dashboardService.getArtistApplications(
                anyString(), anyInt(), anyInt(), any(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/artist-applications")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-OK"))
                .andExpect(jsonPath("$.data.summary.total").value(2))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].applicationId").value(1))
                .andExpect(jsonPath("$.data.content[0].artistName").value("테스트작가"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("정상적인 주문 목록 조회")
    void getOrders_Success() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();

        given(dashboardService.getOrders(
                anyString(), anyInt(), anyInt(), any(), any(), any(), any(), anyString(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING")
                        .param("period", "MONTH"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalOrders").value(42))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].orderId").exists())
                .andExpect(jsonPath("$.data.content[0].orderNumber").exists())
                .andExpect(jsonPath("$.data.content[0].representativeItem").exists())
                .andExpect(jsonPath("$.data.content[0].items").isArray());
    }

    // Mock 데이터 생성 헬퍼 메서드들
    private ArtistApplicationResponse.List createMockArtistApplicationList() {
        ArtistApplicationResponse.SummaryDto summary = ArtistApplicationResponse.SummaryDto.builder()
                .total(2)
                .pending(1)
                .approved(1)
                .rejected(0)
                .build();

        List<ArtistApplicationResponse.Summary> content = List.of(
                ArtistApplicationResponse.Summary.builder()
                        .applicationId(1L)
                        .artistName("테스트작가")
                        .submittedAt("2025-09-20")
                        .status("PENDING")
                        .statusText("대기중")
                        .permissions(ArtistApplicationResponse.Permission.builder()
                                .canEdit(true)
                                .canCancel(true)
                                .build())
                        .lastUpdatedAt(LocalDateTime.now())
                        .build()
        );

        return new ArtistApplicationResponse.List(summary, content, 0, 10, 2, 1, false, false);
    }

    private OrderResponse.List createMockOrderList() {
        OrderResponse.SummaryDto summary = OrderResponse.SummaryDto.builder()
                .totalOrders(42)
                .pending(3)
                .confirmed(2)
                .preparing(5)
                .shipped(12)
                .delivered(20)
                .canceled(2)
                .cancelRequested(2)
                .cancelProcessing(1)
                .cancelCompleted(1)
                .exchangeRequested(1)
                .exchangeProcessing(0)
                .exchangeCompleted(3)
                .build();

        List<OrderResponse.Order> content = List.of(
                OrderResponse.Order.builder()
                        .orderId("test-order-123")
                        .orderNumber("ORD001")
                        .orderDate(LocalDateTime.of(2025, 9, 20, 14, 30))
                        .status("PENDING")
                        .statusText("결제완료")
                        .totalAmount(47500)
                        .itemCount(2)
                        .representativeItem(OrderResponse.RepresentativeItem.builder()
                                .productId(101L)
                                .productName("테스트상품")
                                .quantity(1)
                                .price(25000)
                                .imageUrl("https://cdn.example.com/product.jpg")
                                .build())
                        .shipping(OrderResponse.Shipping.builder()
                                .addressShort("서울시 강남구...")
                                .recipient("테스트고객")
                                .build())
                        .aftersales(OrderResponse.Aftersales.builder()
                                .cancel(OrderResponse.AftersalesInfo.builder()
                                        .status("REQUESTED")
                                        .statusText("취소 요청 중")
                                        .requestId(901L)
                                        .build())
                                .exchange(null)
                                .build())
                        .permissions(OrderResponse.Permission.builder()
                                .canCancel(true)
                                .canReturn(false)
                                .canExchange(false)
                                .build())
                        .links(OrderResponse.Link.builder()
                                .detail("/orders/ORD001")
                                .build())
                        .items(List.of(
                                OrderResponse.OrderItem.builder()
                                        .orderItemId(1L)
                                        .productId(101L)
                                        .productName("테스트상품")
                                        .quantity(1)
                                        .price(25000)
                                        .imageUrl("https://cdn.example.com/product.jpg")
                                        .build()
                        ))
                        .build()
        );

        OrderResponse.Period period = OrderResponse.Period.builder()
                .type("MONTH")
                .from("2025-09-01")
                .to("2025-09-30")
                .build();

        return new OrderResponse.List(summary, content, 0, 10, 42, 5, true, false, "Asia/Seoul", period);
    }
}
