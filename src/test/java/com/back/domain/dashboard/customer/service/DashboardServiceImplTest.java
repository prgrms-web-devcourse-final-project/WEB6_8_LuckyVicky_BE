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

        // 진행중인 펀딩 생성 (옵션 포함)
        activeFunding = Funding.builder()
                .user(testArtist)
                .title("진행중인 펀딩")
                .description("테스트 펀딩입니다")
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
    @DisplayName("펀딩 참여 목록 조회 - 실제 DB 데이터 검증")
    void getFundingParticipations_ReturnsRealData() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNull(),  // summary는 null
                () -> assertThat(result.getContent()).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(2)
        );
    }

    // 통계 계산 테스트 제거 (프론트 요구사항에 통계 없음)
    // TODO: 프론트에서 통계 기능 요청 시 다시 추가

    @Test
    @DisplayName("펀딩 참여 목록 조회 - participationNumber 포맷 검증")
    void getFundingParticipations_FormatsParticipationNumber() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).participationNumber())
                        .matches("^\\d{5}$"),
                () -> assertThat(result.getContent().get(0).participationNumber().length())
                        .isEqualTo(5)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 매핑 검증")
    void getFundingParticipations_MapsStatus() {
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

        FundingResponse.Participation endedParticipation = result.getContent().stream()
                .filter(p -> p.title().equals("종료된 펀딩"))
                .findFirst()
                .orElseThrow();

        // Then
        assertAll(
                () -> assertThat(activeParticipation.status()).isEqualTo("ACTIVE"),
                () -> assertThat(activeParticipation.statusText()).isEqualTo("진행중"),
                () -> assertThat(endedParticipation.status()).isEqualTo("ENDED"),
                () -> assertThat(endedParticipation.statusText()).isIn("종료", "성공", "실패", "취소")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - Meta 제외 검증")
    void getFundingParticipations_ExcludesMeta() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).meta()).isNull(),
                () -> assertThat(result.getContent().get(1).meta()).isNull()
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 필드 타입 검증")
    void getFundingParticipations_ValidatesFieldTypes() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        FundingResponse.Participation participation = result.getContent().get(0);

        // Then
        assertAll(
                () -> assertThat(participation.participationNumber()).isInstanceOf(String.class),
                () -> assertThat(participation.participationId()).isInstanceOf(Long.class),
                () -> assertThat(participation.pledgedAmount()).isInstanceOf(Long.class),
                () -> assertThat(participation.artist().id()).isInstanceOf(Long.class),
                () -> assertThat(participation.quantity()).isInstanceOf(Integer.class)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (ACTIVE)")
    void getFundingParticipations_FiltersActiveStatus() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, "ACTIVE", null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("ACTIVE"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("진행중인 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 상태 필터링 (ENDED)")
    void getFundingParticipations_FiltersEndedStatus() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, "ENDED", null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("ENDED"),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("종료된 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 키워드 검색 (제목)")
    void getFundingParticipations_SearchesByTitle() {
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
    @DisplayName("펀딩 참여 목록 조회 - 키워드 검색 (작가명)")
    void getFundingParticipations_SearchesByArtistName() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, "테스트작가", "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).artist().name()).isEqualTo("테스트작가"),
                () -> assertThat(result.getContent().get(1).artist().name()).isEqualTo("테스트작가")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 정렬 (paidAt ASC)")
    void getFundingParticipations_SortsByPaidAtAsc() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "ASC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("종료된 펀딩"),
                () -> assertThat(result.getContent().get(1).title()).isEqualTo("진행중인 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 정렬 (paidAt DESC)")
    void getFundingParticipations_SortsByPaidAtDesc() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("진행중인 펀딩"),
                () -> assertThat(result.getContent().get(1).title()).isEqualTo("종료된 펀딩")
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 페이징 검증")
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
                () -> assertThat(result.getPage()).isEqualTo(0),
                () -> assertThat(result.getSize()).isEqualTo(1),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result.isHasNext()).isTrue(),
                () -> assertThat(result.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - paidDate 필드 포맷 검증")
    void getFundingParticipations_FormatsPaidDate() {
        // Given
        FundingSearchRequest request = new FundingSearchRequest(
                0, 10, null, null, "paidAt", "DESC"
        );

        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                testBuyer.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).paidDate())
                        .matches("^\\d{4}\\. \\d{2}\\. \\d{2}$")
        );
    }

    // TODO: 배송 CRUD 완성 후 추가할 테스트들
    /**
     * 배송 CRUD 완성 후 수정해야 할 테스트:
     *
     * 1. getFundingParticipations_CalculatesStatistics()
     *    - fulfilling, fulfilled 카운트 검증 주석 해제
     *
     * 2. 새로운 테스트 추가:
     *    - getFundingParticipations_IncludesDeliveryStatus()
     *      : 배송 상태 포함 여부 검증
     *    - getFundingParticipations_FiltersDeliveryStatus()
     *      : 배송 상태별 필터링 검증 (FULFILLING/FULFILLED)
     *
     * 3. Response DTO 수정:
     *    - FundingResponse.SummaryDto에서 배송 필드 주석 해제
     *    - (선택) Participation에 DeliveryInfo 필드 추가 시 검증 로직 추가
     */

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

    // ==================== 교환/환불 신청 모달 실제 DB 연동 테스트 ====================

    @Test
    @DisplayName("교환/환불 신청 모달 데이터 조회 - 모든 필수 필드 검증")
    void getReturnFormData_ReturnsAllRequiredFields() {
        // Given
        Long orderId = testOrder.getId();

        // When
        com.back.domain.dashboard.customer.dto.response.ReturnResponse.FormData result =
                dashboardService.getReturnFormData(testBuyer.getId(), orderId);

        // Then
        com.back.domain.dashboard.customer.dto.response.ReturnResponse.Summary summary = result.summary();
        
        assertAll(
                // 기본 구조 검증
                () -> assertThat(result.summary()).isNotNull(),
                () -> assertThat(result.form()).isNull(),
                () -> assertThat(result.permissions()).isNull(),
                
                // 주문번호 (7자리 포맷)
                () -> assertThat(summary.orderNo()).matches("^\\d{7}$"),
                
                // 브랜드명 (DB에서 조회)
                () -> assertThat(summary.brandName()).isEqualTo("테스트 브랜드"),
                
                // 상품명
                () -> assertThat(summary.title()).isEqualTo("테스트 상품"),
                
                // 총 가격 (finalAmount)
                () -> assertThat(summary.price()).isEqualTo(testOrder.getFinalAmount().intValue()),
                
                // 상품 갯수 (orderItems 개수)
                () -> assertThat(summary.quantity()).isEqualTo(testOrder.getOrderItems().size())
        );
    }

    @Test
    @DisplayName("교환/환불 신청 모달 - 권한 검증")
    void getReturnFormData_ValidatesAuthorization() {
        // Given
        User anotherUser = User.createLocalUser(
                "another@example.com",
                "password",
                "다른사용자",
                "01099999999"
        );
        User savedAnotherUser = userRepository.save(anotherUser);

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.back.global.exception.ServiceException.class,
                () -> dashboardService.getReturnFormData(savedAnotherUser.getId(), testOrder.getId())
        );
    }
}

