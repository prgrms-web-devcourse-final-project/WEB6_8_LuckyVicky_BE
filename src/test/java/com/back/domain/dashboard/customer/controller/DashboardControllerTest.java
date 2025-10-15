package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.request.*;
import com.back.domain.dashboard.customer.dto.response.AccountResponse;
import com.back.domain.dashboard.customer.dto.response.ArtistApplicationResponse;
import com.back.domain.dashboard.customer.dto.response.OrderResponse;
import com.back.domain.dashboard.customer.service.DashboardService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DashboardController 테스트
 * API 엔드포인트와 응답 구조에 집중
 * 2025.10.02 수정 - JWT 표준 패턴 적용에 따른 테스트 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 컨트롤러 테스트")
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private static final Long TEST_USER_ID = 10025L;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // CustomUserDetails Mock 생성
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(TEST_USER_ID);
        testUserDetails = new CustomUserDetails(mockUser, Role.USER);

        // MockMvc에 커스텀 ArgumentResolver 설정
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setCustomArgumentResolvers(new CustomAuthenticationPrincipalArgumentResolver(testUserDetails))
                .build();
    }

    @Test
    @DisplayName("계정 설정 조회 API - 성공")
    void getAccountSettings_Success() throws Exception {
        // Given
        AccountResponse.Settings mockResponse = createMockAccountSettings();
        given(dashboardService.getAccountSettings(eq(TEST_USER_ID), anyString()))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/account")
                        .param("include", "profile,contact,security"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.profile").exists())
                .andExpect(jsonPath("$.data.contact").exists())
                .andExpect(jsonPath("$.data.security").exists());
    }

    @Test
    @DisplayName("작가 신청 목록 조회 API - 성공")
    void getArtistApplications_Success() throws Exception {
        // Given
        ArtistApplicationResponse.List mockResponse = createMockArtistApplicationList();
        given(dashboardService.getArtistApplications(
                eq(TEST_USER_ID), any(ArtistApplicationSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/artist-applications")
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
    @DisplayName("주문 목록 조회 API - 성공 (실제 DB 연동)")
    void getOrders_Success() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "상품명")
                        .param("sort", "orderDate")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.summary.totalOrders").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].orderId").exists())
                .andExpect(jsonPath("$.data.content[0].orderNumber").exists())
                .andExpect(jsonPath("$.data.content[0].orderDate").exists())
                .andExpect(jsonPath("$.data.content[0].status").exists())
                .andExpect(jsonPath("$.data.content[0].statusText").exists())
                .andExpect(jsonPath("$.data.content[0].totalAmount").exists())
                .andExpect(jsonPath("$.data.content[0].representativeItem").exists())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("주문 목록 조회 API - 주문번호 7자리 포맷 검증")
    void getOrders_ValidatesOrderNumberFormat() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].orderNumber").isString())
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("0001234"));
    }

    @Test
    @DisplayName("주문 목록 조회 API - 4가지 상태만 반환 검증")
    void getOrders_OnlyReturnsValidStatuses() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("PAYMENT_COMPLETED"))
                .andExpect(jsonPath("$.data.content[0].statusText").value("결제완료"));
    }

    @Test
    @DisplayName("주문 목록 조회 API - 날짜 포맷 검증")
    void getOrders_ValidatesDateFormat() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].orderDate").value("2025. 09. 18"));
    }

    @Test
    @DisplayName("주문 목록 조회 API - 삭제된 상품 처리 검증")
    void getOrders_HandlesDeletedProducts() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderListWithDeletedProduct();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].representativeItem.productName")
                        .value("테스트상품"))  // 삭제된 상품도 이름은 그대로
                .andExpect(jsonPath("$.data.content[0].representativeItem.imageUrl").isEmpty());  // 이미지만 null
    }

    @Test
    @DisplayName("주문 목록 조회 API - 검색 파라미터 검증")
    void getOrders_ValidatesSearchParameters() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then - 상품명으로 검색
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "포스터"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 목록 조회 API - 정렬 파라미터 검증")
    void getOrders_ValidatesSortParameters() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then - 금액순 정렬
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "totalAmount")
                        .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 목록 조회 API - 상품명 정렬 검증")
    void getOrders_SortsByProductName() throws Exception {
        // Given
        OrderResponse.List mockResponse = createMockOrderList();
        given(dashboardService.getOrders(
                eq(TEST_USER_ID), any(OrderSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then - 상품명 오름차순 정렬
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productName")
                        .param("order", "ASC"))
                .andDo(print())
                .andExpect(status().isOk());

        // When & Then - 상품명 내림차순 정렬
        mockMvc.perform(get("/api/dashboard/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "productName")
                        .param("order", "DESC"))
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
                eq(TEST_USER_ID), any(FundingSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
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
    @DisplayName("펀딩 참여 목록 조회 API - participationNumber 포맷 검증")
    void getFundingParticipations_ValidatesParticipationNumberFormat() throws Exception {
        // Given
        com.back.domain.dashboard.customer.dto.response.FundingResponse.List mockResponse =
                createMockFundingList();
        given(dashboardService.getFundingParticipations(
                eq(TEST_USER_ID), any(FundingSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
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
                eq(TEST_USER_ID), any(FundingSearchRequest.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/dashboard/funding")
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
                        TEST_USER_ID,
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
        OrderResponse.SummaryDto summary = new OrderResponse.SummaryDto(156);

        List<OrderResponse.Summary> content = List.of(
                new OrderResponse.Summary(
                        "1234",                           // orderId
                        "0001234",                        // orderNumber (7자리 포맷)
                        "2025. 09. 18",                   // orderDate (포맷팅됨)
                        "PAYMENT_COMPLETED",              // status
                        "결제완료",                        // statusText
                        10000,                            // totalAmount
                        2,                                // itemCount
                        new OrderResponse.Product(        // representativeItem
                                101L,
                                "테스트상품",
                                2,
                                5000,
                                "https://cdn.example.com/product.jpg"
                        ),
                        new OrderResponse.Shipping(       // shipping
                                "서울시 강남구",
                                "홍길동"
                        ),
                        null,                             // aftersales (제거됨)
                        new OrderResponse.Permission(     // permissions
                                true,                     // canCancel
                                false,                    // canReturn
                                false                     // canExchange
                        ),
                        new OrderResponse.Link(           // links
                                "/orders/ORD1234567890"
                        ),
                        List.of(                          // items
                                new OrderResponse.OrderItem(
                                        1L,
                                        101L,
                                        "테스트상품",
                                        2,
                                        5000,
                                        "https://cdn.example.com/product.jpg"
                                )
                        )
                )
        );

        return new OrderResponse.List(summary, content, 0, 10, 156, 16, true, false);
    }

    private OrderResponse.List createMockOrderListWithDeletedProduct() {
        OrderResponse.SummaryDto summary = new OrderResponse.SummaryDto(1);

        List<OrderResponse.Summary> content = List.of(
                new OrderResponse.Summary(
                        "1234",
                        "0001234",
                        "2025. 09. 18",
                        "PAYMENT_COMPLETED",
                        "결제완료",
                        10000,
                        1,
                        new OrderResponse.Product(
                                101L,
                                "테스트상품",              // 삭제된 상품도 이름은 그대로
                                1,
                                10000,
                                null                      // 이미지만 null
                        ),
                        new OrderResponse.Shipping(
                                "서울시 강남구",
                                "홍길동"
                        ),
                        null,
                        new OrderResponse.Permission(true, false, false),
                        new OrderResponse.Link("/orders/ORD1234567890"),
                        List.of(
                                new OrderResponse.OrderItem(
                                        1L,
                                        101L,
                                        "테스트상품",      // 삭제된 상품도 이름은 그대로
                                        1,
                                        10000,
                                        null              // 이미지만 null
                                )
                        )
                )
        );

        return new OrderResponse.List(summary, content, 0, 10, 1, 1, false, false);
    }

    private com.back.domain.dashboard.customer.dto.response.FundingResponse.List createMockFundingList() {
        com.back.domain.dashboard.customer.dto.response.FundingResponse.SummaryDto summary =
                new com.back.domain.dashboard.customer.dto.response.FundingResponse.SummaryDto(
                        5, 2, 3
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

    /**
     * @AuthenticationPrincipal을 위한 커스텀 ArgumentResolver
     */
    private static class CustomAuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        private final CustomUserDetails userDetails;

        public CustomAuthenticationPrincipalArgumentResolver(CustomUserDetails userDetails) {
            this.userDetails = userDetails;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return userDetails;
        }
    }
}
