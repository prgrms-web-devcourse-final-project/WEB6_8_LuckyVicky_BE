package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.request.OrderStatusChangeRequestDto;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import com.back.domain.order.order.entity.PaymentMethod;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.order.orderItem.entity.OrderItem;
import com.back.domain.payment.moriCash.entity.MoriCashBalance;
import com.back.domain.payment.moriCash.repository.MoriCashBalanceRepository;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderServiceArtistRevenueTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MoriCashBalanceRepository moriCashBalanceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private User buyer;
    private User artist;
    private User admin;
    private Category category;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // 고유한 이름 생성을 위한 타임스탬프 사용
        String uniqueSuffix = "_" + System.nanoTime();

        // 구매자 생성
        buyer = User.createLocalUser("buyer@test.com" + uniqueSuffix, "password", "구매자" + uniqueSuffix, "010-1111-1111");
        buyer = userRepository.save(buyer);

        // 작가 생성
        artist = User.createLocalUser("artist@test.com" + uniqueSuffix, "password", "작가" + uniqueSuffix, "010-2222-2222");
        artist.becomeArtist();
        artist = userRepository.save(artist);

        // 관리자 생성 (테스트용)
        admin = User.createLocalUser("admin@test.com" + uniqueSuffix, "password", "관리자" + uniqueSuffix, "010-9999-9999");
        admin.becomeAdmin();  // 관리자 권한 부여

        admin = userRepository.save(admin);

        // 카테고리 생성
        category = Category.builder()
                .categoryName("테스트 카테고리")
                .build();
        category = categoryRepository.save(category);

        // 상품 생성 (필수 필드 모두 포함)
        product = Product.builder()
                .category(category)
                .user(artist)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .price(10000)
                .discountRate(0)
                .bundleShippingAvailable(true)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(com.back.domain.product.product.entity.DeliveryType.PAID)
                .stock(100)
                .description("테스트 상품 설명")
                .sellingStatus(SellingStatus.SELLING)
                .displayStatus(com.back.domain.product.product.entity.DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(100)
                .productModelName("테스트 모델")
                .certification(false)
                .origin("한국")
                .material("테스트 재질")
                .size("프리사이즈")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        product = productRepository.save(product);

        // 주문 생성
        OrderItem orderItem = OrderItem.createOrderItem(product, 2, null); // 2개 구매
        order = Order.createOrder(
                buyer,
                List.of(orderItem),
                "서울시 강남구",
                "테헤란로 123",
                "06234",
                "구매자",
                "010-1111-1111",
                "문 앞에 놔주세요",
                PaymentMethod.CARD
        );
        order = orderRepository.save(order);
    }

    @Test
    @DisplayName("주문 배송 완료 시 작가 모리캐시 증가")
    void creditArtistRevenue_OnDeliveryCompleted() {
        // given
        // 배송 완료 전 작가의 모리캐시 확인 (없거나 0)
        MoriCashBalance balanceBefore = moriCashBalanceRepository.findByUser(artist).orElse(null);
        int balanceBeforeAmount = balanceBefore != null ? balanceBefore.getAvailableBalance() : 0;

        entityManager.flush();
        entityManager.clear();

        // when - 주문 상태를 DELIVERED로 변경
        orderService.changeOrderStatus(
                order.getId(),
                new OrderStatusChangeRequestDto(OrderStatus.DELIVERED),
                admin // 관리자가 상태 변경
        );

        entityManager.flush();
        entityManager.clear();

        // then
        MoriCashBalance balanceAfter = moriCashBalanceRepository.findByUser(artist)
                .orElseThrow(() -> new AssertionError("작가의 모리캐시 잔액이 생성되지 않았습니다."));

        // 상품 2개 * 10,000원 = 20,000원
        // 수수료 10% = 2,000원
        // 순수익 = 18,000원
        int expectedRevenue = 18000;
        assertThat(balanceAfter.getAvailableBalance()).isEqualTo(balanceBeforeAmount + expectedRevenue);
        assertThat(balanceAfter.getTotalBalance()).isEqualTo(balanceBeforeAmount + expectedRevenue);
    }

    @Test
    @DisplayName("여러 작가의 상품을 구매한 경우 각 작가에게 수익 적립")
    void creditArtistRevenue_MultipleArtists() {
        // given
        // 두 번째 작가 생성
        String uniqueSuffix = "_" + System.nanoTime();
        User artist2 = User.createLocalUser("artist2@test.com" + uniqueSuffix, "password", "작가2" + uniqueSuffix, "010-3333-3333");
        artist2.becomeArtist();
        artist2 = userRepository.save(artist2);

        // 두 번째 작가의 상품 생성
        Product product2 = Product.builder()
                .category(category)
                .user(artist2)
                .name("테스트 상품2")
                .brandName("테스트 브랜드2")
                .price(5000)
                .discountRate(0)
                .bundleShippingAvailable(true)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(com.back.domain.product.product.entity.DeliveryType.PAID)
                .stock(100)
                .description("테스트 상품2 설명")
                .sellingStatus(SellingStatus.SELLING)
                .displayStatus(com.back.domain.product.product.entity.DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(100)
                .productModelName("테스트 모델2")
                .certification(false)
                .origin("한국")
                .material("테스트 재질2")
                .size("프리사이즈")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        product2 = productRepository.save(product2);

        // 두 작가의 상품을 모두 포함한 주문 생성
        OrderItem orderItem1 = OrderItem.createOrderItem(product, 1, null); // 10,000원
        OrderItem orderItem2 = OrderItem.createOrderItem(product2, 2, null); // 5,000원 * 2 = 10,000원
        Order multiArtistOrder = Order.createOrder(
                buyer,
                List.of(orderItem1, orderItem2),
                "서울시 강남구",
                "테헤란로 123",
                "06234",
                "구매자",
                "010-1111-1111",
                "문 앞에 놔주세요",
                PaymentMethod.CARD
        );
        multiArtistOrder = orderRepository.save(multiArtistOrder);

        entityManager.flush();
        entityManager.clear();

        // when - 배송 완료
        orderService.changeOrderStatus(
                multiArtistOrder.getId(),
                new OrderStatusChangeRequestDto(OrderStatus.DELIVERED),
                admin // 관리자가 상태 변경
        );

        entityManager.flush();
        entityManager.clear();

        // then
        // 작가1: 10,000원 - 1,000원 = 9,000원
        MoriCashBalance balance1 = moriCashBalanceRepository.findByUser(artist)
                .orElseThrow(() -> new AssertionError("작가1의 모리캐시가 생성되지 않았습니다."));
        assertThat(balance1.getAvailableBalance()).isEqualTo(9000);

        // 작가2: 10,000원 - 1,000원 = 9,000원
        MoriCashBalance balance2 = moriCashBalanceRepository.findByUser(artist2)
                .orElseThrow(() -> new AssertionError("작가2의 모리캐시가 생성되지 않았습니다."));
        assertThat(balance2.getAvailableBalance()).isEqualTo(9000);
    }

    @Test
    @DisplayName("배송 완료가 아닌 다른 상태로 변경 시 수익 적립 안됨")
    void creditArtistRevenue_NotDelivered() {
        // given
        MoriCashBalance balanceBefore = moriCashBalanceRepository.findByUser(artist).orElse(null);
        int balanceBeforeAmount = balanceBefore != null ? balanceBefore.getAvailableBalance() : 0;

        entityManager.flush();
        entityManager.clear();

        // when - SHIPPING으로만 변경 (DELIVERED 아님)
        orderService.changeOrderStatus(
                order.getId(),
                new OrderStatusChangeRequestDto(OrderStatus.SHIPPING),
                admin // 관리자가 상태 변경
        );

        entityManager.flush();
        entityManager.clear();

        // then - 모리캐시 증가 안됨
        MoriCashBalance balanceAfter = moriCashBalanceRepository.findByUser(artist).orElse(null);
        int balanceAfterAmount = balanceAfter != null ? balanceAfter.getAvailableBalance() : 0;
        assertThat(balanceAfterAmount).isEqualTo(balanceBeforeAmount);
    }

    @Test
    @DisplayName("이미 모리캐시 잔액이 있는 작가의 경우 기존 잔액에 추가")
    void creditArtistRevenue_ExistingBalance() {
        // given
        // 작가에게 기존 모리캐시 잔액 생성
        MoriCashBalance existingBalance = MoriCashBalance.createInitialBalance(artist);
        existingBalance.addBalance(50000); // 기존 50,000원
        moriCashBalanceRepository.save(existingBalance);

        entityManager.flush();
        entityManager.clear();

        // when - 배송 완료
        orderService.changeOrderStatus(
                order.getId(),
                new OrderStatusChangeRequestDto(OrderStatus.DELIVERED),
                admin // 관리자가 상태 변경
        );

        entityManager.flush();
        entityManager.clear();

        // then
        MoriCashBalance balanceAfter = moriCashBalanceRepository.findByUser(artist)
                .orElseThrow(() -> new AssertionError("작가의 모리캐시를 찾을 수 없습니다."));

        // 기존 50,000원 + 신규 18,000원 = 68,000원
        assertThat(balanceAfter.getAvailableBalance()).isEqualTo(68000);
    }

    @Test
    @DisplayName("수수료 계산이 정확한지 검증")
    void creditArtistRevenue_CommissionCalculation() {
        // given
        // 다양한 금액의 상품 생성
        Product expensiveProduct = Product.builder()
                .category(category)
                .user(artist)
                .name("고가 상품")
                .brandName("고가 브랜드")
                .price(99999)
                .discountRate(0)
                .bundleShippingAvailable(true)
                .deliveryCharge(3000)
                .additionalShippingCharge(3000)
                .deliveryType(com.back.domain.product.product.entity.DeliveryType.PAID)
                .stock(100)
                .description("고가 상품 설명")
                .sellingStatus(SellingStatus.SELLING)
                .displayStatus(com.back.domain.product.product.entity.DisplayStatus.DISPLAYING)
                .minQuantity(1)
                .maxQuantity(100)
                .productModelName("고가 모델")
                .certification(false)
                .origin("한국")
                .material("고급 재질")
                .size("프리사이즈")
                .isPlanned(false)
                .isRestock(false)
                .isDeleted(false)
                .build();
        expensiveProduct = productRepository.save(expensiveProduct);

        OrderItem orderItem = OrderItem.createOrderItem(expensiveProduct, 1, null);
        Order expensiveOrder = Order.createOrder(
                buyer,
                List.of(orderItem),
                "서울시 강남구",
                "테헤란로 123",
                "06234",
                "구매자",
                "010-1111-1111",
                "빠른 배송 부탁드립니다",
                PaymentMethod.CARD
        );
        expensiveOrder = orderRepository.save(expensiveOrder);

        entityManager.flush();
        entityManager.clear();

        // when
        orderService.changeOrderStatus(
                expensiveOrder.getId(),
                new OrderStatusChangeRequestDto(OrderStatus.DELIVERED),
                admin // 관리자가 상태 변경
        );

        entityManager.flush();
        entityManager.clear();

        // then
        MoriCashBalance balance = moriCashBalanceRepository.findByUser(artist)
                .orElseThrow(() -> new AssertionError("작가의 모리캐시를 찾을 수 없습니다."));

        // 99,999원 - 9,999원(수수료) = 89,999원 (소수점 버림)
        int expectedRevenue = 99999 - (99999 / 10);
        assertThat(balance.getAvailableBalance()).isEqualTo(expectedRevenue);
    }
}
