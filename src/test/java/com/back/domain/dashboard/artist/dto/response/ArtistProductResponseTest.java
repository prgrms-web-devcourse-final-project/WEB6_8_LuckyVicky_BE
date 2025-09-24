package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistProductResponse DTO 테스트
 * 상품 목록과 페이징 로직에 집중
 * 2025.09.23 생성
 */
@DisplayName("ArtistProductResponse DTO 테스트")
public class ArtistProductResponseTest {

    @Test
    @DisplayName("상품 정보 구조 생성 및 검증")
    void createProduct_Success() {
        // When
        ArtistProductResponse.Product product = createSampleProduct();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(product).isNotNull(),
                () -> assertThat(product.getProductNumber()).isNotBlank(),
                () -> assertThat(product.getProductName()).isNotBlank(),
                () -> assertThat(product.getPrice()).isNotNegative(),
                () -> assertThat(product.getSellingStatus()).isNotBlank(),
                () -> assertThat(product.getStatusText()).isNotBlank(),
                () -> assertThat(product.getRegistrationDate()).isNotBlank()
        );
    }

    @Test
    @DisplayName("상품 상태별 검증")
    void validateProductStatuses_Success() {
        // Given
        List<ArtistProductResponse.Product> products = Arrays.asList(
                createProductWithStatus("SELLING", "판매중"),
                createProductWithStatus("STOP_SELLING", "판매중단"),
                createProductWithStatus("SOLD_OUT", "품절")
        );

        // Then - 상태별 검증
        assertAll(
                () -> assertThat(products.get(0).getSellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(products.get(0).getStatusText()).isEqualTo("판매중"),
                () -> assertThat(products.get(1).getSellingStatus()).isEqualTo("STOP_SELLING"),
                () -> assertThat(products.get(1).getStatusText()).isEqualTo("판매중단"),
                () -> assertThat(products.get(2).getSellingStatus()).isEqualTo("SOLD_OUT"),
                () -> assertThat(products.get(2).getStatusText()).isEqualTo("품절")
        );
    }

    @Test
    @DisplayName("상품 목록 페이징 구조 검증")
    void validatePagination_Success() {
        // Given
        List<ArtistProductResponse.Product> products = Arrays.asList(
                createSampleProduct(),
                createSampleProduct(),
                createSampleProduct()
        );

        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                products, 0, 10, 25L, 3, true, false
        );

        // Then - 페이징 로직 검증
        assertAll(
                () -> assertThat(response.getContent()).hasSize(3),
                () -> assertThat(response.getPage()).isNotNegative(),
                () -> assertThat(response.getSize()).isPositive(),
                () -> assertThat(response.getTotalElements()).isNotNegative(),
                () -> assertThat(response.getTotalPages()).isPositive(),
                () -> assertThat(response.isHasNext()).isTrue(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("빈 목록과 마지막 페이지 처리")
    void handleEdgeCases_Success() {
        // Given - 빈 목록
        ArtistProductResponse.List emptyResponse = new ArtistProductResponse.List(
                Arrays.asList(), 0, 10, 0L, 0, false, false
        );

        // Given - 마지막 페이지
        ArtistProductResponse.List lastPageResponse = new ArtistProductResponse.List(
                Arrays.asList(createSampleProduct()), 2, 10, 21L, 3, false, true
        );

        // Then
        assertAll(
                // 빈 목록 검증
                () -> assertThat(emptyResponse.getContent()).isEmpty(),
                () -> assertThat(emptyResponse.getTotalElements()).isEqualTo(0L),
                () -> assertThat(emptyResponse.isHasNext()).isFalse(),
                // 마지막 페이지 검증
                () -> assertThat(lastPageResponse.isHasNext()).isFalse(),
                () -> assertThat(lastPageResponse.isHasPrevious()).isTrue()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // Given
        List<ArtistProductResponse.Product> products = Arrays.asList(
                ArtistProductResponse.Product.builder()
                        .productNumber("ART-20250922-001")
                        .productName("디지털 아트 작품 #1")
                        .price(350000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025-09-22")
                        .build(),
                ArtistProductResponse.Product.builder()
                        .productNumber("ART-20250921-002")
                        .productName("수채화 풍경화")
                        .price(750000)
                        .sellingStatus("STOP_SELLING")
                        .statusText("판매중단")
                        .registrationDate("2025-09-21")
                        .build()
        );

        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                products, 0, 10, 50L, 5, true, false
        );

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.getContent()).hasSize(2),
                () -> assertThat(response.getContent().get(0).getProductNumber()).isEqualTo("ART-20250922-001"),
                () -> assertThat(response.getContent().get(1).getSellingStatus()).isEqualTo("STOP_SELLING"),
                () -> assertThat(response.getTotalElements()).isEqualTo(50L),
                () -> assertThat(response.getTotalPages()).isEqualTo(5)
        );
    }

    // -------------- 헬퍼 메서드들----------------

    private ArtistProductResponse.Product createSampleProduct() {
        return ArtistProductResponse.Product.builder()
                .productNumber("PROD-001")
                .productName("테스트 작품")
                .price(150000)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025-09-22")
                .build();
    }

    private ArtistProductResponse.Product createProductWithStatus(String status, String statusText) {
        return ArtistProductResponse.Product.builder()
                .productNumber("PROD-" + status)
                .productName("상품명")
                .price(100000)
                .sellingStatus(status)
                .statusText(statusText)
                .registrationDate("2025-09-22")
                .build();
    }
}
