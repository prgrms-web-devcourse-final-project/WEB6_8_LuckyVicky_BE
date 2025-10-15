package com.back.domain.dashboard.customer.service;


import com.back.domain.dashboard.customer.dto.request.ArtistApplicationSearchRequest;
import com.back.domain.dashboard.customer.dto.request.FundingSearchRequest;
import com.back.domain.dashboard.customer.dto.response.ArtistApplicationResponse;
import com.back.domain.dashboard.customer.dto.response.FundingResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingContribution;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * DashboardServiceImpl 테스트
 * 핵심 비즈니스 로직과 데이터 일관성에 집중
 * 2025.10.14 수정 - 팔로우 작가 조회 테스트 추가
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("대시보드 서비스 구현체 테스트")
class DashboardServiceImplTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private FundingContributionRepository fundingContributionRepository;

    @Autowired
    private com.back.domain.order.order.repository.OrderRepository orderRepository;

    @Autowired
    private com.back.domain.product.product.repository.ProductRepository productRepository;

    @Autowired
    private com.back.domain.artist.repository.ArtistApplicationRepository artistApplicationRepository;

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    // 캐시 테스트용 Repository
    @Autowired
    private com.back.domain.payment.cash.repository.CashTransactionRepository cashTransactionRepository;

    @Autowired
    private com.back.domain.payment.moriCash.repository.MoriCashPaymentRepository moriCashPaymentRepository;

    @Autowired
    private com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository moriCashBalanceRepository;

    private User testBuyer;
    private User testArtist;
    private Funding activeFunding;
    private Funding endedFunding;
    private com.back.domain.product.product.entity.Product testProduct;
    private com.back.domain.order.order.entity.Order testOrder;

    @BeforeEach
    void setUp() {
        // 테스트 구매자 생성
        testBuyer = User.createLocalUser(
                "test-buyer@example.com",
                "password",
                "테스트구매자",
                "01012345678"
        );
        testBuyer = userRepository.save(testBuyer);

        // 테스트 작가 생성
        testArtist = User.createLocalUser(
                "test-artist@example.com",
                "password",
                "테스트작가",
                "01087654321"
        );
        testArtist.becomeArtist();
        testArtist = userRepository.save(testArtist);

        // 테스트 카테고리 생성 (funding에 필수)
        Category category = categoryRepository.findById((1L))
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().categoryName("테스트 카테고리").build()
                ));

        // 진행중인 펀딩 생성 (옵션 포함)
        activeFunding = Funding.builder()
                .user(testArtist)
                .title("진행중인 펀딩")
                .description("테스트 펀딩입니다")
                .category(category)
                .imageUrl("https://example.com/image1.jpg")
                .targetAmount(1000000L)
                .collectedAmount(500000L)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .status(FundingStatus.OPEN)
                .participantCount(5)
                .build();
        activeFunding = fundingRepository.save(activeFunding);

        // 종료된 펀딩 생성 (옵션 포함)
        endedFunding = Funding.builder()
                .user(testArtist)
                .title("종료된 펀딩")
                .description("테스트 펀딩입니다")
                .category(category)
                .imageUrl("https://example.com/image2.jpg")
                .targetAmount(500000L)
                .collectedAmount(600000L)
                .startDate(LocalDateTime.now().minusDays(35))
                .endDate(LocalDateTime.now().minusDays(5))
                .status(FundingStatus.SUCCESS)
                .participantCount(10)
                .build();
        endedFunding = fundingRepository.save(endedFunding);

        // 펀딩 참여 내역 생성 (진행중)
        FundingContribution activeContribution = FundingContribution.builder()
                .funding(activeFunding)
                .buyer(testBuyer)
                .quantity(2)
                .totalAmount(50000L)
                .paidAt(LocalDateTime.now().minusDays(3))
                .build();
        fundingContributionRepository.save(activeContribution);

        // 펀딩 참여 내역 생성 (종료)
        FundingContribution endedContribution = FundingContribution.builder()
                .funding(endedFunding)
                .buyer(testBuyer)
                .quantity(1)
                .totalAmount(30000L)
                .paidAt(LocalDateTime.now().minusDays(10))
                .build();
        fundingContributionRepository.save(endedContribution);

        // 테스트 카테고리 생성 (Product에 필수)
        com.back.domain.product.category.entity.Category testCategory =
                com.back.domain.product.category.entity.Category.builder()
                        .categoryName("테스트카테고리")
                        .build();

        // 카테고리 먼저 저장 (Product가 참조하므로)
        com.back.domain.product.category.repository.CategoryRepository categoryRepository =
                applicationContext.getBean(com.back.domain.product.category.repository.CategoryRepository.class);
        testCategory = categoryRepository.save(testCategory);

        // 테스트 상품 생성
        testProduct = com.back.domain.product.product.entity.Product.builder()
                .category(testCategory)
                .user(testArtist)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .price(10000)
                .discountRate(0)
                .stock(100)
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(com.back.domain.product.product.entity.DeliveryType.PAID)
                .description("테스트 상품 설명")
                .sellingStatus(com.back.domain.product.product.entity.SellingStatus.SELLING)
                .displayStatus(com.back.domain.product.product.entity.DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("TEST-001")
                .certification(false)
                .origin("한국")
                .material("플라스틱")
                .size("10x10cm")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        testProduct = productRepository.save(testProduct);

        // 테스트 주문 생성 (결제완료 상태)
        testOrder = com.back.domain.order.order.entity.Order.builder()
                .user(testBuyer)
                .orderNumber("ORD" + System.currentTimeMillis())
                .status(com.back.domain.order.order.entity.OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(2)
                .totalAmount(java.math.BigDecimal.valueOf(20000))
                .shippingFee(java.math.BigDecimal.valueOf(3000))
                .finalAmount(java.math.BigDecimal.valueOf(23000))
                .shippingAddress1("서울시 강남구")
                .shippingAddress2("테헤란로 123")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .paymentMethod(com.back.domain.order.order.entity.PaymentMethod.CARD)
                .orderDate(LocalDateTime.now().minusDays(2))
                .build();

        // 주문 아이템 추가
        com.back.domain.order.orderItem.entity.OrderItem orderItem =
                com.back.domain.order.orderItem.entity.OrderItem.builder()
                        .order(testOrder)
                        .product(testProduct)
                        .quantity(2)
                        .price(java.math.BigDecimal.valueOf(10000))
                        .build();

        testOrder.addOrderItem(orderItem);
        testOrder = orderRepository.save(testOrder);
    }

    // ==================== Funding 실제 DB 연동 테스트 ====================

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 실제 DB 연동 및 8가지 상태 확인")
    void getFundingParticipations_ReturnsRealDataWithStatus() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        FundingResponse.Participation activeParticipation = result.getContent().stream()
                .filter(p -> p.title().equals("진행중인 펀딩"))
                .findFirst()
                .orElseThrow();

        FundingResponse.Participation successParticipation = result.getContent().stream()
                .filter(p -> p.title().equals("종료된 펀딩"))
                .findFirst()
                .orElseThrow();

        // Then: 실제 DB 연동 및 8가지 상태 확인
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getSummary()).isNull(),
                // OPEN 상태 확인
                () -> assertThat(activeParticipation.status()).isEqualTo("OPEN"),
                () -> assertThat(activeParticipation.statusText()).isEqualTo("진행중"),
                // SUCCESS 상태 확인
                () -> assertThat(successParticipation.status()).isEqualTo("SUCCESS"),
                () -> assertThat(successParticipation.statusText()).isEqualTo("성공")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (OPEN)")
    void getFundingParticipations_FiltersOpenStatus() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, "OPEN", null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("OPEN"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("진행중인 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (SUCCESS)")
    void getFundingParticipations_FiltersSuccessStatus() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, "SUCCESS", null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("SUCCESS"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("종료된 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 키워드 검색")
    void getFundingParticipations_SearchesByKeyword() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, "진행중", "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).title()).contains("진행중")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 페이징 처리")
    void getFundingParticipations_HandlesPagination() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 1, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.isHasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 날짜 포맷 검증")
    void getFundingParticipations_FormatsPaidDate() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertThat(result.getContent().get(0).paidDate())
                .matches("^\\d{4}\\. \\d{2}\\. \\d{2}$");
    }

    // ==================== ArtistApplication 작가 신청 내역 조회 테스트 ====================
    @Test
    @DisplayName("작가 신청 내역 조회 - 실제 DB 데이터 검증")
    void getArtistApplications_ReturnsRealData() {
        // Given
        com.back.domain.artist.entity.ArtistApplication pendingApplication =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testBuyer)
                        .ownerName("홍길동")
                        .email("test@example.com")
                        .phone("010-1234-5678")
                        .artistName("아티스트1")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001")
                        .build();
        artistApplicationRepository.save(pendingApplication);

        ArtistApplicationSearchRequest request = new ArtistApplicationSearchRequest(
                0, 10, null, null, null, null, null
        );

        // When
        ArtistApplicationResponse.List result =
                dashboardService.getArtistApplications(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNull(),  // 통계 없음
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).artistName()).isEqualTo("아티스트1")
        );
    }

    @Test
    @DisplayName("작가 신청 내역 조회 - CANCELLED 상태 텍스트 매핑")
    void getArtistApplications_MapsCancelledStatus() {
        // Given
        com.back.domain.artist.entity.ArtistApplication application =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testBuyer)
                        .ownerName("홍길동")
                        .email("test@example.com")
                        .phone("010-1234-5678")
                        .artistName("아티스트2")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0002")
                        .build();
        artistApplicationRepository.save(application);

        // 취소 처리
        application.cancel();

        ArtistApplicationSearchRequest request = new ArtistApplicationSearchRequest(
                0, 10, null, null, null, null, null
        );

        // When
        ArtistApplicationResponse.List result =
                dashboardService.getArtistApplications(testBuyer.getId(), request);

        // Then
        ArtistApplicationResponse.Summary summary = result.getContent().get(0);
        assertAll(
                () -> assertThat(summary.status()).isEqualTo("CANCELLED"),
                () -> assertThat(summary.statusText()).isEqualTo("취소"),
                () -> assertThat(summary.permissions().canEdit()).isFalse(),
                () -> assertThat(summary.permissions().canCancel()).isFalse()
        );
    }

    @Test
    @DisplayName("작가 신청 내역 조회 - 상태별 필터링 (PENDING)")
    void getArtistApplications_FiltersByPendingStatus() {
        // Given
        com.back.domain.artist.entity.ArtistApplication pendingApp =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testBuyer)
                        .ownerName("홍길동")
                        .email("test@example.com")
                        .phone("010-1234-5678")
                        .artistName("작가A")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001")
                        .build();
        artistApplicationRepository.save(pendingApp);

        com.back.domain.artist.entity.ArtistApplication cancelledApp =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testBuyer)
                        .ownerName("홍길동")
                        .email("test@example.com")
                        .phone("010-1234-5678")
                        .artistName("작가B")
                        .businessNumber("123-45-67891")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0002")
                        .build();
        artistApplicationRepository.save(cancelledApp);
        cancelledApp.cancel();

        ArtistApplicationSearchRequest request = new ArtistApplicationSearchRequest(
                0, 10, "PENDING", null, null, null, null
        );

        // When
        ArtistApplicationResponse.List result =
                dashboardService.getArtistApplications(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("PENDING"),
                () -> assertThat(result.getContent().get(0).artistName()).isEqualTo("작가A")
        );
    }

    // ==================== Cash 캐시 충전/사용 내역 조회 테스트 ====================

    @Test
    @DisplayName("캐시 잔액 조회 - 실제 DB 연동 확인")
    void getCashBalance_ReturnsRealData() {
        // Given
        com.back.domain.payment.moriCash.entity.MoriCashBalance balance =
                com.back.domain.payment.moriCash.entity.MoriCashBalance.builder()
                        .user(testBuyer)
                        .totalBalance(50000)
                        .availableBalance(50000)
                        .frozenBalance(0)
                        .totalCharged(100000)
                        .totalUsed(50000)
                        .build();
        moriCashBalanceRepository.save(balance);

        // When
        com.back.domain.dashboard.customer.dto.response.CashResponse.Balance result =
                dashboardService.getCashBalance(testBuyer.getId());

        // Then
        assertAll(
                () -> assertThat(result.currentBalance()).isEqualTo(50000),
                () -> assertThat(result.currency()).isEqualTo("KRW"),
                () -> assertThat(result.updatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 충전과 사용 내역 Union 조회 및 정렬")
    void getCashHistory_UnionChargeAndPurchase() {
        // Given: 잔액 생성
        com.back.domain.payment.moriCash.entity.MoriCashBalance balance =
                com.back.domain.payment.moriCash.entity.MoriCashBalance.createInitialBalance(testBuyer);
        moriCashBalanceRepository.save(balance);

        // 충전 2건
        createChargeTransaction(30000, LocalDateTime.now().minusDays(3));
        createChargeTransaction(20000, LocalDateTime.now().minusDays(1));

        // 사용 1건
        createPurchaseTransaction(10000, LocalDateTime.now().minusDays(2));

        com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest(
                        0, 10, null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.CashResponse.HistoryList result =
                dashboardService.getCashHistory(testBuyer.getId(), request);

        // Then: 최신순 정렬 확인
        assertAll(
                () -> assertThat(result.getContent()).hasSize(3),
                // 1번째: 가장 최근 충전
                () -> assertThat(result.getContent().get(0).category()).isEqualTo("모리캐시 충전"),
                () -> assertThat(result.getContent().get(0).chargeAmount()).isEqualTo(20000),
                () -> assertThat(result.getContent().get(0).useAmount()).isEqualTo(0),
                // 2번째: 사용
                () -> assertThat(result.getContent().get(1).category()).isEqualTo("상품 주문"),
                () -> assertThat(result.getContent().get(1).chargeAmount()).isEqualTo(0),
                () -> assertThat(result.getContent().get(1).useAmount()).isEqualTo(10000),
                // 3번째: 오래된 충전
                () -> assertThat(result.getContent().get(2).chargeAmount()).isEqualTo(30000)
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 페이징 처리")
    void getCashHistory_HandlesPagination() {
        // Given
        com.back.domain.payment.moriCash.entity.MoriCashBalance balance =
                com.back.domain.payment.moriCash.entity.MoriCashBalance.createInitialBalance(testBuyer);
        moriCashBalanceRepository.save(balance);

        for (int i = 0; i < 15; i++) {
            createChargeTransaction(10000, LocalDateTime.now().minusDays(i));
        }

        com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest(
                        0, 10, null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.CashResponse.HistoryList result =
                dashboardService.getCashHistory(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(10),
                () -> assertThat(result.getTotalElements()).isEqualTo(15),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.isHasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 결제수단 텍스트 매핑")
    void getCashHistory_MapsPaymentMethod() {
        // Given
        com.back.domain.payment.moriCash.entity.MoriCashBalance balance =
                com.back.domain.payment.moriCash.entity.MoriCashBalance.createInitialBalance(testBuyer);
        moriCashBalanceRepository.save(balance);

        // 명확한 시간 차이를 두어 정렬 순서 보장
        createChargeTransactionWithTimeAndMethod(10000, LocalDateTime.now().minusDays(3), "TOSS");   // 가장 오래된
        createChargeTransactionWithTimeAndMethod(20000, LocalDateTime.now().minusDays(2), "NAVERPAY"); // 중간
        createPurchaseTransaction(5000, LocalDateTime.now().minusDays(1)); // 가장 최근

        com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest(
                        0, 10, null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.CashResponse.HistoryList result =
                dashboardService.getCashHistory(testBuyer.getId(), request);

        // Then: 최신순 정렬이므로 역순으로 검증
        assertAll(
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent().get(0).paymentMethod()).isEqualTo("모리캐시"),  // 가장 최근
                () -> assertThat(result.getContent().get(1).paymentMethod()).isEqualTo("네이버페이"), // 중간
                () -> assertThat(result.getContent().get(2).paymentMethod()).isEqualTo("토스페이")   // 가장 오래된
        );
    }

    // ==================== Following 팔로우한 작가 목록 조회 테스트 ====================

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 실제 DB 연동 확인")
    void getFollowingArtists_ReturnsRealData() {
        // Given: 고유한 작가 생성 (테스트 격리)
        String uniqueSuffix = "_follow_real_" + System.nanoTime();
        User uniqueArtist = User.createLocalUser(
                "unique-artist@example.com" + uniqueSuffix,
                "password",
                "고유작가" + uniqueSuffix,
                "010-8888-8888"
        );
        uniqueArtist.becomeArtist();
        uniqueArtist = userRepository.save(uniqueArtist);

        // Given: 작가 프로필 생성
        com.back.domain.artist.entity.ArtistApplication application =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(uniqueArtist)
                        .ownerName("테스트작가")
                        .email("artist@example.com" + uniqueSuffix)
                        .phone("010-1234-5678")
                        .artistName("작가명입니다")
                        .businessNumber("123-45-67890" + uniqueSuffix)
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001" + uniqueSuffix)
                        .build();
        artistApplicationRepository.save(application);

        com.back.domain.artist.entity.ArtistProfile artistProfile =
                com.back.domain.artist.entity.ArtistProfile.fromApplication(uniqueArtist, application);
        artistProfile.updateProfile(
                "https://cdn.example.com/artist.jpg",
                "작가명입니다",
                "@instagram",
                "작가 소개",
                null, null, null, null, null, null, null
        );

        com.back.domain.artist.repository.ArtistProfileRepository artistProfileRepository =
                applicationContext.getBean(com.back.domain.artist.repository.ArtistProfileRepository.class);
        artistProfile = artistProfileRepository.save(artistProfile);

        // Follow 생성
        com.back.domain.follow.repository.FollowRepository followRepository =
                applicationContext.getBean(com.back.domain.follow.repository.FollowRepository.class);

        com.back.domain.follow.entity.Follow follow =
                com.back.domain.follow.entity.Follow.create(testBuyer, artistProfile);
        followRepository.save(follow);

        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).artistName()).isEqualTo("작가명입니다"),
                () -> assertThat(result.getContent().get(0).profileImageUrl())
                        .isEqualTo("https://cdn.example.com/artist.jpg"),
                () -> assertThat(result.getContent().get(0).artistPageUrl()).contains("/artists/"),
                () -> assertThat(result.getTotalElements()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 팔로우가 없는 경우")
    void getFollowingArtists_ReturnsEmptyWhenNoFollows() {
        // Given
        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isEqualTo(0),
                () -> assertThat(result.getTotalPages()).isEqualTo(0),
                () -> assertThat(result.isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 자신이 팔로우한 작가만 조회")
    void getFollowingArtists_ReturnsOnlyOwnFollows() {
        // Given: 고유한 데이터로 테스트 격리
        String uniqueSuffix = "_follow_own_" + System.nanoTime();

        // 다른 사용자 생성
        User otherUser = User.createLocalUser(
                "other@example.com" + uniqueSuffix,
                "password",
                "다른사용자" + uniqueSuffix,
                "01099999999"
        );
        otherUser = userRepository.save(otherUser);

        // 고유한 작가 생성
        User uniqueArtist = User.createLocalUser(
                "unique-artist2@example.com" + uniqueSuffix,
                "password",
                "고유작가2" + uniqueSuffix,
                "010-7777-7777"
        );
        uniqueArtist.becomeArtist();
        uniqueArtist = userRepository.save(uniqueArtist);

        // 작가 프로필 생성
        com.back.domain.artist.entity.ArtistApplication application =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(uniqueArtist)
                        .ownerName("테스트작가")
                        .email("artist@example.com" + uniqueSuffix)
                        .phone("010-1234-5678")
                        .artistName("공통작가")
                        .businessNumber("123-45-67890" + uniqueSuffix)
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001" + uniqueSuffix)
                        .build();
        artistApplicationRepository.save(application);

        com.back.domain.artist.entity.ArtistProfile artistProfile =
                com.back.domain.artist.entity.ArtistProfile.fromApplication(uniqueArtist, application);

        com.back.domain.artist.repository.ArtistProfileRepository artistProfileRepository =
                applicationContext.getBean(com.back.domain.artist.repository.ArtistProfileRepository.class);
        artistProfile = artistProfileRepository.save(artistProfile);

        // Follow 생성
        com.back.domain.follow.repository.FollowRepository followRepository =
                applicationContext.getBean(com.back.domain.follow.repository.FollowRepository.class);

        // testBuyer가 팔로우
        com.back.domain.follow.entity.Follow follow1 =
                com.back.domain.follow.entity.Follow.create(testBuyer, artistProfile);
        followRepository.save(follow1);

        // otherUser도 같은 작가 팔로우
        com.back.domain.follow.entity.Follow follow2 =
                com.back.domain.follow.entity.Follow.create(otherUser, artistProfile);
        followRepository.save(follow2);

        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When: testBuyer의 팔로우 목록 조회
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then: testBuyer의 팔로우만 조회되어야 함
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(1)
        );

        // When: otherUser의 팔로우 목록 조회
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List otherResult =
                dashboardService.getFollowingArtists(otherUser.getId(), request);

        // Then: otherUser의 팔로우도 1개여야 함
        assertAll(
                () -> assertThat(otherResult.getContent()).hasSize(1),
                () -> assertThat(otherResult.getTotalElements()).isEqualTo(1)
        );
    }

    // ==================== Following 팔로우한 작가 목록 조회 테스트 ====================

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 실제 DB 연동 확인")
    void getFollowingArtists_ReturnsRealData() {
        // Given: 작가 프로필 생성
        com.back.domain.artist.entity.ArtistApplication application =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testArtist)
                        .ownerName("테스트작가")
                        .email("artist@example.com")
                        .phone("010-1234-5678")
                        .artistName("작가명입니다")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001")
                        .build();
        artistApplicationRepository.save(application);

        com.back.domain.artist.entity.ArtistProfile artistProfile =
                com.back.domain.artist.entity.ArtistProfile.fromApplication(testArtist, application);
        artistProfile.updateProfile(
                "https://cdn.example.com/artist.jpg",
                "작가명입니다",
                "@instagram",
                "작가 소개",
                null, null, null, null, null, null, null
        );

        com.back.domain.artist.repository.ArtistProfileRepository artistProfileRepository =
                applicationContext.getBean(com.back.domain.artist.repository.ArtistProfileRepository.class);
        artistProfile = artistProfileRepository.save(artistProfile);

        // Follow 생성
        com.back.domain.follow.repository.FollowRepository followRepository =
                applicationContext.getBean(com.back.domain.follow.repository.FollowRepository.class);

        com.back.domain.follow.entity.Follow follow =
                com.back.domain.follow.entity.Follow.create(testBuyer, artistProfile);
        followRepository.save(follow);

        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).artistName()).isEqualTo("작가명입니다"),
                () -> assertThat(result.getContent().get(0).profileImageUrl())
                        .isEqualTo("https://cdn.example.com/artist.jpg"),
                () -> assertThat(result.getContent().get(0).artistPageUrl()).contains("/artists/"),
                () -> assertThat(result.getTotalElements()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 팔로우가 없는 경우")
    void getFollowingArtists_ReturnsEmptyWhenNoFollows() {
        // Given
        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getTotalElements()).isEqualTo(0),
                () -> assertThat(result.getTotalPages()).isEqualTo(0),
                () -> assertThat(result.isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("팔로우한 작가 목록 조회 - 자신이 팔로우한 작가만 조회")
    void getFollowingArtists_ReturnsOnlyOwnFollows() {
        // Given: 다른 사용자 생성
        User otherUser = User.createLocalUser(
                "other@example.com",
                "password",
                "다른사용자",
                "01099999999"
        );
        otherUser = userRepository.save(otherUser);

        // 작가 프로필 생성
        com.back.domain.artist.entity.ArtistApplication application =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testArtist)
                        .ownerName("테스트작가")
                        .email("artist@example.com")
                        .phone("010-1234-5678")
                        .artistName("공통작가")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001")
                        .build();
        artistApplicationRepository.save(application);

        com.back.domain.artist.entity.ArtistProfile artistProfile =
                com.back.domain.artist.entity.ArtistProfile.fromApplication(testArtist, application);

        com.back.domain.artist.repository.ArtistProfileRepository artistProfileRepository =
                applicationContext.getBean(com.back.domain.artist.repository.ArtistProfileRepository.class);
        artistProfile = artistProfileRepository.save(artistProfile);

        // Follow 생성
        com.back.domain.follow.repository.FollowRepository followRepository =
                applicationContext.getBean(com.back.domain.follow.repository.FollowRepository.class);

        // testBuyer가 팔로우
        com.back.domain.follow.entity.Follow follow1 =
                com.back.domain.follow.entity.Follow.create(testBuyer, artistProfile);
        followRepository.save(follow1);

        // otherUser도 같은 작가 팔로우
        com.back.domain.follow.entity.Follow follow2 =
                com.back.domain.follow.entity.Follow.create(otherUser, artistProfile);
        followRepository.save(follow2);

        com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.FollowingSearchRequest(0, 8);

        // When: testBuyer의 팔로우 목록 조회
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List result =
                dashboardService.getFollowingArtists(testBuyer.getId(), request);

        // Then: testBuyer의 팔로우만 조회되어야 함
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(1)
        );

        // When: otherUser의 팔로우 목록 조회
        com.back.domain.dashboard.customer.dto.response.FollowingResponse.List otherResult =
                dashboardService.getFollowingArtists(otherUser.getId(), request);

        // Then: otherUser의 팔로우도 1개여야 함
        assertAll(
                () -> assertThat(otherResult.getContent()).hasSize(1),
                () -> assertThat(otherResult.getTotalElements()).isEqualTo(1)
        );
    }

    // ==================== Helper Methods (캐시 테스트용) ====================

    private com.back.domain.payment.cash.entity.CashTransaction createChargeTransaction(
            int amount, LocalDateTime completedAt) {
        com.back.domain.payment.cash.entity.CashTransaction transaction =
                com.back.domain.payment.cash.entity.CashTransaction.builder()
                        .user(testBuyer)
                        .transactionType(com.back.domain.payment.cash.entity.CashTransactionType.CHARGING)
                        .amount(amount)
                        .paymentMethod("TOSS")
                        .pgProvider("TOSS")
                        .balanceAfter(50000 + amount)
                        .build();

        transaction.completeTransaction("PG-" + System.currentTimeMillis(), "APPR-123",
                50000 + amount);

        // completedAt을 테스트용으로 강제 설정 (Reflection 사용)
        try {
            var field = transaction.getClass().getDeclaredField("completedAt");
            field.setAccessible(true);
            field.set(transaction, completedAt);
        } catch (Exception e) {
            // completedAt 설정 실패 시 현재 시간 사용
        }

        return cashTransactionRepository.save(transaction);
    }

    private com.back.domain.payment.cash.entity.CashTransaction createChargeTransactionWithMethod(
            int amount, String method) {
        com.back.domain.payment.cash.entity.CashTransaction transaction =
                com.back.domain.payment.cash.entity.CashTransaction.builder()
                        .user(testBuyer)
                        .transactionType(com.back.domain.payment.cash.entity.CashTransactionType.CHARGING)
                        .amount(amount)
                        .paymentMethod(method)
                        .pgProvider("TOSS")
                        .balanceAfter(50000 + amount)
                        .build();

        transaction.completeTransaction("PG-" + System.currentTimeMillis(), "APPR-123",
                50000 + amount);

        return cashTransactionRepository.save(transaction);
    }

    private com.back.domain.payment.cash.entity.CashTransaction createChargeTransactionWithTimeAndMethod(
            int amount, LocalDateTime completedAt, String method) {
        com.back.domain.payment.cash.entity.CashTransaction transaction =
                com.back.domain.payment.cash.entity.CashTransaction.builder()
                        .user(testBuyer)
                        .transactionType(com.back.domain.payment.cash.entity.CashTransactionType.CHARGING)
                        .amount(amount)
                        .paymentMethod(method)
                        .pgProvider("TOSS")
                        .balanceAfter(50000 + amount)
                        .build();

        transaction.completeTransaction("PG-" + System.currentTimeMillis(), "APPR-123",
                50000 + amount);

        // completedAt을 테스트용으로 강제 설정 (Reflection 사용)
        try {
            var field = transaction.getClass().getDeclaredField("completedAt");
            field.setAccessible(true);
            field.set(transaction, completedAt);
        } catch (Exception e) {
            // completedAt 설정 실패 시 현재 시간 사용
        }

        return cashTransactionRepository.save(transaction);
    }

    private com.back.domain.payment.moriCash.entity.MoriCashPayment createPurchaseTransaction(
            int amount, LocalDateTime paidAt) {
        com.back.domain.payment.moriCash.entity.MoriCashPayment payment =
                com.back.domain.payment.moriCash.entity.MoriCashPayment.builder()
                        .user(testBuyer)
                        .order(testOrder)
                        .totalPrice(amount)
                        .usedMoriCash(amount)
                        .status(com.back.domain.payment.moriCash.entity.MoriCashPaymentStatus.COMPLETED)
                        .transactionType(com.back.domain.payment.moriCash.entity.TransactionType.PURCHASE)
                        .balanceAfter(50000 - amount)
                        .build();

        payment.completePayment("CASH-" + System.currentTimeMillis());

        // paidAt을 테스트용으로 강제 설정 (Reflection 사용)
        try {
            var field = payment.getClass().getDeclaredField("paidAt");
            field.setAccessible(true);
            field.set(payment, paidAt);
        } catch (Exception e) {
            // paidAt 설정 실패 시 현재 시간 사용
        }

        return moriCashPaymentRepository.save(payment);
    }

    // ==================== Wishlist 찜한 상품 목록 조회 테스트 ====================

    @Test
    @DisplayName("찜한 상품 목록 조회 - 실제 DB 연동 확인")
    void getWishlist_ReturnsRealData() {
        // Given: 찜하기 생성
        com.back.domain.wishlist.repository.WishlistRepository wishlistRepository =
                applicationContext.getBean(com.back.domain.wishlist.repository.WishlistRepository.class);

        com.back.domain.wishlist.entity.Wishlist wishlist =
                com.back.domain.wishlist.entity.Wishlist.builder()
                        .user(testBuyer)
                        .product(testProduct)
                        .build();
        wishlistRepository.save(wishlist);

        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 8, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).brandName()).isEqualTo("테스트 브랜드"),
                () -> assertThat(result.getContent().get(0).productName()).isEqualTo("테스트 상품"),
                () -> assertThat(result.getContent().get(0).price()).isEqualTo(10000),
                () -> assertThat(result.getContent().get(0).artist().name()).isEqualTo("테스트작가"),
                () -> assertThat(result.getSummary().totalWishItems()).isEqualTo(1),
                () -> assertThat(result.getSize()).isEqualTo(8)  // 페이지 크기 8개 고정
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - 페이지 크기 8개 고정 확인")
    void getWishlist_FixesPageSizeToEight() {
        // Given: 찜 10개 생성
        com.back.domain.wishlist.repository.WishlistRepository wishlistRepository =
                applicationContext.getBean(com.back.domain.wishlist.repository.WishlistRepository.class);

        for (int i = 0; i < 10; i++) {
            com.back.domain.product.product.entity.Product product =
                    createTestProduct("상품" + i, "브랜드" + i);

            com.back.domain.wishlist.entity.Wishlist wishlist =
                    com.back.domain.wishlist.entity.Wishlist.builder()
                            .user(testBuyer)
                            .product(product)
                            .build();
            wishlistRepository.save(wishlist);
        }

        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 999, null, null, null, null, null  // size를 999로 요청해도
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then: 8개만 조회되어야 함
        assertAll(
                () -> assertThat(result.getContent()).hasSize(8),
                () -> assertThat(result.getSize()).isEqualTo(8),  // 페이지 크기 8개 고정
                () -> assertThat(result.getTotalElements()).isEqualTo(10),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.isHasNext()).isTrue()
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - 삭제된 상품 제외")
    void getWishlist_ExcludesDeletedProducts() {
        // Given: 일반 상품과 삭제된 상품 찜하기
        com.back.domain.wishlist.repository.WishlistRepository wishlistRepository =
                applicationContext.getBean(com.back.domain.wishlist.repository.WishlistRepository.class);

        // 일반 상품 찜
        com.back.domain.wishlist.entity.Wishlist wishlist1 =
                com.back.domain.wishlist.entity.Wishlist.builder()
                        .user(testBuyer)
                        .product(testProduct)
                        .build();
        wishlistRepository.save(wishlist1);

        // 삭제된 상품 생성 및 찜
        com.back.domain.product.product.entity.Product deletedProduct =
                createTestProduct("삭제된상품", "삭제된브랜드");
        deletedProduct.setDeleted(true);
        productRepository.save(deletedProduct);

        com.back.domain.wishlist.entity.Wishlist wishlist2 =
                com.back.domain.wishlist.entity.Wishlist.builder()
                        .user(testBuyer)
                        .product(deletedProduct)
                        .build();
        wishlistRepository.save(wishlist2);

        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 8, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then: 삭제된 상품은 제외되어야 함
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).productName()).isEqualTo("테스트 상품"),
                () -> assertThat(result.getSummary().totalWishItems()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - 최신 찜 순 정렬")
    void getWishlist_SortsByLatestFirst() {
        // Given: 3개의 찜 생성
        com.back.domain.wishlist.repository.WishlistRepository wishlistRepository =
                applicationContext.getBean(com.back.domain.wishlist.repository.WishlistRepository.class);

        com.back.domain.product.product.entity.Product product1 =
                createTestProduct("첫번째상품", "브랜드1");
        com.back.domain.product.product.entity.Product product2 =
                createTestProduct("두번째상품", "브랜드2");
        com.back.domain.product.product.entity.Product product3 =
                createTestProduct("세번째상품", "브랜드3");

        wishlistRepository.save(com.back.domain.wishlist.entity.Wishlist.builder()
                .user(testBuyer).product(product1).build());

        try {
            Thread.sleep(10);  // 시간 차이를 두기 위해
        } catch (InterruptedException e) {
            // ignore
        }

        wishlistRepository.save(com.back.domain.wishlist.entity.Wishlist.builder()
                .user(testBuyer).product(product2).build());

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        wishlistRepository.save(com.back.domain.wishlist.entity.Wishlist.builder()
                .user(testBuyer).product(product3).build());

        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 8, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then: 최신 순으로 정렬되어야 함
        assertAll(
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent().get(0).productName()).isEqualTo("세번째상품"),
                () -> assertThat(result.getContent().get(1).productName()).isEqualTo("두번째상품"),
                () -> assertThat(result.getContent().get(2).productName()).isEqualTo("첫번째상품")
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - 빈 목록일 때")
    void getWishlist_ReturnsEmptyWhenNoWishlists() {
        // Given: 찜이 없는 상태
        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 8, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isEmpty(),
                () -> assertThat(result.getSummary().totalWishItems()).isEqualTo(0),
                () -> assertThat(result.getTotalElements()).isEqualTo(0),
                () -> assertThat(result.getTotalPages()).isEqualTo(0),
                () -> assertThat(result.isHasNext()).isFalse()
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - brandName과 productName 모두 반환")
    void getWishlist_ReturnsBothBrandNameAndProductName() {
        // Given
        com.back.domain.wishlist.repository.WishlistRepository wishlistRepository =
                applicationContext.getBean(com.back.domain.wishlist.repository.WishlistRepository.class);

        com.back.domain.wishlist.entity.Wishlist wishlist =
                com.back.domain.wishlist.entity.Wishlist.builder()
                        .user(testBuyer)
                        .product(testProduct)
                        .build();
        wishlistRepository.save(wishlist);

        com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.WishlistSearchRequest(
                        0, 8, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.List result =
                dashboardService.getWishlist(testBuyer.getId(), request);

        // Then: brandName과 productName 모두 있어야 함
        com.back.domain.dashboard.customer.dto.response.WishlistResponse.Item item =
                result.getContent().get(0);
        assertAll(
                () -> assertThat(item.brandName()).isNotNull(),
                () -> assertThat(item.brandName()).isEqualTo("테스트 브랜드"),
                () -> assertThat(item.productName()).isNotNull(),
                () -> assertThat(item.productName()).isEqualTo("테스트 상품")
        );
    }

    // Helper: 테스트 상품 생성
    private com.back.domain.product.product.entity.Product createTestProduct(
            String productName, String brandName) {
        com.back.domain.product.category.repository.CategoryRepository categoryRepository =
                applicationContext.getBean(com.back.domain.product.category.repository.CategoryRepository.class);

        com.back.domain.product.category.entity.Category category =
                categoryRepository.findById(1L)
                        .orElseGet(() -> categoryRepository.save(
                                com.back.domain.product.category.entity.Category.builder()
                                        .categoryName("테스트카테고리")
                                        .build()
                        ));

        com.back.domain.product.product.entity.Product product =
                com.back.domain.product.product.entity.Product.builder()
                        .category(category)
                        .user(testArtist)
                        .name(productName)
                        .brandName(brandName)
                        .price(10000)
                        .discountRate(0)
                        .stock(100)
                        .bundleShippingAvailable(false)
                        .deliveryCharge(3000)
                        .additionalShippingCharge(3000)
                        .deliveryType(com.back.domain.product.product.entity.DeliveryType.PAID)
                        .description("테스트 상품 설명")
                        .sellingStatus(com.back.domain.product.product.entity.SellingStatus.SELLING)
                        .displayStatus(com.back.domain.product.product.entity.DisplayStatus.DISPLAYING)
                        .minQuantity(1)
                        .maxQuantity(10)
                        .productModelName("TEST-" + System.currentTimeMillis())
                        .certification(false)
                        .origin("한국")
                        .material("플라스틱")
                        .size("10x10cm")
                        .isPlanned(false)
                        .isRestock(false)
                        .isDeleted(false)
                        .build();

        return productRepository.save(product);
    }

}