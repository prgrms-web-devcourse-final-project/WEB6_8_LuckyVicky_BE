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
        Page<Funding> allFundings = new PageImpl<>(List.of(mockFunding), PageRequest.of(0, Integer.MAX_VALUE), 1);

        when(fundingRepository.findFundingsByArtist(
                eq(TEST_ARTIST_ID), isNull(), eq(FundingStatus.SUCCESS), eq("endDate"), eq("ASC"), any(PageRequest.class)))
                .thenReturn(mockPage);

        when(fundingRepository.findFundingsByArtist(
                eq(TEST_ARTIST_ID), isNull(), isNull(), eq("createDate"), eq("DESC"), any(PageRequest.class)))
                .thenReturn(allFundings);

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
                () -> assertThat(result.getContent().getFirst().statusText()).isEqualTo("성공"),
                () -> assertThat(result.getSummary().successFundings()).isEqualTo(1)
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

        Funding funding = Funding.create(
                user,
                title,
                "테스트 펀딩 설명",
                "https://example.com/image.jpg",
                targetAmount,
                now.plusDays(1),          // 시작일은 현재 이후여야 함 (검증 통과)
                now.plusDays(20),
                status,                   // 전달받은 상태
                List.of()                 // 옵션 비워두기 (new ArrayList<>() 불필요)
        );

// 도메인 메서드로 참여자 수 반영 (직접 세터/빌더로 넣지 않음)
        funding.increaseParticipantCount(10);


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

    // 나머지 Mock 데이터 기반 테스트들은 아직 실제 구현이 없으므로 주석 처리
    // TODO: 실제 Order, Cash, Settlement 등의 CRUD 구현 후 테스트 재작성
}
