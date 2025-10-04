package com.back.domain.dashboard.admin.service;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.dashboard.admin.dto.request.*;
import com.back.domain.dashboard.admin.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * AdminDashboardServiceImpl 통합 테스트
 * 실제 DB 연동을 통한 비즈니스 로직 검증
 * 2025.10.02 JWT 표준 패턴 적용 - SecurityContext Mock 사용
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
    private FundingRepository fundingRepository;

    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User artistUser;
    private User adminUser;
    private User customerUser;
    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        // TestInitData에서 생성된 사용자 조회
        artistUser = userRepository.findByEmail("user1@user.com").orElseThrow();
        customerUser = userRepository.findByEmail("user2@user.com").orElseThrow();
        
        // Admin 사용자 조회 또는 생성
        adminUser = userRepository.findByEmail("admin@admin.com")
                .orElseGet(() -> {
                    User admin = User.createLocalUser("admin@admin.com", "password", "Admin", "010-0000-0000");
                    admin.becomeAdmin();
                    return userRepository.save(admin);
                });

        // TestInitData에서 생성된 카테고리 조회
        defaultCategory = categoryRepository.findAll().stream()
                .filter(c -> "회화".equals(c.getCategoryName()))
                .findFirst()
                .orElseThrow();

        // SecurityContext 설정
        setupSecurityContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * SecurityContext에 Admin 인증 정보 설정
     */
    private void setupSecurityContext() {
        CustomUserDetails adminDetails = new CustomUserDetails(adminUser, Role.ADMIN);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("전체 현황 조회 - 실제 DB 데이터로 검증")
    void getOverview_WithRealData() {
        // Given - TestInitData에 이미 데이터 존재
        long expectedUserCount = userRepository.count();
        long expectedProductCount = productRepository.count();
        long expectedFundingCount = fundingRepository.count();

        AdminOverviewRequest request = new AdminOverviewRequest(
                "1M", "DAY", "MONTH", "Asia/Seoul"
        );

        // When
        AdminOverviewResponse result = adminDashboardService.getOverview(request);

        // Then - 실제 DB 연동된 부분만 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.timezone()).isEqualTo("Asia/Seoul"),
                // 실제 DB 데이터와 일치
                () -> assertThat(result.overview().userCount().count()).isEqualTo(expectedUserCount),
                () -> assertThat(result.overview().productCount().count()).isEqualTo(expectedProductCount),
                () -> assertThat(result.overview().fundingCount().count()).isEqualTo(expectedFundingCount),
                // Order 관련은 더미 (0) - TODO: Order 테이블 연동 후 검증
                () -> assertThat(result.overview().orderStats().count()).isEqualTo(0L),
                () -> assertThat(result.overview().salesStats().count()).isEqualTo(0L),
                // 알림 데이터는 실제 DB
                () -> assertThat(result.alerts()).isNotNull()
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 실제 DB 데이터로 검증")
    void getProducts_WithRealData() {
        // Given - 테스트 상품 생성
        Product product1 = createTestProduct("테스트 상품 1", SellingStatus.SELLING);
        Product product2 = createTestProduct("테스트 상품 2", SellingStatus.END_OF_SALE);
        productRepository.save(product1);
        productRepository.save(product2);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, null, null, null, null, null, "registeredAt", "DESC"
        );

        // When
        AdminProductResponse result = adminDashboardService.getProducts(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.totalElements()).isGreaterThanOrEqualTo(2),
                () -> assertThat(result.page()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(20),
                // 상품 데이터 검증
                () -> result.content().forEach(product -> {
                    assertThat(product.productId()).isNotNull();
                    assertThat(product.name()).isNotBlank();
                    assertThat(product.artist()).isNotNull();
                    assertThat(product.artist().name()).isNotBlank();
                })
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 판매 상태 필터링")
    void getProducts_WithSellingStatusFilter() {
        // Given
        Product selling = createTestProduct("판매중 상품", SellingStatus.SELLING);
        Product ended = createTestProduct("판매종료 상품", SellingStatus.END_OF_SALE);
        productRepository.save(selling);
        productRepository.save(ended);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, null, "SELLING", null, null, null, null, "registeredAt", "DESC"
        );

        // When
        AdminProductResponse result = adminDashboardService.getProducts(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> result.content().forEach(product ->
                        assertThat(product.sellingStatus()).isEqualTo("SELLING"))
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 키워드 검색")
    void getProducts_WithKeywordSearch() {
        // Given
        Product product = createTestProduct("특별한 작품", SellingStatus.SELLING);
        productRepository.save(product);

        AdminProductSearchRequest request = new AdminProductSearchRequest(
                0, 20, "특별한", null, null, null, null, null, "registeredAt", "DESC"
        );

        // When
        AdminProductResponse result = adminDashboardService.getProducts(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.content().getFirst().name()).contains("특별한")
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 실제 DB 데이터로 검증")
    void getUsers_WithRealData() {
        // Given - TestInitData에 이미 사용자 존재
        long expectedUserCount = userRepository.count();

        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, null, null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse result = adminDashboardService.getUsers(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.totalElements()).isEqualTo(expectedUserCount),
                () -> assertThat(result.page()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(20),
                // 사용자 데이터 검증
                () -> result.content().forEach(user -> {
                    assertThat(user.userId()).isNotNull();
                    assertThat(user.nickname()).isNotBlank();
                    assertThat(user.grade()).isNotBlank();
                })
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 - 역할 필터링 (작가만)")
    void getUsers_WithRoleFilter() {
        // Given
        AdminUserSearchRequest request = new AdminUserSearchRequest(
                0, 20, null, "ARTIST", null, null, null, null, null, "joinedAt", "DESC"
        );

        // When
        AdminUserResponse result = adminDashboardService.getUsers(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> result.content().forEach(user ->
                        assertThat(user.artistName()).isNotNull())
        );
    }

    @Test
    @DisplayName("매출/정산 조회 - 기본 조회")
    void getSettlements_Success() {
        // Given
        AdminSettlementRequest request = new AdminSettlementRequest(
                2025, null, "MONTH", "Asia/Seoul"
        );

        // When
        AdminSettlementResponse result = adminDashboardService.getSettlements(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.scope()).isNotNull(),
                () -> assertThat(result.scope().year()).isEqualTo(2025),
                () -> assertThat(result.scope().month()).isNull(),
                () -> assertThat(result.granularity()).isEqualTo("MONTH"),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.chart()).isNotNull(),
                () -> assertThat(result.table()).isNotNull()
        );
    }

    @Test
    @DisplayName("펀딩 목록 조회 - 실제 DB 데이터로 검증")
    void getFundings_WithRealData() {
        // Given - TestInitData에 이미 펀딩 존재
        long expectedFundingCount = fundingRepository.count();

        AdminFundingSearchRequest request = new AdminFundingSearchRequest(
                0, 20, null, null, null, null, null, null, null, null, null, null, "endDate", "ASC"
        );

        // When
        AdminFundingResponse result = adminDashboardService.getFundings(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.totalElements()).isEqualTo(expectedFundingCount),
                () -> assertThat(result.page()).isEqualTo(0),
                () -> assertThat(result.size()).isEqualTo(20),
                // 펀딩 데이터 검증
                () -> result.content().forEach(funding -> {
                    assertThat(funding.fundingId()).isNotNull();
                    assertThat(funding.title()).isNotBlank();
                    assertThat(funding.artist()).isNotNull();
                    // 달성률 계산 검증
                    if (funding.targetAmount() > 0) {
                        int expectedRate = (int) ((funding.currentAmount() * 100) / funding.targetAmount());
                        assertThat(funding.achievementRate()).isEqualTo(expectedRate);
                    }
                })
        );
    }

    @Test
    @DisplayName("펀딩 목록 조회 - 상태 필터링")
    void getFundings_WithStatusFilter() {
        // Given - 추가 펀딩 생성
        Funding openFunding = createTestFunding("진행중 펀딩", FundingStatus.OPEN);
        fundingRepository.save(openFunding);

        AdminFundingSearchRequest request = new AdminFundingSearchRequest(
                0, 20, null, "OPEN", null, null, null, null, null, null, null, null, "endDate", "ASC"
        );

        // When
        AdminFundingResponse result = adminDashboardService.getFundings(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> result.content().forEach(funding ->
                        assertThat(funding.status()).isEqualTo("OPEN"))
        );
    }

    @Test
    @DisplayName("입점 신청 목록 조회 - 실제 DB 데이터로 검증")
    void getArtistApplications_WithRealData() {
        // Given - 테스트 입점 신청 생성
        ArtistApplication application1 = createTestApplication("신청자1");
        ArtistApplication application2 = createTestApplication("신청자2");
        artistApplicationRepository.save(application1);
        artistApplicationRepository.save(application2);

        AdminArtistApplicationSearchRequest request = new AdminArtistApplicationSearchRequest(
                0, 20, null, null, null, null, "submittedAt", "DESC"
        );

        // When
        AdminArtistApplicationResponse result = adminDashboardService.getArtistApplications(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> assertThat(result.totalElements()).isGreaterThanOrEqualTo(2),
                // Summary 검증
                () -> {
                    int total = result.summary().totalApplications();
                    int pending = result.summary().pending();
                    int approved = result.summary().approved();
                    int rejected = result.summary().rejected();
                    assertThat(total).isEqualTo(pending + approved + rejected);
                },
                // 신청 데이터 검증
                () -> result.content().forEach(app -> {
                    assertThat(app.applicationId()).isNotNull();
                    assertThat(app.artist().name()).isNotBlank();
                    assertThat(app.status()).isNotBlank();
                })
        );
    }

    @Test
    @DisplayName("입점 신청 목록 조회 - 상태 필터링 (대기중)")
    void getArtistApplications_WithPendingFilter() {
        // Given
        ArtistApplication pending = createTestApplication("대기중 신청자");
        artistApplicationRepository.save(pending);

        AdminArtistApplicationSearchRequest request = new AdminArtistApplicationSearchRequest(
                0, 20, null, "PENDING", null, null, "submittedAt", "DESC"
        );

        // When
        AdminArtistApplicationResponse result = adminDashboardService.getArtistApplications(request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.content()).isNotEmpty(),
                () -> result.content().forEach(app ->
                        assertThat(app.status()).isEqualTo("PENDING"))
        );
    }

    @Test
    @DisplayName("입점 신청 상세 조회 - 실제 DB 데이터로 검증")
    void getArtistApplicationDetail_WithRealData() {
        // Given - 테스트 입점 신청 생성
        ArtistApplication application = createTestApplication("상세조회 테스트");
        ArtistApplication saved = artistApplicationRepository.save(application);

        // When
        AdminArtistApplicationDetailResponse result = 
                adminDashboardService.getArtistApplicationDetail(saved.getId());

        // Then - 필수 정보 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.applicationId()).isEqualTo(saved.getId()),
                () -> assertThat(result.status()).isEqualTo("PENDING"),
                () -> assertThat(result.artist()).isNotNull(),
                () -> assertThat(result.artist().userId()).isNotNull(),
                () -> assertThat(result.contact()).isNotNull(),
                () -> assertThat(result.contact().email()).contains("@"),
                () -> assertThat(result.business()).isNotNull(),
                () -> assertThat(result.business().registrationNo()).isNotBlank(),
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.permissions()).isNotNull()
        );
    }

    // ===== 헬퍼 메서드 =====

    /**
     * 테스트용 상품 생성
     */
    private Product createTestProduct(String name, SellingStatus status) {
        return Product.builder()
                .name(name)
                .brandName("테스트 브랜드")
                .user(artistUser)
                .category(defaultCategory)
                .sellingStatus(status)
                .displayStatus(DisplayStatus.DISPLAYING)
                .price(10000)
                .discountRate(0)
                .stock(100)
                .description("테스트 상품 설명")
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(DeliveryType.FREE)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("테스트 모델")
                .certification(false)
                .origin("대한민국")
                .material("종이")
                .size("A4")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
    }

    /**
     * 테스트용 펀딩 생성
     */
    private Funding createTestFunding(String title, FundingStatus status) {
        LocalDateTime now = LocalDateTime.now();

        return Funding.create(
                artistUser,                                 // 작성자
                title,                                      // 제목
                "테스트 펀딩 설명",                              // 설명
                "https://example.com/image.jpg",            // 이미지
                1_000_000L,                                 // 목표 금액 (long)
                now.plusDays(1),                            // 시작일: 현재 이후
                now.plusDays(25),                           // 종료일: 시작일 이후
                status,                                     // 초기 상태
                List.of(FundingOption.create("기본 옵션", 10_000L, 100, 1)) // 옵션들
        );
    }

    /**
     * 테스트용 입점 신청 생성
     */
    private ArtistApplication createTestApplication(String artistName) {
        return ArtistApplication.builder()
                .user(customerUser)
                .ownerName("대표자명")
                .artistName(artistName)
                .email("test@test.com")
                .phone("010-1234-5678")
                .businessNumber("123-45-67890")
                .businessAddress("서울특별시 강남구")
                .businessAddressDetail("123동 456호")
                .mainProducts("회화,조각")
                .build();
    }
}
