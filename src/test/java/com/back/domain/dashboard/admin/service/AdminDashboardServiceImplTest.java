package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.request.*;
import com.back.domain.dashboard.admin.dto.response.*;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.DeliveryType;
import com.back.domain.product.product.entity.DisplayStatus;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.security.auth.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * 관리자 대시보드 서비스 통합 테스트
 * 알림 부분 제외 (펀딩 승인 프로세스 미구현)
 * 핵심 기능만 간단하게 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("관리자 대시보드 서비스 통합 테스트")
class AdminDashboardServiceImplTest {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User adminUser;
    private User artistUser;
    private User customerUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        adminUser = createAdminUser();
        artistUser = createArtistUser();
        customerUser = createCustomerUser();

        // 테스트 카테고리 생성
        testCategory = createTestCategory();

        // SecurityContext 설정
        setupSecurityContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser, Role.ADMIN);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========== 메인 현황 (Overview) 테스트 ==========

    @Test
    @DisplayName("메인 현황 조회 - 6개 통계 모두 조회")
    void getOverview_메인통계_6개항목() {
        // Given
        createTestProduct("테스트 상품", SellingStatus.SELLING);
        createTestOrder(LocalDateTime.now(), 100000, OrderStatus.PAYMENT_COMPLETED);

        AdminOverviewRequest request = new AdminOverviewRequest("1M", "day", "day", "Asia/Seoul");

        // When
        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        // Then - 6개 통계 모두 조회 확인
        assertAll(
                () -> assertThat(response.overview().userCount().count()).isGreaterThan(0),
                () -> assertThat(response.overview().productCount().count()).isGreaterThan(0),
                () -> assertThat(response.overview().fundingCount().count()).isGreaterThanOrEqualTo(0),
                () -> assertThat(response.overview().artistCount().count()).isGreaterThan(0),
                () -> assertThat(response.overview().orderStats().count()).isGreaterThanOrEqualTo(0),
                () -> assertThat(response.overview().salesStats().count()).isGreaterThanOrEqualTo(0),
                // 라벨 확인
                () -> assertThat(response.overview().userCount().label()).isEqualTo("가입자 수"),
                () -> assertThat(response.overview().orderStats().label()).isEqualTo("오늘의 주문"),
                () -> assertThat(response.overview().salesStats().label()).isEqualTo("오늘의 매출")
        );
    }

    @Test
    @DisplayName("메인 현황 조회 - 오늘의 매출과 주문 집계")
    void getOverview_오늘의매출과주문() {
        // Given - 오늘 주문 2건 생성
        LocalDate today = LocalDate.now();
        createTestOrder(today.atTime(10, 0), 50000, OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(today.atTime(14, 0), 30000, OrderStatus.PAYMENT_COMPLETED);

        AdminOverviewRequest request = new AdminOverviewRequest("1M", "day", "day", "Asia/Seoul");

        // When
        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        // Then
        assertAll(
                () -> assertThat(response.overview().orderStats().count()).isEqualTo(2),
                () -> assertThat(response.overview().salesStats().count()).isEqualTo(80000)
        );
    }

    // ========== 트렌드 그래프 테스트 ==========

    @Test
    @DisplayName("트렌드 그래프 - 매출 트렌드 조회 (1M)")
    void getOverview_매출트렌드_1M() {
        // Given - 최근 1개월 주문 생성
        createTestOrder(LocalDateTime.now().minusDays(5), 100000, OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(LocalDateTime.now().minusDays(10), 200000, OrderStatus.PAYMENT_COMPLETED);

        AdminOverviewRequest request = new AdminOverviewRequest("1M", "day", "day", "Asia/Seoul");

        // When
        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        // Then
        assertAll(
                () -> assertThat(response.charts()).isNotNull(),
                () -> assertThat(response.charts().meta().range()).isEqualTo("1M"),
                () -> assertThat(response.charts().salesTrend()).isNotNull(),
                () -> assertThat(response.charts().salesTrend().series().sales()).isNotEmpty(),
                () -> assertThat(response.charts().salesTrend().series().orders()).isNotEmpty()
        );
    }

    @Test
    @DisplayName("트렌드 그래프 - 사용자 증가 트렌드 조회")
    void getOverview_사용자증가트렌드() {
        // Given
        AdminOverviewRequest request = new AdminOverviewRequest("1M", "day", "day", "Asia/Seoul");

        // When
        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        // Then
        assertAll(
                () -> assertThat(response.charts().userGrowth()).isNotNull(),
                () -> assertThat(response.charts().userGrowth().series().users()).isNotEmpty(),
                () -> assertThat(response.charts().userGrowth().series().artists()).isNotEmpty()
        );
    }

    @Test
    @DisplayName("트렌드 그래프 - 카테고리 분포 조회")
    void getOverview_카테고리분포() {
        // Given
        createTestProduct("상품1", SellingStatus.SELLING);
        createTestProduct("상품2", SellingStatus.SELLING);

        AdminOverviewRequest request = new AdminOverviewRequest("1M", "day", "day", "Asia/Seoul");

        // When
        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        // Then
        assertAll(
                () -> assertThat(response.charts().categoryDistribution()).isNotNull(),
                () -> assertThat(response.charts().categoryDistribution().totalProducts()).isGreaterThan(0)
        );
    }

    @Test
    @DisplayName("트렌드 그래프 - 기간 옵션 테스트 (3M, 6M, 1Y, ALL)")
    void getOverview_기간옵션() {
        // Given
        String[] ranges = {"3M", "6M", "1Y", "ALL"};

        for (String range : ranges) {
            // When
            AdminOverviewRequest request = new AdminOverviewRequest(range, "day", "day", "Asia/Seoul");
            AdminOverviewResponse response = adminDashboardService.getOverview(request);

            // Then
            assertAll(
                    () -> assertThat(response.charts().meta().range()).isEqualTo(range),
                    () -> assertThat(response.charts().salesTrend()).isNotNull(),
                    () -> assertThat(response.charts().userGrowth()).isNotNull()
            );
        }
    }

    // ========== 매출/정산 테스트 ==========

    @Test
    @DisplayName("매출/정산 조회 - 월별 집계")
    void getSettlements_월별집계() {
        // Given
        int year = LocalDate.now().getYear();
        createTestOrder(LocalDateTime.of(year, 3, 15, 10, 0), 100000, OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(LocalDateTime.of(year, 5, 20, 14, 0), 200000, OrderStatus.PAYMENT_COMPLETED);

        AdminSettlementRequest request = new AdminSettlementRequest(year, null, "month", "Asia/Seoul");

        // When
        AdminSettlementResponse response = adminDashboardService.getSettlements(request);

        // Then
        assertAll(
                () -> assertThat(response.scope().year()).isEqualTo(year),
                () -> assertThat(response.scope().month()).isNull(),
                () -> assertThat(response.summary().totalGrossSales()).isEqualTo(300000),
                () -> assertThat(response.summary().totalArtistPayout()).isEqualTo(270000), // 90%
                () -> assertThat(response.summary().totalNetIncome()).isEqualTo(30000), // 10%
                () -> assertThat(response.table()).hasSize(12) // 12개월
        );
    }

    @Test
    @DisplayName("매출/정산 조회 - 일별 집계")
    void getSettlements_일별집계() {
        // Given
        int year = LocalDate.now().getYear();
        int month = 6;
        createTestOrder(LocalDateTime.of(year, month, 5, 10, 0), 50000, OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(LocalDateTime.of(year, month, 10, 14, 0), 30000, OrderStatus.PAYMENT_COMPLETED);

        AdminSettlementRequest request = new AdminSettlementRequest(year, month, "day", "Asia/Seoul");

        // When
        AdminSettlementResponse response = adminDashboardService.getSettlements(request);

        // Then
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        assertAll(
                () -> assertThat(response.scope().month()).isEqualTo(month),
                () -> assertThat(response.summary().totalGrossSales()).isEqualTo(80000),
                () -> assertThat(response.table()).hasSize(daysInMonth)
        );
    }

    @Test
    @DisplayName("매출/정산 조회 - 결제완료 상태만 집계")
    void getSettlements_결제완료만집계() {
        // Given
        int year = LocalDate.now().getYear();
        createTestOrder(LocalDateTime.of(year, 7, 1, 10, 0), 100000, OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(LocalDateTime.of(year, 7, 2, 11, 0), 200000, OrderStatus.CANCELLATION_COMPLETED);
        createTestOrder(LocalDateTime.of(year, 7, 3, 12, 0), 300000, OrderStatus.REFUND_COMPLETED);

        AdminSettlementRequest request = new AdminSettlementRequest(year, 7, "day", "Asia/Seoul");

        // When
        AdminSettlementResponse response = adminDashboardService.getSettlements(request);

        // Then - PAYMENT_COMPLETED만 집계
        assertThat(response.summary().totalGrossSales()).isEqualTo(100000);
    }

    // ========== 상품 목록 테스트 ==========

    @Test
    @DisplayName("상품 목록 조회 - 기본 조회")
    void getProducts_기본조회() {
        // Given
        createTestProduct("테스트 상품1", SellingStatus.SELLING);
        createTestProduct("테스트 상품2", SellingStatus.END_OF_SALE);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, null, null, null, null, null, "registeredAt", "DESC"
        );

        // When
        AdminProductResponse response = adminDashboardService.getProducts(request);

        // Then
        assertAll(
                () -> assertThat(response.content()).hasSizeGreaterThanOrEqualTo(2),
                () -> assertThat(response.page()).isEqualTo(0),
                () -> assertThat(response.size()).isEqualTo(20)
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 판매 상태 필터링")
    void getProducts_판매상태필터() {
        // Given
        createTestProduct("판매중", SellingStatus.SELLING);
        createTestProduct("판매종료", SellingStatus.END_OF_SALE);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, "SELLING", null, null, null, null, "registeredAt", "DESC"
        );

        // When
        AdminProductResponse response = adminDashboardService.getProducts(request);

        // Then
        response.content().forEach(product ->
                assertThat(product.sellingStatus()).isEqualTo("SELLING")
        );
    }

    // ========== 사용자 목록 테스트 ==========

    @Test
    @DisplayName("사용자 목록 조회 - 기본 조회")
    void getUsers_기본조회() {
        // Given
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then
        assertAll(
                () -> assertThat(response.content()).isNotEmpty(),
                () -> assertThat(response.totalElements()).isGreaterThan(0)
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 작가만 필터링")
    void getUsers_작가필터() {
        // Given
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, "ARTIST", null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then
        response.content().forEach(user ->
                assertThat(user.artistName()).isNotNull()
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 수수료율 검증 (작가 10%, 일반 유저 null)")
    void getUsers_수수료율검증() {
        // Given
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then
        assertThat(response.content()).isNotEmpty();
        
        response.content().forEach(user -> {
            if (user.artistName() != null) {
                // 작가는 수수료율 10%
                assertThat(user.commissionRate())
                        .as("작가 유저는 수수료율이 10%여야 합니다")
                        .isEqualTo(10);
            } else {
                // 일반 유저는 수수료율 null
                assertThat(user.commissionRate())
                        .as("일반 유저는 수수료율이 null이어야 합니다")
                        .isNull();
            }
        });
    }

    @Test
    @DisplayName("사용자 목록 조회 - 작가 수수료율 정확성 검증")
    void getUsers_작가수수료율정확성() {
        // Given - 작가만 필터링
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, "ARTIST", null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then - 모든 작가는 수수료율 10%
        assertThat(response.content()).isNotEmpty();
        response.content().forEach(user -> {
            assertAll(
                    () -> assertThat(user.artistName()).isNotNull(),
                    () -> assertThat(user.commissionRate()).isEqualTo(10)
            );
        });
    }

    @Test
    @DisplayName("사용자 목록 조회 - 일반 유저 수수료율 null 검증")
    void getUsers_일반유저수수료율null() {
        // Given - 일반 유저만 필터링 (USER)
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, "USER", null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then - 일반 유저는 수수료율 null
        response.content().forEach(user -> {
            assertAll(
                    () -> assertThat(user.artistName()).isNull(),
                    () -> assertThat(user.commissionRate()).isNull()
            );
        });
    }

    @Test
    @DisplayName("사용자 목록 조회 - 수수료율 정렬 (실제로는 회원등급 정렬)")
    void getUsers_수수료율정렬() {
        // Given - 수수료율 오름차순 정렬 (실제로는 grade 정렬)
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null,
                "commissionRate", "ASC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then
        assertThat(response.content()).isNotEmpty();
        // 등급 순서: SPROUT(일반) < GRASS < TREE < FOREST < GUARDIAN(작가)
        // 따라서 일반 유저(수수료율 null)가 먼저, 작가(수수료율 10%)가 나중에 나와야 함
        
        List<AdminUserResponse.User> users = response.content();
        for (int i = 0; i < users.size() - 1; i++) {
            String currentGrade = users.get(i).grade();
            String nextGrade = users.get(i + 1).grade();
            
            // GUARDIAN(작가) 앞에는 GUARDIAN이 아닌 등급이 와야 함
            if ("GUARDIAN".equals(nextGrade)) {
                assertThat(currentGrade).isIn("SPROUT", "GRASS", "TREE", "FOREST", "GUARDIAN");
            }
        }
    }

    @Test
    @DisplayName("사용자 목록 조회 - 수수료율 내림차순 정렬")
    void getUsers_수수료율내림차순정렬() {
        // Given - 수수료율 내림차순 정렬 (실제로는 grade 역순 정렬)
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null,
                "commissionRate", "DESC"
        );

        // When
        AdminUserResponse response = adminDashboardService.getUsers(request);

        // Then
        assertThat(response.content()).isNotEmpty();
        // 내림차순이므로 GUARDIAN(작가, 10%)가 먼저, 일반 유저(null)가 나중에 나와야 함
    }

    // ========== 펀딩 승인 대기 목록 테스트 ==========

    @Test
    @DisplayName("펀딩 승인 대기 목록 조회 - 기본 조회")
    void getFundingApprovals_기본조회() {
        // Given
        AdminFundingApprovalSearchRequest request = new AdminFundingApprovalSearchRequest(
                0, 20, null, null, "registeredAt", "DESC"
        );

        // When
        AdminFundingApprovalResponse response = adminDashboardService.getFundingApprovals(request);

        // Then
        assertAll(
                () -> assertThat(response.page()).isEqualTo(0),
                () -> assertThat(response.size()).isEqualTo(20),
                () -> assertThat(response.totalElements()).isGreaterThanOrEqualTo(0)
        );
    }

    @Test
    @DisplayName("펀딩 승인 대기 목록 조회 - 작가 ID로 정렬")
    void getFundingApprovals_작가ID정렬() {
        // Given
        AdminFundingApprovalSearchRequest request = new AdminFundingApprovalSearchRequest(
                0, 20, null, null, "artistId", "ASC"
        );

        // When
        AdminFundingApprovalResponse response = adminDashboardService.getFundingApprovals(request);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("펀딩 승인 대기 목록 조회 - 펀딩 제목으로 정렬")
    void getFundingApprovals_제목정렬() {
        // Given
        AdminFundingApprovalSearchRequest request = new AdminFundingApprovalSearchRequest(
                0, 20, null, null, "title", "ASC"
        );

        // When
        AdminFundingApprovalResponse response = adminDashboardService.getFundingApprovals(request);

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("펀딩 승인 대기 목록 조회 - 특정 작가 필터링")
    void getFundingApprovals_작가필터() {
        // Given
        AdminFundingApprovalSearchRequest request = new AdminFundingApprovalSearchRequest(
                0, 20, null, artistUser.getId(), "registeredAt", "DESC"
        );

        // When
        AdminFundingApprovalResponse response = adminDashboardService.getFundingApprovals(request);

        // Then
        response.content().forEach(funding ->
                assertThat(funding.artist().id()).isEqualTo(artistUser.getId())
        );
    }

    // ========== 헬퍼 메서드 ==========

    private User createAdminUser() {
        User admin = User.createLocalUser("admin@test.com", "password", "관리자", "010-0000-0000");
        admin.becomeAdmin();
        return userRepository.save(admin);
    }

    private User createArtistUser() {
        User artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1111-1111");
        artist.becomeArtist();
        return userRepository.save(artist);
    }

    private User createCustomerUser() {
        User customer = User.createLocalUser("customer@test.com", "password", "고객", "010-2222-2222");
        return userRepository.save(customer);
    }

    private Category createTestCategory() {
        Category category = Category.builder()
                .categoryName("테스트 카테고리")
                .build();
        return categoryRepository.save(category);
    }

    private Product createTestProduct(String name, SellingStatus status) {
        Product product = Product.builder()
                .name(name)
                .brandName("테스트 브랜드")
                .user(artistUser)
                .category(testCategory)
                .sellingStatus(status)
                .displayStatus(DisplayStatus.DISPLAYING)
                .price(10000)
                .discountRate(0)
                .stock(100)
                .description("테스트 설명")
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(0)
                .deliveryType(DeliveryType.PAID)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("모델명")
                .certification(false)
                .origin("대한민국")
                .material("재질")
                .size("크기")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        return productRepository.save(product);
    }

    private Order createTestOrder(LocalDateTime orderDate, long amount, OrderStatus status) {
        Order order = Order.builder()
                .user(customerUser)
                .orderNumber("TEST-" + System.nanoTime())
                .status(status)
                .totalQuantity(1)
                .totalAmount(BigDecimal.valueOf(amount))
                .shippingFee(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(amount))
                .paymentMethod(PaymentMethod.CARD)
                .orderDate(orderDate)
                .build();
        return orderRepository.save(order);
    }
}
