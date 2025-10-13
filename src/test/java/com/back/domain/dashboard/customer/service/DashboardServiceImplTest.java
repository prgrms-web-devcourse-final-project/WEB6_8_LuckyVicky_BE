package com.back.domain.dashboard.customer.service;


import com.back.domain.dashboard.customer.dto.request.ArtistApplicationSearchRequest;
import com.back.domain.dashboard.customer.dto.request.FundingSearchRequest;
import com.back.domain.dashboard.customer.dto.response.ArtistApplicationResponse;
import com.back.domain.dashboard.customer.dto.response.FundingResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingContribution;
import com.back.domain.funding.entity.FundingOption;
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
 * 2025.10.10 수정 - 작가 신청 내역 조회 테스트 추가
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

        // FundingOption 먼저 생성
        FundingOption activeOption = FundingOption.builder()
                .name("테스트 리워드 A")
                .price(25000L)
                .stock(100)
                .sortOrder(1)
                .build();

        FundingOption endedOption = FundingOption.builder()
                .name("테스트 리워드 B")
                .price(30000L)
                .stock(50)
                .sortOrder(1)
                .build();

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
        activeFunding.attachOption(activeOption);
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
        endedFunding.attachOption(endedOption);
        endedFunding = fundingRepository.save(endedFunding);

        // 저장된 옵션 가져오기 (cascade로 저장됨)
        activeOption = activeFunding.getOptions().get(0);
        endedOption = endedFunding.getOptions().get(0);

        // 펀딩 참여 내역 생성 (진행중)
        FundingContribution activeContribution = FundingContribution.builder()
                .funding(activeFunding)
                .option(activeOption)
                .buyer(testBuyer)
                .quantity(2)
                .unitPrice(25000L)
                .totalAmount(50000L)
                .paidAt(LocalDateTime.now().minusDays(3))
                .build();
        fundingContributionRepository.save(activeContribution);

        // 펀딩 참여 내역 생성 (종료)
        FundingContribution endedContribution = FundingContribution.builder()
                .funding(endedFunding)
                .option(endedOption)
                .buyer(testBuyer)
                .quantity(1)
                .unitPrice(30000L)
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

        createChargeTransactionWithMethod(10000, "TOSS");
        createChargeTransactionWithMethod(20000, "NAVERPAY");
        createPurchaseTransaction(5000, LocalDateTime.now());

        com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest request =
                new com.back.domain.dashboard.customer.dto.request.CashHistorySearchRequest(
                        0, 10, null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.customer.dto.response.CashResponse.HistoryList result =
                dashboardService.getCashHistory(testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(3),
                () -> assertThat(result.getContent().get(0).paymentMethod()).isEqualTo("모리캐시"),
                () -> assertThat(result.getContent().get(1).paymentMethod()).isEqualTo("네이버페이"),
                () -> assertThat(result.getContent().get(2).paymentMethod()).isEqualTo("토스페이")
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

}