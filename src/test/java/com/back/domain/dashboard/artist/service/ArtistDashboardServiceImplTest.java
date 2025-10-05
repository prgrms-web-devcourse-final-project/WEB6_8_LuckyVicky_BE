package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.request.*;
import com.back.domain.dashboard.artist.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * ArtistDashboardServiceImpl 테스트
 * 핵심 비즈니스 로직만 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 서비스 테스트")
class ArtistDashboardServiceImplTest {

    @InjectMocks
    private ArtistDashboardServiceImpl artistDashboardService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingContributionRepository fundingContributionRepository;

    @Mock
    private com.back.domain.artist.repository.ArtistProfileRepository artistProfileRepository;

    @Mock
    private com.back.domain.order.refund.repository.RefundRepository refundRepository;

    @Mock
    private com.back.domain.order.exchange.repository.ExchangeRepository exchangeRepository;

    @Mock
    private com.back.domain.order.order.repository.OrderRepository orderRepository;

    private static final Long TEST_ARTIST_ID = 5L;

    // ==================== 상품 테스트 ====================

    @Test
    @DisplayName("상품 목록 조회 - 기본 조회 및 변환 검증")
    void getProducts_Success() {
        // Given
        Product mockProduct = createMockProduct(101L, "테스트 상품", 10000, 10, SellingStatus.SELLING);
        Page<Product> mockPage = new PageImpl<>(List.of(mockProduct), PageRequest.of(0, 10), 1);

        when(productRepository.findProductsByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), eq("createDate"), eq("DESC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistProductSearchRequest request = new ArtistProductSearchRequest(0, 10, null, null, "createDate", "DESC");

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().discountPrice()).isEqualTo(9000),
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("판매중")
        );
    }

    // ==================== 펀딩 테스트 ====================

    @Test
    @DisplayName("펀딩 목록 조회 - 상태 필터 및 변환 검증")
    void getFundings_WithStatusFilter() {
        // Given
        User mockUser = createMockUser(TEST_ARTIST_ID, "작가");
        Funding mockFunding = createMockFunding(1L, mockUser, "성공 펀딩", 500000L, FundingStatus.SUCCESS);
        Page<Funding> mockPage = new PageImpl<>(List.of(mockFunding), PageRequest.of(0, 10), 1);

        when(fundingRepository.findFundingsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq(FundingStatus.SUCCESS), eq("endDate"), eq("ASC"), any(PageRequest.class)))
                .thenReturn(mockPage);
        when(fundingContributionRepository.sumContributedAmountByFundingId(1L)).thenReturn(500000L);

        ArtistFundingSearchRequest request = new ArtistFundingSearchRequest(
                0, 10, null, "SUCCESS", null, null, null, null, null, "endDate", "ASC");

        // When
        ArtistFundingResponse.List result = artistDashboardService.getFundings(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("성공")
        );
    }

    // ==================== 설정 테스트 ====================

    @Test
    @DisplayName("작가 설정 조회 - 계좌번호 마스킹 검증")
    void getSettings_AccountMasking() {
        // Given
        User mockUser = createMockUser(TEST_ARTIST_ID, "작가");
        com.back.domain.artist.entity.ArtistProfile mockProfile = createMockArtistProfile(mockUser, "123-456-789012");

        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.of(mockProfile));

        // When
        ArtistSettingsResponse result = artistDashboardService.getSettings(TEST_ARTIST_ID);

        // Then
        assertThat(result.payout().accountMasked()).isEqualTo("****-****-**9012");
    }

    // ==================== 취소 요청 테스트 ====================

    @Test
    @DisplayName("취소 요청 조회 - 기본 조회 및 상태 매핑 검증")
    void getCancellationRequests_Success() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "테스트 상품", 15000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem = createMockRefundItem(1L, mockOrderItem, 1);
        com.back.domain.order.refund.entity.Refund mockRefund = createMockRefund(
                1L, mockOrder, mockCustomer, List.of(mockRefundItem),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED);

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(List.of(mockRefund), PageRequest.of(0, 10), 1);
        when(refundRepository.findRefundsByArtist(eq(TEST_ARTIST_ID), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistCancellationSearchRequest request = new ArtistCancellationSearchRequest(0, 10, null, null, null, null, null, null, null);

        // When
        ArtistCancellationResponse.List result = artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("처리대기"),
                () -> assertThat(result.content().getFirst().permissions().canApprove()).isTrue()
        );
    }

    // ==================== 교환 요청 테스트 ====================

    @Test
    @DisplayName("교환 요청 조회 - 기본 조회 및 상태 매핑 검증")
    void getExchangeRequests_Success() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "테스트 상품", 20000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.exchange.entity.ExchangeItem mockExchangeItem = createMockExchangeItem(1L, mockOrderItem, 1);
        com.back.domain.order.exchange.entity.Exchange mockExchange = createMockExchange(
                1L, mockOrder, mockCustomer, List.of(mockExchangeItem),
                com.back.domain.order.exchange.entity.Exchange.ExchangeStatus.REQUESTED,
                com.back.domain.order.exchange.entity.Exchange.ExchangeMethod.PICKUP);

        Page<com.back.domain.order.exchange.entity.Exchange> mockPage = new PageImpl<>(List.of(mockExchange), PageRequest.of(0, 10), 1);
        when(exchangeRepository.findExchangesByArtist(eq(TEST_ARTIST_ID), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistExchangeSearchRequest request = new ArtistExchangeSearchRequest(0, 10, null, null, null, null, null, null, null);

        // When
        ArtistExchangeResponse.List result = artistDashboardService.getExchangeRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().status()).isEqualTo("PENDING"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("처리대기"),
                () -> assertThat(result.content().getFirst().exchangeRequested().option()).isEmpty(),
                () -> assertThat(result.content().getFirst().permissions().canApprove()).isTrue()
        );
    }

    @Test
    @DisplayName("교환 요청 조회 - 상태 필터 및 권한 검증")
    void getExchangeRequests_StatusFilter() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "상품", 20000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.exchange.entity.ExchangeItem mockExchangeItem = createMockExchangeItem(1L, mockOrderItem, 1);
        com.back.domain.order.exchange.entity.Exchange mockExchange = createMockExchange(
                1L, mockOrder, mockCustomer, List.of(mockExchangeItem),
                com.back.domain.order.exchange.entity.Exchange.ExchangeStatus.COMPLETED,
                com.back.domain.order.exchange.entity.Exchange.ExchangeMethod.DIRECT);

        Page<com.back.domain.order.exchange.entity.Exchange> mockPage = new PageImpl<>(List.of(mockExchange), PageRequest.of(0, 10), 1);
        when(exchangeRepository.findExchangesByArtist(
                eq(TEST_ARTIST_ID), eq(com.back.domain.order.exchange.entity.Exchange.ExchangeStatus.COMPLETED), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistExchangeSearchRequest request = new ArtistExchangeSearchRequest(0, 10, "APPROVED", null, null, null, null, null, null);

        // When
        ArtistExchangeResponse.List result = artistDashboardService.getExchangeRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content().getFirst().status()).isEqualTo("APPROVED"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("승인됨"),
                () -> assertThat(result.content().getFirst().permissions().canApprove()).isFalse()
        );
    }

    // ==================== 주문 내역 테스트 ====================

    @Test
    @DisplayName("주문 내역 조회 - 기본 조회 및 DTO 변환 검증")
    void getOrders_Success() {
        // Given
        User mockCustomer = createMockUser(201L, "구매자김철수");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "핸드메이드 도자기", 50000, 10, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD20250918001");
        setOrderStatus(mockOrder, com.back.domain.order.order.entity.OrderStatus.SHIPPING);
        
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 2);

        // 주문에 주문 아이템 추가
        try {
            java.lang.reflect.Field orderItemsField = mockOrder.getClass().getDeclaredField("orderItems");
            orderItemsField.setAccessible(true);
            orderItemsField.set(mockOrder, List.of(mockOrderItem));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set orderItems", e);
        }

        Page<com.back.domain.order.order.entity.Order> mockPage = new PageImpl<>(List.of(mockOrder), PageRequest.of(0, 10), 1);
        when(orderRepository.findOrdersByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        when(orderRepository.findOrdersWithDetailsByArtist(anyList(), eq(TEST_ARTIST_ID)))
                .thenReturn(List.of(mockOrder));

        ArtistOrderSearchRequest request = new ArtistOrderSearchRequest(0, 10, null, null, null, null, null, null);

        // When
        ArtistOrderResponse.List result = artistDashboardService.getOrders(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().orderNumber()).isEqualTo("ORD20250918001"),
                () -> assertThat(result.content().getFirst().status()).isEqualTo("SHIPPING"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("배송중"),
                () -> assertThat(result.content().getFirst().buyer().name()).isEqualTo("구매자김철수"),
                () -> assertThat(result.content().getFirst().productSummary()).contains("도자기"),
                () -> assertThat(result.content().getFirst().shipment().status()).isEqualTo("배송 중")
        );
    }

    @Test
    @DisplayName("주문 내역 조회 - 상태 필터 검증")
    void getOrders_WithStatusFilter() {
        // Given
        User mockCustomer = createMockUser(201L, "구매자");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "상품", 30000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD001");
        setOrderStatus(mockOrder, com.back.domain.order.order.entity.OrderStatus.PREPARING_SHIPMENT);
        
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);

        try {
            java.lang.reflect.Field orderItemsField = mockOrder.getClass().getDeclaredField("orderItems");
            orderItemsField.setAccessible(true);
            orderItemsField.set(mockOrder, List.of(mockOrderItem));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set orderItems", e);
        }

        Page<com.back.domain.order.order.entity.Order> mockPage = new PageImpl<>(List.of(mockOrder), PageRequest.of(0, 10), 1);
        when(orderRepository.findOrdersByArtist(
                eq(TEST_ARTIST_ID), 
                eq(com.back.domain.order.order.entity.OrderStatus.PREPARING_SHIPMENT), 
                isNull(), isNull(), isNull(), 
                any(PageRequest.class)))
                .thenReturn(mockPage);

        when(orderRepository.findOrdersWithDetailsByArtist(anyList(), eq(TEST_ARTIST_ID)))
                .thenReturn(List.of(mockOrder));

        ArtistOrderSearchRequest request = new ArtistOrderSearchRequest(
                0, 10, "PREPARING_SHIPMENT", null, null, null, null, null);

        // When
        ArtistOrderResponse.List result = artistDashboardService.getOrders(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().status()).isEqualTo("PREPARING_SHIPMENT"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("배송준비중")
        );
    }

    // ==================== 메인 대시보드 통계/트렌드 테스트 ====================

    @Test
    @DisplayName("메인 대시보드 통계 조회 - 기본 통계 및 팔로우 처리 검증")
    void getMainStats_BasicStats() {
        // Given
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        com.back.domain.artist.entity.ArtistProfile mockProfile = createMockArtistProfileWithStats(
                mockArtist, 150, 10  // followerCount, productCount
        );

        com.back.domain.dashboard.artist.dto.DashboardStatsDto mockStats =
                new com.back.domain.dashboard.artist.dto.DashboardStatsDto(
                        3L,        // 오늘 주문 건수
                        150000L,   // 오늘 매출
                        50L,       // 총 주문 건수
                        5000000L   // 총 매출
                );

        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.of(mockProfile));
        when(orderRepository.getArtistDashboardStats(eq(TEST_ARTIST_ID), any(), any()))
                .thenReturn(mockStats);
        when(orderRepository.findOrdersByArtist(
                eq(TEST_ARTIST_ID),
                eq(com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED),
                isNull(), isNull(), isNull(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 5)); // 5건 대기중

        // 트렌드 데이터 모킹 (빈 데이터)
        when(orderRepository.findDailyTrendsByArtist(eq(TEST_ARTIST_ID), any(), any()))
                .thenReturn(List.of());

        ArtistMainStatsRequest request = new ArtistMainStatsRequest("30D", null, null, null, "Asia/Seoul");

        // When
        ArtistMainResponse result = artistDashboardService.getMainStats(TEST_ARTIST_ID, request);

        // Then - 프로필 정보
        assertAll(
                () -> assertThat(result.profile().userId()).isEqualTo(TEST_ARTIST_ID),
                () -> assertThat(result.profile().nickname()).isEqualTo("작가")
        );

        // Then - 통계 정보 (팔로우는 고정값, 나머지는 DB 조회)
        assertAll(
                () -> assertThat(result.stats().followerCount()).isEqualTo(150),  // 프로필에서 조회
                () -> assertThat(result.stats().productCount()).isEqualTo(10),    // 프로필에서 조회
                () -> assertThat(result.stats().todaysSales()).isEqualTo(150000), // DB 조회
                () -> assertThat(result.stats().todaysOrders()).isEqualTo(3),     // DB 조회
                () -> assertThat(result.stats().totalSales()).isEqualTo(5000000), // DB 조회
                () -> assertThat(result.stats().totalOrders()).isEqualTo(50),     // DB 조회
                () -> assertThat(result.stats().pendingOrders()).isEqualTo(5)     // DB 조회
        );
    }

    @Test
    @DisplayName("메인 대시보드 트렌드 조회 - 일별 매출/주문 집계 검증")
    void getMainStats_DailyTrends() {
        // Given
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        com.back.domain.artist.entity.ArtistProfile mockProfile = createMockArtistProfileWithStats(mockArtist, 0, 0);

        // 7일간의 트렌드 데이터
        List<com.back.domain.dashboard.artist.dto.DailyTrendDto> dailyTrends = List.of(
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(6), 5L, 250000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(5), 3L, 150000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(4), 8L, 400000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(3), 2L, 100000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(2), 6L, 300000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(1), 4L, 200000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now(), 7L, 350000L)
        );

        // 비교 기간 데이터
        List<com.back.domain.dashboard.artist.dto.DailyTrendDto> compareTrends = List.of(
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(13), 3L, 150000L),
                new com.back.domain.dashboard.artist.dto.DailyTrendDto(
                        java.time.LocalDate.now().minusDays(12), 4L, 200000L)
        );

        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.of(mockProfile));
        when(orderRepository.getArtistDashboardStats(eq(TEST_ARTIST_ID), any(), any()))
                .thenReturn(com.back.domain.dashboard.artist.dto.DashboardStatsDto.empty());
        when(orderRepository.findOrdersByArtist(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        // 첫 번째 호출: 현재 기간 트렌드
        // 두 번째 호출: 비교 기간 트렌드
        when(orderRepository.findDailyTrendsByArtist(eq(TEST_ARTIST_ID), any(), any()))
                .thenReturn(dailyTrends)
                .thenReturn(compareTrends);

        ArtistMainStatsRequest request = new ArtistMainStatsRequest("7D", null, null, null, "Asia/Seoul");

        // When
        ArtistMainResponse result = artistDashboardService.getMainStats(TEST_ARTIST_ID, request);

        // Then - 트렌드 시계열 데이터
        ArtistMainResponse.Trends trends = result.trends();
        
        assertAll(
                // 메타 정보
                () -> assertThat(trends.meta().range()).isEqualTo("7D"),
                () -> assertThat(trends.meta().interval()).isEqualTo("day"),
                () -> assertThat(trends.meta().maxPoints()).isEqualTo(7),
                
                // 매출 시계열
                () -> assertThat(trends.series().sales().points()).hasSize(7),
                () -> assertThat(trends.series().sales().total()).isEqualTo(1750000), // 250k+150k+400k+100k+300k+200k+350k
                
                // 주문 수 시계열
                () -> assertThat(trends.series().orders().points()).hasSize(7),
                () -> assertThat(trends.series().orders().total()).isEqualTo(35), // 5+3+8+2+6+4+7
                
                // 팔로워 시계열 (빈 데이터)
                () -> assertThat(trends.series().followers().points()).isEmpty(),
                () -> assertThat(trends.series().followers().total()).isEqualTo(0)
        );
    }

    // ==================== 헬퍼 메서드 ====================

    private Product createMockProduct(Long id, String name, int price, int discountRate, SellingStatus status) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .discountRate(discountRate)
                .sellingStatus(status)
                .isDeleted(false)
                .build();

        try {
            java.lang.reflect.Field idField = product.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);

            java.lang.reflect.Field createDateField = product.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(product, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields", e);
        }

        return product;
    }

    private User createMockUser(Long id, String name) {
        User user = User.createLocalUser("test@example.com", "password", name, "010-1234-5678");

        try {
            java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return user;
    }

    private Funding createMockFunding(Long id, User user, String title, long targetAmount, FundingStatus status) {
        LocalDateTime now = LocalDateTime.now();

        Funding funding = Funding.builder()
                .user(user)
                .title(title)
                .description("테스트 설명")
                .imageUrl("https://example.com/image.jpg")
                .status(status)
                .targetAmount(targetAmount)
                .collectedAmount(0L)
                .startDate(now.minusDays(10))
                .endDate(now.plusDays(20))
                .participantCount(10)
                .options(new ArrayList<>())
                .news(new ArrayList<>())
                .communities(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        try {
            java.lang.reflect.Field idField = funding.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(funding, id);

            java.lang.reflect.Field createDateField = funding.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(funding, now.minusDays(15));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields", e);
        }

        return funding;
    }

    private com.back.domain.artist.entity.ArtistProfile createMockArtistProfile(User user, String accountNumber) {
        return com.back.domain.artist.entity.ArtistProfile.builder()
                .user(user)
                .artistName("작가")
                .bankAccount(accountNumber)
                .build();
    }

    private com.back.domain.artist.entity.ArtistProfile createMockArtistProfileWithStats(
            User user, int followerCount, int productCount) {
        com.back.domain.artist.entity.ArtistProfile profile = 
                com.back.domain.artist.entity.ArtistProfile.builder()
                .user(user)
                .artistName("작가")
                .bankAccount("123-456-789012")
                .build();

        try {
            java.lang.reflect.Field followerField = profile.getClass().getDeclaredField("followerCount");
            followerField.setAccessible(true);
            followerField.set(profile, followerCount);

            java.lang.reflect.Field productField = profile.getClass().getDeclaredField("productCount");
            productField.setAccessible(true);
            productField.set(profile, productCount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set stats fields", e);
        }

        return profile;
    }

    private void setProductUser(Product product, User user) {
        try {
            java.lang.reflect.Field userField = product.getClass().getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(product, user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user", e);
        }
    }

    private void setOrderStatus(com.back.domain.order.order.entity.Order order, com.back.domain.order.order.entity.OrderStatus status) {
        try {
            java.lang.reflect.Field statusField = order.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set status", e);
        }
    }

    private com.back.domain.order.order.entity.Order createMockOrder(Long id, User user, String orderNumber) {
        com.back.domain.order.order.entity.Order order = com.back.domain.order.order.entity.Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .status(com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(1)
                .totalAmount(java.math.BigDecimal.valueOf(15000))
                .shippingFee(java.math.BigDecimal.ZERO)
                .finalAmount(java.math.BigDecimal.valueOf(15000))
                .paymentMethod(com.back.domain.order.order.entity.PaymentMethod.CARD)
                .orderDate(LocalDateTime.now())
                .build();

        try {
            java.lang.reflect.Field idField = order.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return order;
    }

    private com.back.domain.order.orderItem.entity.OrderItem createMockOrderItem(
            Long id, com.back.domain.order.order.entity.Order order, Product product, int quantity) {

        com.back.domain.order.orderItem.entity.OrderItem orderItem =
                com.back.domain.order.orderItem.entity.OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(quantity)
                        .price(java.math.BigDecimal.valueOf(product.getPrice()))
                        .build();

        try {
            java.lang.reflect.Field idField = orderItem.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(orderItem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return orderItem;
    }

    private com.back.domain.order.refund.entity.RefundItem createMockRefundItem(
            Long id, com.back.domain.order.orderItem.entity.OrderItem orderItem, int quantity) {

        com.back.domain.order.refund.entity.RefundItem refundItem =
                com.back.domain.order.refund.entity.RefundItem.builder()
                        .orderItem(orderItem)
                        .quantity(quantity)
                        .refundPrice(orderItem.getPrice())
                        .build();

        try {
            java.lang.reflect.Field idField = refundItem.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(refundItem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return refundItem;
    }

    private com.back.domain.order.refund.entity.Refund createMockRefund(
            Long id, com.back.domain.order.order.entity.Order order, User user,
            List<com.back.domain.order.refund.entity.RefundItem> refundItems,
            com.back.domain.order.refund.entity.Refund.RefundStatus status) {

        com.back.domain.order.refund.entity.Refund refund =
                com.back.domain.order.refund.entity.Refund.builder()
                        .order(order)
                        .user(user)
                        .status(status)
                        .reason("상품불량")
                        .detailReason("상세 사유")
                        .refundAmount(java.math.BigDecimal.valueOf(15000))
                        .refundMethod(com.back.domain.order.refund.entity.Refund.RefundMethod.ORIGINAL_PAYMENT)
                        .build();

        try {
            java.lang.reflect.Field idField = refund.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(refund, id);

            java.lang.reflect.Field createDateField = refund.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(refund, LocalDateTime.now());

            java.lang.reflect.Field refundItemsField = refund.getClass().getDeclaredField("refundItems");
            refundItemsField.setAccessible(true);
            refundItemsField.set(refund, refundItems);

            for (com.back.domain.order.refund.entity.RefundItem item : refundItems) {
                java.lang.reflect.Field refundField = item.getClass().getDeclaredField("refund");
                refundField.setAccessible(true);
                refundField.set(item, refund);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields", e);
        }

        return refund;
    }

    private com.back.domain.order.exchange.entity.ExchangeItem createMockExchangeItem(
            Long id, com.back.domain.order.orderItem.entity.OrderItem orderItem, int quantity) {

        com.back.domain.order.exchange.entity.ExchangeItem exchangeItem =
                com.back.domain.order.exchange.entity.ExchangeItem.builder()
                        .orderItem(orderItem)
                        .quantity(quantity)
                        .build();

        try {
            java.lang.reflect.Field idField = exchangeItem.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(exchangeItem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return exchangeItem;
    }

    private com.back.domain.order.exchange.entity.Exchange createMockExchange(
            Long id, com.back.domain.order.order.entity.Order order, User user,
            List<com.back.domain.order.exchange.entity.ExchangeItem> exchangeItems,
            com.back.domain.order.exchange.entity.Exchange.ExchangeStatus status,
            com.back.domain.order.exchange.entity.Exchange.ExchangeMethod method) {

        com.back.domain.order.exchange.entity.Exchange exchange =
                com.back.domain.order.exchange.entity.Exchange.builder()
                        .order(order)
                        .user(user)
                        .status(status)
                        .reason("사이즈 변경")
                        .detailReason("L사이즈로 교환 희망")
                        .exchangeMethod(method)
                        .build();

        try {
            java.lang.reflect.Field idField = exchange.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(exchange, id);

            java.lang.reflect.Field createDateField = exchange.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(exchange, LocalDateTime.now());

            java.lang.reflect.Field exchangeItemsField = exchange.getClass().getDeclaredField("exchangeItems");
            exchangeItemsField.setAccessible(true);
            exchangeItemsField.set(exchange, exchangeItems);

            for (com.back.domain.order.exchange.entity.ExchangeItem item : exchangeItems) {
                java.lang.reflect.Field exchangeField = item.getClass().getDeclaredField("exchange");
                exchangeField.setAccessible(true);
                exchangeField.set(item, exchange);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fields", e);
        }

        return exchange;
    }
}
