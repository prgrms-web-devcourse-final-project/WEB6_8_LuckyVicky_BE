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
 * API 엔드포인트와 응답 구조에 집중
 * 2025.09.23 수정
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
    @DisplayName("계정 설정 조회 API - 성공")
    void getAccountSettings_Success() throws Exception {
        // Given
        AccountResponse.Settings mockResponse = createMockAccountSettings();
        given(dashboardService.getAccountSettings(anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/account")
                        .header("Authorization", BEARER_TOKEN)
                        .param("include", "profile,contact,security"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("작가 신청 목록 조회 API - 성공")
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
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @DisplayName("주문 목록 조회 API - 성공")
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
                        .param("status", "PENDING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.totalOrders").value(25))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].orderId").exists())
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"));
    }

    @Test
    @DisplayName("API 파라미터 처리 검증")
    void validateApiParameters_Success() throws Exception {
        // Given
        given(dashboardService.getAccountSettings(anyString(), eq("profile")))
                .willReturn(createMockAccountSettings());

        // When & Then - 프로필만 조회
        mockMvc.perform(get("/api/dashboard/account")
                        .header("Authorization", BEARER_TOKEN)
                        .param("include", "profile"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =========================== 헬퍼 메서드들 ===========================

    private AccountResponse.Settings createMockAccountSettings() {
        return AccountResponse.Settings.builder()
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
    }

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
                .totalOrders(25)
                .pending(3)
                .confirmed(2)
                .cancelRequested(2)
                .exchangeRequested(1)
                .build();

        List<OrderResponse.Summary> content = List.of(
                OrderResponse.Summary.builder()
                        .orderId("test-order-123")
                        .orderNumber("ORD001")
                        .orderDate("2025-09-20T11:20:00+09:00")
                        .status("PENDING")
                        .statusText("발주 전")
                        .totalAmount(47500)
                        .itemCount(2)
                        .representativeItem(OrderResponse.Product.builder()
                                .productId(101L)
                                .productName("테스트상품")
                                .quantity(1)
                                .price(25000)
                                .imageUrl("https://cdn.example.com/product.jpg")
                                .build())
                        .aftersales(OrderResponse.Aftersales.builder()
                                .cancel(OrderResponse.AftersalesItem.builder()
                                        .status("REQUESTED")
                                        .statusText("취소 요청 중")
                                        .requestId(901L)
                                        .build())
                                .build())
                        .items(List.of(
                                OrderResponse.OrderItem.builder()
                                        .orderItemId(1L)
                                        .productId(101L)
                                        .productName("테스트상품")
                                        .quantity(1)
                                        .price(25000)
                                        .build()
                        ))
                        .build()
        );

        OrderResponse.PeriodInfo periodInfo = OrderResponse.PeriodInfo.builder()
                .type("MONTH")
                .from("2025-09-01")
                .to("2025-09-30")
                .build();

        return new OrderResponse.List(summary, content, 0, 10, 25, 3, true, false, "Asia/Seoul", periodInfo);
    }
}
