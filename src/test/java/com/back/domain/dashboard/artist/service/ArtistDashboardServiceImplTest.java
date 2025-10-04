package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.request.ArtistFundingSearchRequest;
import com.back.domain.dashboard.artist.dto.request.ArtistProductSearchRequest;
import com.back.domain.dashboard.artist.dto.response.ArtistFundingResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.back.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * ArtistDashboardServiceImpl 테스트
 * 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.30 펀딩 실제 DB 연동에 맞춰 테스트 수정
 * 2025.10.02 JWT 표준 패턴 적용 - Request DTO 사용
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("작가 대시보드 서비스 구현체 테스트")
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

    private static final Long TEST_ARTIST_ID = 5L;

    @Test
    @DisplayName("상품 목록 조회 - 실제 DB 연동 테스트")
    void getProducts_ReturnsPaginatedResults() {
        // Given
        Product mockProduct1 = createMockProduct(101L, "상품A", 10000, 10, SellingStatus.SELLING);
        Product mockProduct2 = createMockProduct(102L, "상품B", 20000, 20, SellingStatus.SELLING);
        Product mockProduct3 = createMockProduct(103L, "상품C", 30000, 0, SellingStatus.SOLD_OUT);
        Product mockProduct4 = createMockProduct(104L, "상품D", 15000, 5, SellingStatus.BEFORE_SELLING);

        Page<Product> mockPage = new PageImpl<>(
                List.of(mockProduct1, mockProduct2, mockProduct3, mockProduct4),
                PageRequest.of(0, 10),
                4
        );

        when(productRepository.findProductsByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), eq("createDate"), eq("DESC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistProductSearchRequest request = new ArtistProductSearchRequest(
                0, 10, null, null, "createDate", "DESC"
        );

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(TEST_ARTIST_ID, request);

        // Then - 페이징 로직과 데이터 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getContent()).hasSize(4),
                () -> assertThat(result.getTotalElements()).isEqualTo(4),
                () -> assertThat(result.getTotalPages()).isEqualTo(1),
                () -> assertThat(result.isHasNext()).isFalse(),
                () -> assertThat(result.isHasPrevious()).isFalse(),
                // 첫 번째 상품 검증
                () -> assertThat(result.getContent().getFirst().productId()).isEqualTo(101L),
                () -> assertThat(result.getContent().getFirst().productName()).isEqualTo("상품A"),
                () -> assertThat(result.getContent().getFirst().price()).isEqualTo(10000),
                () -> assertThat(result.getContent().getFirst().discountRate()).isEqualTo(10),
                () -> assertThat(result.getContent().getFirst().discountPrice()).isEqualTo(9000),
                () -> assertThat(result.getContent().getFirst().sellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("판매중")
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 검색 키워드 필터 적용")
    void getProducts_WithKeywordFilter() {
        // Given
        Product mockProduct = createMockProduct(101L, "감성 포스터", 25000, 10, SellingStatus.SELLING);

        Page<Product> mockPage = new PageImpl<>(
                List.of(mockProduct),
                PageRequest.of(0, 10),
                1
        );

        when(productRepository.findProductsByArtist(
                eq(TEST_ARTIST_ID), eq("포스터"), isNull(), eq("createDate"), eq("DESC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistProductSearchRequest request = new ArtistProductSearchRequest(
                0, 10, "포스터", null, "createDate", "DESC"
        );

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().productName()).contains("포스터"),
                () -> assertThat(result.getTotalElements()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("상품 목록 조회 - 판매중 필터 적용")
    void getProducts_WithSellingFilter() {
        // Given
        Product mockProduct = createMockProduct(101L, "판매중 상품", 10000, 0, SellingStatus.SELLING);

        Page<Product> mockPage = new PageImpl<>(
                List.of(mockProduct),
                PageRequest.of(0, 10),
                1
        );

        when(productRepository.findProductsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq(true), eq("createDate"), eq("DESC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        ArtistProductSearchRequest request = new ArtistProductSearchRequest(
                0, 10, null, true, "createDate", "DESC"
        );

        // When
        ArtistProductResponse.List result = artistDashboardService.getProducts(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().sellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("판매중")
        );
    }

    @Test
    @DisplayName("펀딩 목록 조회 - 상태 필터 적용")
    void getFundings_WithStatusFilter() {
        // Given
        User mockUser = createMockUser(TEST_ARTIST_ID, "작가명");
        Funding mockFunding = createMockFunding(1L, mockUser, "성공한 펀딩", 500000L, FundingStatus.SUCCESS);

        Page<Funding> mockPage = new PageImpl<>(List.of(mockFunding), PageRequest.of(0, 10), 1);

        when(fundingRepository.findFundingsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq(FundingStatus.SUCCESS), eq("endDate"), eq("ASC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        when(fundingContributionRepository.sumContributedAmountByFundingId(1L)).thenReturn(500000L);

        ArtistFundingSearchRequest request = new ArtistFundingSearchRequest(
                0, 10, null, "SUCCESS", null, null, null, null, null, "endDate", "ASC"
        );

        // When
        ArtistFundingResponse.List result = artistDashboardService.getFundings(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().status()).isEqualTo("SUCCESS"),
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("성공")
        );
    }

    /**
     * Mock Product 생성 헬퍼 메서드
     */
    private Product createMockProduct(Long id, String name, int price, int discountRate, SellingStatus status) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .discountRate(discountRate)
                .sellingStatus(status)
                .isDeleted(false)
                .build();

        // Reflection을 통해 id와 createDate 설정
        try {
            java.lang.reflect.Field idField = product.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(product, id);

            java.lang.reflect.Field createDateField = product.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(product, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id or createDate", e);
        }

        return product;
    }

    /**
     * Mock User 생성 헬퍼 메서드
     */
    private User createMockUser(Long id, String name) {
        // User는 static factory 메서드 사용
        User user = User.createLocalUser(
                "test@example.com",
                "password",
                name,
                "010-1234-5678"
        );

        try {
            java.lang.reflect.Field idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }

        return user;
    }

    /**
     * Mock Funding 생성 헬퍼 메서드
     */
    private Funding createMockFunding(Long id, User user, String title, long targetAmount, FundingStatus status) {
        LocalDateTime now = LocalDateTime.now();

        Funding funding = Funding.builder()
                .user(user)
                .title(title)
                .description("테스트 펀딩 설명")
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

        // Reflection을 통해 id와 createDate 설정
        try {
            java.lang.reflect.Field idField = funding.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(funding, id);

            java.lang.reflect.Field createDateField = funding.getClass().getSuperclass().getDeclaredField("createDate");
            createDateField.setAccessible(true);
            createDateField.set(funding, now.minusDays(15));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id or createDate", e);
        }

        return funding;
    }

    @Test
    @DisplayName("작가 설정 정보 조회 - 실제 DB 연동 테스트")
    void getSettings_Success() {
        // Given
        User mockUser = createMockUser(TEST_ARTIST_ID, "테스트작가");
        com.back.domain.artist.entity.ArtistProfile mockProfile = createMockArtistProfile(mockUser);

        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.of(mockProfile));

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse result =
                artistDashboardService.getSettings(TEST_ARTIST_ID);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                // 프로필 정보 검증
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.profile().nickname()).isEqualTo("테스트작가"),
                () -> assertThat(result.profile().bio()).isEqualTo("작가 소개글입니다"),
                () -> assertThat(result.profile().sns()).hasSize(1),
                () -> assertThat(result.profile().sns().get(0).platform()).isEqualTo("Instagram"),
                () -> assertThat(result.profile().sns().get(0).handle()).isEqualTo("@test_artist"),
                () -> assertThat(result.profile().profileImageUrl()).isEqualTo("https://example.com/profile.jpg"),
                // 사업자 정보 검증
                () -> assertThat(result.business()).isNotNull(),
                () -> assertThat(result.business().address()).isEqualTo("서울특별시 강남구 테헤란로 123 상세주소"),
                () -> assertThat(result.business().verified()).isTrue(),
                // 계좌 정보 검증 (마스킹 확인)
                () -> assertThat(result.payout()).isNotNull(),
                () -> assertThat(result.payout().bankName()).isEqualTo("신한은행"),
                () -> assertThat(result.payout().accountHolder()).isEqualTo("홍길동"),
                () -> assertThat(result.payout().accountMasked()).isEqualTo("****-****-**1234"),
                () -> assertThat(result.payout().status()).isEqualTo("VERIFIED"),
                // 권한 정보 검증
                () -> assertThat(result.permissions()).isNotNull(),
                () -> assertThat(result.permissions().canEditProfile()).isTrue(),
                () -> assertThat(result.permissions().canEditBusiness()).isTrue(),
                () -> assertThat(result.permissions().canEditPayout()).isTrue()
        );
    }

    @Test
    @DisplayName("작가 설정 정보 조회 - 작가 프로필 없음")
    void getSettings_ProfileNotFound() {
        // Given
        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.empty());

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.back.global.exception.ServiceException.class,
                () -> artistDashboardService.getSettings(TEST_ARTIST_ID)
        );
    }

    @Test
    @DisplayName("계좌번호 마스킹 처리 테스트")
    void accountNumberMasking_Success() {
        // Given
        User mockUser = createMockUser(TEST_ARTIST_ID, "작가");

        // 다양한 계좌번호 패턴 테스트
        com.back.domain.artist.entity.ArtistProfile profile1 = createMockArtistProfileWithAccount(mockUser, "123-456-789012");
        com.back.domain.artist.entity.ArtistProfile profile2 = createMockArtistProfileWithAccount(mockUser, "1234567890");
        com.back.domain.artist.entity.ArtistProfile profile3 = createMockArtistProfileWithAccount(mockUser, "123");

        when(artistProfileRepository.findByUserId(TEST_ARTIST_ID))
                .thenReturn(java.util.Optional.of(profile1))
                .thenReturn(java.util.Optional.of(profile2))
                .thenReturn(java.util.Optional.of(profile3));

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse result1 =
                artistDashboardService.getSettings(TEST_ARTIST_ID);
        com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse result2 =
                artistDashboardService.getSettings(TEST_ARTIST_ID);
        com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse result3 =
                artistDashboardService.getSettings(TEST_ARTIST_ID);

        // Then
        assertAll(
                () -> assertThat(result1.payout().accountMasked()).isEqualTo("****-****-**9012"),
                () -> assertThat(result2.payout().accountMasked()).isEqualTo("****-****-**7890"),
                () -> assertThat(result3.payout().accountMasked()).isEqualTo("****")
        );
    }

    /**
     * Mock ArtistProfile 생성 헬퍼 메서드
     */
    private com.back.domain.artist.entity.ArtistProfile createMockArtistProfile(User user) {
        return com.back.domain.artist.entity.ArtistProfile.builder()
                .user(user)
                .artistApplication(null)
                .artistName("테스트작가")
                .mainProducts("주력상품")
                .snsAccount("@test_artist")
                .businessAddress("서울특별시 강남구 테헤란로 123")
                .businessAddressDetail("상세주소")
                .businessZipCode("06234")
                .managerPhone("010-1234-5678")
                .bankName("신한은행")
                .bankAccount("123-456-789-1234")
                .accountName("홍길동")
                .description("작가 소개글입니다")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
    }

    /**
     * 특정 계좌번호를 가진 Mock ArtistProfile 생성
     */
    private com.back.domain.artist.entity.ArtistProfile createMockArtistProfileWithAccount(User user, String accountNumber) {
        return com.back.domain.artist.entity.ArtistProfile.builder()
                .user(user)
                .artistApplication(null)
                .artistName("작가")
                .mainProducts("상품")
                .snsAccount("@artist")
                .businessAddress("주소")
                .businessAddressDetail(null)
                .businessZipCode("12345")
                .managerPhone("010-0000-0000")
                .bankName("은행")
                .bankAccount(accountNumber)
                .accountName("예금주")
                .description("소개")
                .profileImageUrl("https://example.com/img.jpg")
                .build();
    }

    // 나머지 Mock 데이터 기반 테스트들은 아직 실제 구현이 없으므로 주석 처리
    // TODO: 실제 Order, Cash, Settlement 등의 CRUD 구현 후 테스트 재작성

    @Test
    @DisplayName("취소 요청 목록 조회 - 기본 조회")
    void getCancellationRequests_Success() {
        // Given
        User mockCustomer = createMockUser(201L, "고객A");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "테스트 상품", 15000, 0, SellingStatus.SELLING);

        // Product의 user 설정 (작가)
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123456");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem = createMockRefundItem(1L, mockOrderItem, 1);
        com.back.domain.order.refund.entity.Refund mockRefund = createMockRefund(
                1L, mockOrder, mockCustomer, java.util.List.of(mockRefundItem),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED
        );

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(
                List.of(mockRefund),
                PageRequest.of(0, 20),
                1
        );

        when(refundRepository.findRefundsByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest request =
                new com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest(
                        0, 20, null, null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse.List result =
                artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.summary()).isNull(), // summary는 null
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.totalElements()).isEqualTo(1),
                () -> assertThat(result.totalPages()).isEqualTo(1),
                () -> assertThat(result.hasNext()).isFalse(),
                // 첫 번째 취소요청 검증
                () -> assertThat(result.content().getFirst().orderId()).isEqualTo("1"),
                () -> assertThat(result.content().getFirst().orderNumber()).isEqualTo("ORD123456"),
                () -> assertThat(result.content().getFirst().status()).isEqualTo("REQUESTED"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("처리대기"),
                () -> assertThat(result.content().getFirst().customer().nickname()).isEqualTo("고객A"),
                () -> assertThat(result.content().getFirst().orderItem().productName()).isEqualTo("테스트 상품"),
                // 권한 검증 (REQUESTED 상태이므로 승인/거절 가능)
                () -> assertThat(result.content().getFirst().permissions().canApprove()).isTrue(),
                () -> assertThat(result.content().getFirst().permissions().canReject()).isTrue()
        );
    }

    @Test
    @DisplayName("취소 요청 목록 조회 - 키워드 검색 (상품명)")
    void getCancellationRequests_WithProductNameKeyword() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "손흥민 포스터", 15000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem = createMockRefundItem(1L, mockOrderItem, 1);
        com.back.domain.order.refund.entity.Refund mockRefund = createMockRefund(
                1L, mockOrder, mockCustomer, List.of(mockRefundItem),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED
        );

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(
                List.of(mockRefund),
                PageRequest.of(0, 20),
                1
        );

        when(refundRepository.findRefundsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq("손흥민"), any(PageRequest.class)))
                .thenReturn(mockPage);

        com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest request =
                new com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest(
                        0, 20, null, "손흥민", null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse.List result =
                artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().orderItem().productName()).contains("손흥민")
        );
    }

    @Test
    @DisplayName("취소 요청 목록 조회 - 키워드 검색 (구매자 이름)")
    void getCancellationRequests_WithCustomerNameKeyword() {
        // Given
        User mockCustomer = createMockUser(201L, "홍길동");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "상품", 15000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem = createMockRefundItem(1L, mockOrderItem, 1);
        com.back.domain.order.refund.entity.Refund mockRefund = createMockRefund(
                1L, mockOrder, mockCustomer, List.of(mockRefundItem),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED
        );

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(
                List.of(mockRefund),
                PageRequest.of(0, 20),
                1
        );

        when(refundRepository.findRefundsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq("홍길동"), any(PageRequest.class)))
                .thenReturn(mockPage);

        com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest request =
                new com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest(
                        0, 20, null, "홍길동", null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse.List result =
                artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().customer().nickname()).contains("홍길동")
        );
    }

    @Test
    @DisplayName("취소 요청 목록 조회 - 상태별 필터")
    void getCancellationRequests_WithStatusFilter() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct = createMockProduct(101L, "상품", 15000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder = createMockOrder(1L, mockCustomer, "ORD123");
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem = createMockOrderItem(1L, mockOrder, mockProduct, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem = createMockRefundItem(1L, mockOrderItem, 1);
        com.back.domain.order.refund.entity.Refund mockRefund = createMockRefund(
                1L, mockOrder, mockCustomer, List.of(mockRefundItem),
                com.back.domain.order.refund.entity.Refund.RefundStatus.COMPLETED
        );

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(
                List.of(mockRefund),
                PageRequest.of(0, 20),
                1
        );

        when(refundRepository.findRefundsByArtist(
                eq(TEST_ARTIST_ID), eq(com.back.domain.order.refund.entity.Refund.RefundStatus.COMPLETED),
                isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest request =
                new com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest(
                        0, 20, "COMPLETED", null, null, null, null, null, null
                );

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse.List result =
                artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(1),
                () -> assertThat(result.content().getFirst().status()).isEqualTo("COMPLETED"),
                () -> assertThat(result.content().getFirst().statusText()).isEqualTo("승인됨"),
                // COMPLETED 상태이므로 승인/거절 불가
                () -> assertThat(result.content().getFirst().permissions().canApprove()).isFalse(),
                () -> assertThat(result.content().getFirst().permissions().canReject()).isFalse()
        );
    }

    @Test
    @DisplayName("취소 요청 목록 조회 - 상품명 정렬 (오름차순)")
    void getCancellationRequests_SortByProductNameAsc() {
        // Given
        User mockCustomer = createMockUser(201L, "고객");
        User mockArtist = createMockUser(TEST_ARTIST_ID, "작가");
        Product mockProduct1 = createMockProduct(101L, "A상품", 10000, 0, SellingStatus.SELLING);
        Product mockProduct2 = createMockProduct(102L, "B상품", 20000, 0, SellingStatus.SELLING);
        setProductUser(mockProduct1, mockArtist);
        setProductUser(mockProduct2, mockArtist);

        com.back.domain.order.order.entity.Order mockOrder1 = createMockOrder(1L, mockCustomer, "ORD001");
        com.back.domain.order.order.entity.Order mockOrder2 = createMockOrder(2L, mockCustomer, "ORD002");

        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem1 = createMockOrderItem(1L, mockOrder1, mockProduct1, 1);
        com.back.domain.order.orderItem.entity.OrderItem mockOrderItem2 = createMockOrderItem(2L, mockOrder2, mockProduct2, 1);

        com.back.domain.order.refund.entity.RefundItem mockRefundItem1 = createMockRefundItem(1L, mockOrderItem1, 1);
        com.back.domain.order.refund.entity.RefundItem mockRefundItem2 = createMockRefundItem(2L, mockOrderItem2, 1);

        com.back.domain.order.refund.entity.Refund mockRefund1 = createMockRefund(
                1L, mockOrder1, mockCustomer, List.of(mockRefundItem1),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED
        );
        com.back.domain.order.refund.entity.Refund mockRefund2 = createMockRefund(
                2L, mockOrder2, mockCustomer, List.of(mockRefundItem2),
                com.back.domain.order.refund.entity.Refund.RefundStatus.REQUESTED
        );

        Page<com.back.domain.order.refund.entity.Refund> mockPage = new PageImpl<>(
                List.of(mockRefund1, mockRefund2),
                PageRequest.of(0, 20),
                2
        );

        when(refundRepository.findRefundsByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(mockPage);

        com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest request =
                new com.back.domain.dashboard.artist.dto.request.ArtistCancellationSearchRequest(
                        0, 20, null, null, null, null, null, "productName", "ASC"
                );

        // When
        com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse.List result =
                artistDashboardService.getCancellationRequests(TEST_ARTIST_ID, request);

        // Then
        assertAll(
                () -> assertThat(result.content()).hasSize(2),
                () -> assertThat(result.content().get(0).orderItem().productName()).isEqualTo("A상품"),
                () -> assertThat(result.content().get(1).orderItem().productName()).isEqualTo("B상품")
        );
    }

    /**
     * Product에 User 설정 헬퍼 메서드
     */
    private void setProductUser(Product product, User user) {
        try {
            java.lang.reflect.Field userField = product.getClass().getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(product, user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user for product", e);
        }
    }

    /**
     * Mock Order 생성 헬퍼 메서드
     */
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

    /**
     * Mock OrderItem 생성 헬퍼 메서드
     */
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

    /**
     * Mock RefundItem 생성 헬퍼 메서드
     */
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

    /**
     * Mock Refund 생성 헬퍼 메서드
     */
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
                        .detailReason("상세 사유입니다")
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

            // RefundItem에 Refund 설정
            for (com.back.domain.order.refund.entity.RefundItem item : refundItems) {
                java.lang.reflect.Field refundField = item.getClass().getDeclaredField("refund");
                refundField.setAccessible(true);
                refundField.set(item, refund);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set refund fields", e);
        }

        return refund;
    }
}
