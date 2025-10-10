package com.back.domain.dashboard.artist.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.dashboard.artist.dto.request.*;
import com.back.domain.dashboard.artist.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeItem;
import com.back.domain.order.exchange.entity.ExchangeReasonType;
import com.back.domain.order.exchange.repository.ExchangeRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.order.refund.entity.Refund;
import com.back.domain.order.refund.entity.RefundItem;
import com.back.domain.order.refund.entity.RefundReasonType;
import com.back.domain.order.refund.repository.RefundRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.DeliveryType;
import com.back.domain.product.product.entity.DisplayStatus;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistDashboardServiceImpl 통합 테스트
 * 실제 DB 연동하여 핵심 비즈니스 로직과 데이터 일관성 검증
 * 2025.10.10 수정 - Mock에서 실제 DB 연동으로 변경
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("작가 대시보드 서비스 통합 테스트")
class ArtistDashboardServiceImplTest {

    @Autowired
    private ArtistDashboardService artistDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FundingRepository fundingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private ArtistProfileRepository artistProfileRepository;

    @Autowired
    private com.back.domain.artist.repository.ArtistApplicationRepository artistApplicationRepository;

    private User testArtist;
    private User testCustomer;
    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 테스트 작가 생성
        testArtist = User.createLocalUser(
                "artist@test.com",
                "password",
                "테스트작가",
                "010-1111-1111"
        );
        testArtist.becomeArtist();
        testArtist = userRepository.save(testArtist);

        // 작가 신청 생성 (ArtistProfile에 필수)
        com.back.domain.artist.entity.ArtistApplication artistApplication =
                com.back.domain.artist.entity.ArtistApplication.builder()
                        .user(testArtist)
                        .ownerName("테스트작가")
                        .email("artist@test.com")
                        .phone("010-1111-1111")
                        .artistName("테스트작가")
                        .businessNumber("123-45-67890")
                        .businessAddress("서울시")
                        .businessAddressDetail("강남구")
                        .businessZipCode("12345")
                        .telecomSalesNumber("2024-서울-0001")
                        .bankName("테스트은행")
                        .bankAccount("123-456-789012")
                        .accountName("테스트작가")
                        .build();
        artistApplication = artistApplicationRepository.save(artistApplication);

        // 작가 프로필 생성
        ArtistProfile artistProfile = ArtistProfile.builder()
                .user(testArtist)
                .artistApplication(artistApplication)
                .artistName("테스트작가")
                .bankName("테스트은행")
                .bankAccount("123-456-789012")
                .build();
        artistProfileRepository.save(artistProfile);

        // 테스트 고객 생성
        testCustomer = User.createLocalUser(
                "customer@test.com",
                "password",
                "테스트고객",
                "010-2222-2222"
        );
        testCustomer = userRepository.save(testCustomer);

        // 테스트 카테고리 생성
        testCategory = Category.builder()
                .categoryName("테스트카테고리")
                .build();
        testCategory = categoryRepository.save(testCategory);

