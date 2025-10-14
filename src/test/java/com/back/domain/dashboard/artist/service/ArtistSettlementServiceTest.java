package com.back.domain.dashboard.artist.service;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.dashboard.artist.dto.request.ArtistSettlementSearchRequest;
import com.back.domain.dashboard.artist.dto.response.ArtistSettlementResponse;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 작가 정산 내역 조회 테스트
 * Order 기반 정산 통계로 변경 후 테스트
 */
@SpringBootTest
@Transactional
@DisplayName("작가 정산 내역 조회 테스트 (Order 기반)")
class ArtistSettlementServiceTest {

    @Autowired
    private ArtistDashboardService artistDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ArtistProfileRepository artistProfileRepository;

    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;

    private User artist;
    private User customer;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        // 작가 생성
        artist = User.createLocalUser("artist@test.com", "password", "작가", "010-1234-5678");
        artist.becomeArtist();
        artist = userRepository.save(artist);

        // 작가 신청 및 프로필 생성
        ArtistApplication application = ArtistApplication.builder()
                .user(artist)
                .ownerName("작가")
                .email("artist@test.com")
                .phone("010-1234-5678")
                .artistName("작가")
                .businessNumber("123-45-67890")
                .businessAddress("서울시")
                .businessAddressDetail("강남구")
                .businessZipCode("12345")
                .telecomSalesNumber("2024-서울-0001")
                .bankName("테스트은행")
                .bankAccount("123-456-789012")
                .accountName("작가")
                .build();
        application = artistApplicationRepository.save(application);

        ArtistProfile profile = ArtistProfile.builder()
                .user(artist)
                .artistApplication(application)
                .artistName("작가")
                .bankName("테스트은행")
                .bankAccount("123-456-789012")
                .build();
        artistProfileRepository.save(profile);

        // 고객 생성
        customer = User.createLocalUser("customer@test.com", "password", "고객", "010-2222-2222");
        customer = userRepository.save(customer);

        // 카테고리 생성
        category = Category.builder()
                .categoryName("테스트카테고리")
                .build();
        category = categoryRepository.save(category);

        // 상품 생성
        product = Product.builder()
                .category(category)
                .user(artist)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .price(10000)
                .discountRate(0)
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
        product = productRepository.save(product);

