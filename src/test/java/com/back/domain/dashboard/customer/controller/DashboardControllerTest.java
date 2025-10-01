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
                .andExpect(jsonPath("$.resultCode").value("200"))
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

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - 성공")
    void getFundingParticipations_Success() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.summary.totalParticipations").isNumber())
                .andExpect(jsonPath("$.data.summary.active").isNumber())
                .andExpect(jsonPath("$.data.summary.ended").isNumber())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - 상태 필터링")
    void getFundingParticipations_WithStatusFilter() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), eq("ACTIVE"), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - 키워드 검색")
    void getFundingParticipations_WithKeyword() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), any(), eq("굿즈"), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "굿즈"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - 정렬")
    void getFundingParticipations_WithSorting() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), any(), any(), eq("pledgedAmount"), eq("DESC")))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "pledgedAmount")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - participationNumber 포맷 검증")
    void getFundingParticipations_ValidatesParticipationNumberFormat() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].participationNumber").exists())
                .andExpect(jsonPath("$.data.content[0].participationNumber").isString());
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 API - Meta 제외 검증")
    void getFundingParticipations_ExcludesMeta() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                anyString(), anyInt(), anyInt(), any(), any(), anyString(), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
                        .header("Authorization", BEARER_TOKEN)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].meta").doesNotExist());
    }

    // =========================== 헬퍼 메서드들 ===========================

    private AccountResponse.Settings createMockAccountSettings() {
        return new AccountResponse.Settings(
                new AccountResponse.Profile(
                        10025L,
                        "테스트유저",
                        "https://cdn.example.com/profile.jpg"
                ),
                new AccountResponse.Contact(
                        "test@example.com",
                        true,
                        "+821012345678",
                        "서울특별시 강남구 테헤란로 123"
                ),
                new AccountResponse.Security(
                        LocalDateTime.of(2025, 8, 10, 11, 0)
                )
        );
    }

    private ArtistApplicationResponse.List createMockArtistApplicationList() {
        ArtistApplicationResponse.SummaryDto summary = new ArtistApplicationResponse.SummaryDto(
                2, 1, 1, 0
        );

        List<ArtistApplicationResponse.Summary> content = List.of(
                new ArtistApplicationResponse.Summary(
                        1L,
                        "테스트작가",
                        "2025-09-20",
                        "PENDING",
                        "대기중",
                        new ArtistApplicationResponse.Permission(true, true, null),
                        LocalDateTime.now()
                )
        );

        return new ArtistApplicationResponse.List(summary, content, 0, 10, 2, 1, false, false);
    }

    private OrderResponse.List createMockOrderList() {
        OrderResponse.SummaryDto summary = new OrderResponse.SummaryDto(
                25, 3, 2, 0, 0, 0, 0, 2, 0, 0, 1, 0, 0
        );

        List<OrderResponse.Summary> content = List.of(
                new OrderResponse.Summary(
                        "test-order-123",
                        "ORD001",
                        "2025-09-20T11:20:00+09:00",
                        "PENDING",
                        "발주 전",
                        47500,
                        2,
                        new OrderResponse.Product(
                                101L,
                                "테스트상품",
                                1,
                                25000,
                                "https://cdn.example.com/product.jpg"
                        ),
                        null, // shipping
                        new OrderResponse.Aftersales(
                                new OrderResponse.AftersalesItem(
                                        "REQUESTED",
                                        "취소 요청 중",
                                        901L
                                ),
                                null // exchange
                        ),
                        null, // permissions
                        null, // links
                        List.of(
                                new OrderResponse.OrderItem(
                                        1L,
                                        101L,
                                        "테스트상품",
                                        1,
                                        25000,
                                        null
                                )
                        )
                )
        );

        OrderResponse.PeriodInfo periodInfo = new OrderResponse.PeriodInfo(
                "MONTH",
                "2025-09-01",
                "2025-09-30"
        );

        return new OrderResponse.List(summary, content, 0, 10, 25, 3, true, false, "Asia/Seoul", periodInfo);
    }

    private com.back.domain.dashboard.customer.dto.response.FundingResponse.List createMockFundingList() {
        com.back.domain.dashboard.customer.dto.response.FundingResponse.SummaryDto summary =
                new com.back.domain.dashboard.customer.dto.response.FundingResponse.SummaryDto(
                        5, 2, 3
                        // 배송 상태는 주석 처리되어 있으므로 제외
                );

        List<com.back.domain.dashboard.customer.dto.response.FundingResponse.Participation> content = List.of(
                new com.back.domain.dashboard.customer.dto.response.FundingResponse.Participation(
                        "00010",
                        10L,
                        "https://cdn.example.com/funding.jpg",
                        "테스트 펀딩",
                        new com.back.domain.dashboard.customer.dto.response.FundingResponse.Artist(
                                101L,
                                "테스트작가"
                        ),
                        1,
                        25000L,
                        "ACTIVE",
                        "진행중",
                        "2025-09-20",
                        new com.back.domain.dashboard.customer.dto.response.FundingResponse.Link(
                                "/fundings/456789"
                        ),
                        null  // Meta는 목록 조회에서 제외
                )
        );

        return new com.back.domain.dashboard.customer.dto.response.FundingResponse.List(
                summary, content, 0, 10, 5, 1, false, false
        );
    }
}