        // 테스트 상품 생성
        testProduct = Product.builder()
                .category(testCategory)
                .user(testArtist)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .price(10000)
                .discountRate(10)
                .stock(100)
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(0)
                .deliveryType(DeliveryType.PAID)
                .description("테스트 상품 설명")
                .sellingStatus(SellingStatus.SELLING)
                .displayStatus(DisplayStatus.DISPLAYING)
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
    }

    // ==================== 상품 테스트 ====================

    @Test
    @DisplayName("상품 목록 조회 - 실제 DB 데이터 검증")
    void getProducts_ReturnsRealData() {
        // Given
        ArtistProductSearchRequest request = new ArtistProductSearchRequest(
                0, 10, null, null, "createDate", "DESC");

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).isNotEmpty(),
                () -> assertThat(result.getContent().get(0).productName()).isEqualTo("테스트 상품"),
                () -> assertThat(result.getContent().get(0).price()).isEqualTo(10000),
                () -> assertThat(result.getContent().get(0).discountRate()).isEqualTo(10),
                () -> assertThat(result.getContent().get(0).discountPrice()).isEqualTo(9000),
                () -> assertThat(result.getContent().get(0).statusText()).isEqualTo("판매중")
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 판매 상태 필터링")
    void getProducts_FiltersByStatus() {
        // Given
        Product endProduct = Product.builder()
                .category(testCategory)
                .user(testArtist)
                .name("판매종료 상품")
                .brandName("테스트 브랜드")
                .price(20000)
                .discountRate(0)
                .stock(0)
                .bundleShippingAvailable(false)
                .deliveryCharge(3000)
                .additionalShippingCharge(0)
                .deliveryType(DeliveryType.PAID)
                .description("판매종료")
                .sellingStatus(SellingStatus.END_OF_SALE)
                .displayStatus(DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(10)
                .productModelName("TEST-002")
                .certification(false)
                .origin("한국")
                .material("플라스틱")
                .size("10x10cm")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        productRepository.save(endProduct);

        ArtistProductSearchRequest request = new ArtistProductSearchRequest(
                0, 10, null, true, "createDate", "DESC");

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).sellingStatus()).isEqualTo("SELLING")
        );
    }

    // ==================== 펀딩 테스트 ====================

    @Test
    @DisplayName("펀딩 목록 조회 - 실제 DB 데이터 검증")
    void getFundings_ReturnsRealData() {
        // Given
        FundingOption option = FundingOption.builder()
                .name("테스트 리워드")
                .price(25000L)
                .stock(100)
                .sortOrder(1)
                .build();

        Funding funding = Funding.builder()
                .user(testArtist)
                .title("테스트 펀딩")
                .description("테스트 펀딩입니다")
                .imageUrl("https://example.com/image.jpg")
                .targetAmount(1000000L)
                .collectedAmount(0L)
                .startDate(LocalDateTime.now().minusDays(5))
                .endDate(LocalDateTime.now().plusDays(25))
                .status(FundingStatus.OPEN)
                .participantCount(0)
                .build();
        funding.attachOption(option);
        fundingRepository.save(funding);

        ArtistFundingSearchRequest request = new ArtistFundingSearchRequest(
                0, 10, null, null, null, null, null, null, null, "endDate", "DESC");

        // When
        ArtistFundingResponse.List result = artistDashboardService.getFundings(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).title()).isEqualTo("테스트 펀딩"),
                () -> assertThat(result.getContent().get(0).status()).isEqualTo("OPEN"),
                () -> assertThat(result.getContent().get(0).statusText()).isEqualTo("진행중")
        );
    }

    // ==================== 설정 테스트 ====================

    @Test
    @DisplayName("작가 설정 조회 - 계좌번호 마스킹 검증")
    void getSettings_AccountMasking() {
        // When
        ArtistSettingsResponse result = artistDashboardService.getSettings(testArtist.getId());

        // Then
        assertAll(
                () -> assertThat(result.profile().nickname()).isEqualTo("테스트작가"),
                () -> assertThat(result.payout().accountMasked()).isEqualTo("****-****-**9012"),
                () -> assertThat(result.payout().bankName()).isEqualTo("테스트은행")
        );
    }

    // ==================== 주문 내역 테스트 ====================

    @Test
    @DisplayName("주문 내역 조회 - 실제 DB 데이터 검증")
    void getOrders_ReturnsRealData() {
        // Given
        Order order = Order.builder()
                .user(testCustomer)
                .orderNumber("ORD" + System.currentTimeMillis())
                .status(OrderStatus.PAYMENT_COMPLETED)
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(20000))
                .shippingFee(BigDecimal.valueOf(3000))
                .finalAmount(BigDecimal.valueOf(23000))
                .shippingAddress1("서울시 강남구")
                .shippingAddress2("테헤란로 123")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .paymentMethod(PaymentMethod.CARD)
                .orderDate(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(testProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(10000))
                .build();

        order.addOrderItem(orderItem);
        orderRepository.save(order);

        ArtistOrderSearchRequest request = new ArtistOrderSearchRequest(
                0, 10, null, null, null, null, null, null);

        // When
        ArtistOrderResponse.List result = artistDashboardService.getOrders(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).status()).isEqualTo("PAYMENT_COMPLETED"),
                () -> assertThat(result.content().get(0).statusText()).isEqualTo("결제완료"),
                () -> assertThat(result.content().get(0).buyer().name()).isEqualTo("테스트고객")
        );
    }

    @Test
    @DisplayName("주문 내역 조회 - 상태 필터링")
    void getOrders_FiltersByStatus() {
        // Given
        createTestOrder(OrderStatus.PAYMENT_COMPLETED);
        createTestOrder(OrderStatus.PREPARING_SHIPMENT);

        ArtistOrderSearchRequest request = new ArtistOrderSearchRequest(
                0, 10, "PREPARING_SHIPMENT", null, null, null, null, null);

        // When
        ArtistOrderResponse.List result = artistDashboardService.getOrders(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).status()).isEqualTo("PREPARING_SHIPMENT")
        );
    }

    // ==================== 취소 요청 테스트 ====================

    @Test
    @DisplayName("취소 요청 조회 - 실제 DB 데이터 검증")
    void getCancellationRequests_ReturnsRealData() {
        // Given
        Order order = createTestOrder(OrderStatus.PAYMENT_COMPLETED);
        OrderItem orderItem = order.getOrderItems().get(0);

        // Refund 먼저 생성
        Refund refund = Refund.builder()
                .order(order)
                .user(testCustomer)
                .status(Refund.RefundStatus.REQUESTED)
                .reasonType(RefundReasonType.DEFECTIVE)
                .reason("상품불량")
                .detailReason("상세 사유")
                .refundAmount(BigDecimal.valueOf(10000))
                .refundMethod(Refund.RefundMethod.ORIGINAL_PAYMENT)
                .build();
        refund = refundRepository.save(refund);

        // RefundItem 생성
        RefundItem refundItem = RefundItem.builder()
                .refund(refund)
                .orderItem(orderItem)
                .quantity(1)
                .refundPrice(BigDecimal.valueOf(10000))
                .build();
        
        // Refund의 refundItems 리스트에 추가 (양방향 연관관계)
        refund.getRefundItems().add(refundItem);
        refundRepository.save(refund);

        ArtistCancellationSearchRequest request = new ArtistCancellationSearchRequest(
                0, 10, null, null, null, null, null, null, null);

        // When
        ArtistCancellationResponse.List result = artistDashboardService.getCancellationRequests(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).status()).isIn("PENDING", "REQUESTED"),
                () -> assertThat(result.content().get(0).statusText()).isEqualTo("처리대기")
        );
    }

    // ==================== 교환 요청 테스트 ====================

    @Test
    @DisplayName("교환 요청 조회 - 실제 DB 데이터 검증")
    void getExchangeRequests_ReturnsRealData() {
        // Given
        Order order = createTestOrder(OrderStatus.PAYMENT_COMPLETED);
        OrderItem orderItem = order.getOrderItems().get(0);

        Exchange exchange = Exchange.builder()
                .order(order)
                .user(testCustomer)
                .status(Exchange.ExchangeStatus.REQUESTED)
                .reasonType(ExchangeReasonType.SIZE_COLOR)
                .reason("사이즈 변경")
                .detailReason("L사이즈로 교환 희망")
                .exchangeMethod(Exchange.ExchangeMethod.PICKUP)
                .build();
        exchange = exchangeRepository.save(exchange);

        ExchangeItem exchangeItem = ExchangeItem.builder()
                .exchange(exchange)
                .orderItem(orderItem)
                .quantity(1)
                .build();
        // ExchangeItem은 cascade로 저장됨
        exchange.getExchangeItems().add(exchangeItem);

        ArtistExchangeSearchRequest request = new ArtistExchangeSearchRequest(
                0, 10, null, null, null, null, null, null, null);

        // When
        ArtistExchangeResponse.List result = artistDashboardService.getExchangeRequests(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).status()).isEqualTo("PENDING"),
                () -> assertThat(result.content().get(0).statusText()).isEqualTo("처리대기"),
                () -> assertThat(result.content().get(0).permissions().canApprove()).isTrue()
        );
    }

    @Test
    @DisplayName("교환 요청 조회 - 상태 필터링")
    void getExchangeRequests_FiltersByStatus() {
        // Given
        Order order = createTestOrder(OrderStatus.PAYMENT_COMPLETED);
        OrderItem orderItem = order.getOrderItems().get(0);

        Exchange exchange = Exchange.builder()
                .order(order)
                .user(testCustomer)
                .status(Exchange.ExchangeStatus.COMPLETED)
                .reasonType(ExchangeReasonType.SIZE_COLOR)
                .reason("사이즈 변경")
                .detailReason("상세")
                .exchangeMethod(Exchange.ExchangeMethod.DIRECT)
                .build();
        exchange = exchangeRepository.save(exchange);

        ExchangeItem exchangeItem = ExchangeItem.builder()
                .exchange(exchange)
                .orderItem(orderItem)
                .quantity(1)
                .build();
        // ExchangeItem은 cascade로 저장됨
        exchange.getExchangeItems().add(exchangeItem);

        ArtistExchangeSearchRequest request = new ArtistExchangeSearchRequest(
                0, 10, "APPROVED", null, null, null, null, null, null);

        // When
        ArtistExchangeResponse.List result = artistDashboardService.getExchangeRequests(
                testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().get(0).status()).isEqualTo("APPROVED"),
                () -> assertThat(result.content().get(0).statusText()).isEqualTo("승인됨"),
                () -> assertThat(result.content().get(0).permissions().canApprove()).isFalse()
        );
    }

    // ==================== 메인 대시보드 통계 테스트 ====================

    @Test
    @DisplayName("메인 대시보드 통계 조회 - 실제 DB 데이터 검증")
    void getMainStats_ReturnsRealData() {
        // Given
        createTestOrder(OrderStatus.PAYMENT_COMPLETED);

        ArtistMainStatsRequest request = new ArtistMainStatsRequest(
                "30D", null, null, null, "Asia/Seoul");

        // When
        ArtistMainResponse result = artistDashboardService.getMainStats(testArtist.getId(), request);

        // Then
        assertAll(
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.profile().userId()).isEqualTo(testArtist.getId()),
                () -> assertThat(result.profile().nickname()).isEqualTo("테스트작가"),
                () -> assertThat(result.stats()).isNotNull(),
                () -> assertThat(result.stats().productCount()).isGreaterThanOrEqualTo(0),
                () -> assertThat(result.trends()).isNotNull()
        );
    }

    // ==================== 헬퍼 메서드 ====================

    private Order createTestOrder(OrderStatus status) {
        Order order = Order.builder()
                .user(testCustomer)
                .orderNumber("ORD" + System.nanoTime())
                .status(status)
                .totalQuantity(2)
                .totalAmount(BigDecimal.valueOf(20000))
                .shippingFee(BigDecimal.valueOf(3000))
                .finalAmount(BigDecimal.valueOf(23000))
                .shippingAddress1("서울시")
                .shippingAddress2("강남구")
                .recipientName("수령인")
                .recipientPhone("010-1234-5678")
                .paymentMethod(PaymentMethod.CARD)
                .orderDate(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(testProduct)
                .quantity(2)
                .price(BigDecimal.valueOf(10000))
                .build();

        order.addOrderItem(orderItem);
        return orderRepository.save(order);
    }
}