        // 배송 완료된 주문 데이터 생성
        createDeliveredOrders();
    }

    private void createDeliveredOrders() {
        // 5개의 배송 완료된 주문 생성
        int[] amounts = {30000, 40000, 50000, 60000, 70000};
        
        for (int amount : amounts) {
            Order order = Order.builder()
                    .user(customer)
                    .orderNumber("ORD" + System.nanoTime())
                    .status(OrderStatus.DELIVERED)  // 배송 완료 상태
                    .totalQuantity(amount / 10000)
                    .totalAmount(BigDecimal.valueOf(amount))
                    .shippingFee(BigDecimal.valueOf(3000))
                    .finalAmount(BigDecimal.valueOf(amount + 3000))
                    .shippingAddress1("서울시")
                    .shippingAddress2("강남구")
                    .recipientName("수령인")
                    .recipientPhone("010-1234-5678")
                    .paymentMethod(PaymentMethod.CARD)
                    .orderDate(LocalDateTime.now())
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(amount / 10000)
                    .price(BigDecimal.valueOf(10000))
                    .build();

            order.addOrderItem(orderItem);
            orderRepository.save(order);
        }
    }

    @Test
    @DisplayName("정산 현황 조회 - 기본 조회 (Order 기반)")
    void getSettlements_Basic() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.scope().year()).isEqualTo(LocalDate.now().getYear());
        assertThat(response.scope().month()).isNull();

        // 요약 정보 확인 (배송 완료된 주문 기준 - 10% 수수료)
        int expectedTotalSales = 250000; // 30k + 40k + 50k + 60k + 70k
        int expectedCommission = 25000;  // 10%
        int expectedNetIncome = 225000;  // 90%

        assertThat(response.summary().totalSales().amount()).isEqualTo(expectedTotalSales);
        assertThat(response.summary().totalCommission().amount()).isEqualTo(expectedCommission);
        assertThat(response.summary().totalNetIncome().amount()).isEqualTo(expectedNetIncome);

        // 테이블 데이터 확인
        assertThat(response.table().getContent()).hasSize(5);
        assertThat(response.table().getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("정산 현황 조회 - 특정 월")
    void getSettlements_Month() {
        // given
        int currentMonth = LocalDate.now().getMonthValue();
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), currentMonth, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.scope().year()).isEqualTo(LocalDate.now().getYear());
        assertThat(response.scope().month()).isEqualTo(currentMonth);

        // 테이블 데이터 확인 (이번 달 데이터)
        assertThat(response.table().getContent()).hasSize(5);
        assertThat(response.table().getContent())
                .allMatch(s -> s.statusText().equals("정산완료"));
    }

    @Test
    @DisplayName("정산 현황 조회 - 정렬 (매출액 오름차순)")
    void getSettlements_SortByAmount() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "grossAmount", "ASC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 첫 번째가 가장 작은 금액
        assertThat(content.get(0).grossAmount()).isEqualTo(30000);
        
        // 금액이 오름차순인지 확인
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).grossAmount()).isLessThanOrEqualTo(content.get(i + 1).grossAmount());
        }
    }

    @Test
    @DisplayName("정산 현황 조회 - 정렬 (매출액 내림차순)")
    void getSettlements_SortByAmountDesc() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "grossAmount", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 첫 번째가 가장 큰 금액
        assertThat(content.get(0).grossAmount()).isEqualTo(70000);
        
        // 금액이 내림차순인지 확인
        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).grossAmount()).isGreaterThanOrEqualTo(content.get(i + 1).grossAmount());
        }
    }

    @Test
    @DisplayName("정산 현황 조회 - 페이징")
    void getSettlements_Paging() {
        // given - 첫 페이지
        ArtistSettlementSearchRequest request1 = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 2, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response1 = artistDashboardService.getSettlements(artist.getId(), request1);

        // then
        assertThat(response1.table().getContent()).hasSize(2);
        assertThat(response1.table().isHasNext()).isTrue();
        assertThat(response1.table().isHasPrevious()).isFalse();

        // given - 두 번째 페이지
        ArtistSettlementSearchRequest request2 = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 1, 2, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response2 = artistDashboardService.getSettlements(artist.getId(), request2);

        // then
        assertThat(response2.table().getContent()).hasSize(2);
        assertThat(response2.table().isHasNext()).isTrue();
        assertThat(response2.table().isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("정산 현황 조회 - 모든 상태가 정산완료")
    void getSettlements_AllCompleted() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();
        assertThat(content).allMatch(s -> s.status().equals("COMPLETED"));
        assertThat(content).allMatch(s -> s.statusText().equals("정산완료"));
    }

    @Test
    @DisplayName("정산 현황 조회 - 수수료 계산 확인 (Order 기반 - 10% 수수료)")
    void getSettlements_CommissionCalculation() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();

        // 각 정산의 수수료와 순수익 확인 (판매 시 10% 수수료)
        content.forEach(settlement -> {
            int expectedCommission = settlement.grossAmount() / 10;  // 10% 수수료
            int expectedNetAmount = settlement.grossAmount() - expectedCommission;
            
            assertThat(settlement.commission()).isEqualTo(expectedCommission);
            assertThat(settlement.netAmount()).isEqualTo(expectedNetAmount);
        });
    }

    @Test
    @DisplayName("정산 현황 조회 - 차트 데이터 생성 확인")
    void getSettlements_ChartData() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response.chart()).isNotNull();
        assertThat(response.chart().series()).isNotNull();
        assertThat(response.chart().series().sales()).hasSize(12); // 1-12월
        assertThat(response.chart().yDomain().min()).isEqualTo(0);
        assertThat(response.chart().yDomain().max()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("정산 현황 조회 - 데이터 없는 월")
    void getSettlements_EmptyMonth() {
        // given - 데이터가 없는 다음 달
        int nextMonth = (LocalDate.now().getMonthValue() % 12) + 1;
        int year = nextMonth == 1 ? LocalDate.now().getYear() + 1 : LocalDate.now().getYear();
        
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                year, nextMonth, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        assertThat(response.table().getContent()).isEmpty();
        assertThat(response.table().getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("정산 현황 조회 - 응답 구조 검증")
    void getSettlements_ResponseStructure() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        // Scope 검증
        assertThat(response.scope()).isNotNull();
        assertThat(response.scope().year()).isNotNull();
        
        // Summary 검증
        assertThat(response.summary()).isNotNull();
        assertThat(response.summary().totalSales()).isNotNull();
        assertThat(response.summary().totalCommission()).isNotNull();
        assertThat(response.summary().totalNetIncome()).isNotNull();
        
        // Chart 검증
        assertThat(response.chart()).isNotNull();
        assertThat(response.chart().series()).isNotNull();
        assertThat(response.chart().yDomain()).isNotNull();
        
        // Table 검증
        assertThat(response.table()).isNotNull();
        assertThat(response.table().getContent()).isNotNull();

        // 기타 필드 검증
        assertThat(response.timezone()).isEqualTo("Asia/Seoul");
        assertThat(response.serverTime()).isNotNull();
    }

    @Test
    @DisplayName("정산 현황 조회 - 상품명 포함 확인")
    void getSettlements_ProductName() {
        // given
        ArtistSettlementSearchRequest request = new ArtistSettlementSearchRequest(
                LocalDate.now().getYear(), null, null, null, 0, 20, "date", "DESC"
        );

        // when
        ArtistSettlementResponse response = artistDashboardService.getSettlements(artist.getId(), request);

        // then
        List<ArtistSettlementResponse.Settlement> content = response.table().getContent();
        assertThat(content).isNotEmpty();
        assertThat(content).allMatch(s -> s.product() != null);
        assertThat(content).allMatch(s -> s.product().name().equals("테스트 상품"));
    }
}
