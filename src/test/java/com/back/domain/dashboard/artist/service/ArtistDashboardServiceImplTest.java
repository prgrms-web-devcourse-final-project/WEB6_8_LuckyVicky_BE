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

    private void setProductUser(Product product, User user) {
        try {
            java.lang.reflect.Field userField = product.getClass().getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(product, user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user", e);
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
