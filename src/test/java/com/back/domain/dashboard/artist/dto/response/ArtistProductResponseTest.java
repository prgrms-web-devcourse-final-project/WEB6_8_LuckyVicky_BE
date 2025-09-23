package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistProductResponse DTO 테스트
 * Builder 패턴과 데이터 구조의 정확성을 검증
 * 2025.09.22 생성
 */
@DisplayName("ArtistProductResponse DTO 테스트")
public class ArtistProductResponseTest {

    @Test
    @DisplayName("Product Builder 패턴 테스트")
    void builder_Product_Success() {
        // When
        ArtistProductResponse.Product product = ArtistProductResponse.Product.builder()
                .productNumber("PROD-001")
                .productName("테스트 작품")
                .price(150000)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025-09-22")
                .build();

        // Then
        assertAll(
                () -> assertThat(product).isNotNull(),
                () -> assertThat(product.getProductNumber()).isEqualTo("PROD-001"),
                () -> assertThat(product.getProductName()).isEqualTo("테스트 작품"),
                () -> assertThat(product.getPrice()).isEqualTo(150000),
                () -> assertThat(product.getSellingStatus()).isEqualTo("SELLING"),
                () -> assertThat(product.getStatusText()).isEqualTo("판매중"),
                () -> assertThat(product.getRegistrationDate()).isEqualTo("2025-09-22")
        );
    }

    @Test
    @DisplayName("Product 판매 중단 상태 테스트")
    void builder_Product_StoppedSelling() {
        // When
        ArtistProductResponse.Product product = ArtistProductResponse.Product.builder()
                .productNumber("PROD-002")
                .productName("중단된 작품")
                .price(200000)
                .sellingStatus("STOP_SELLING")
                .statusText("판매중단")
                .registrationDate("2025-09-15")
                .build();

        // Then
        assertAll(
                () -> assertThat(product.getSellingStatus()).isEqualTo("STOP_SELLING"),
                () -> assertThat(product.getStatusText()).isEqualTo("판매중단")
        );
    }

    @Test
    @DisplayName("Product 품절 상태 테스트")
    void builder_Product_SoldOut() {
        // When
        ArtistProductResponse.Product product = ArtistProductResponse.Product.builder()
                .productNumber("PROD-003")
                .productName("품절된 작품")
                .price(300000)
                .sellingStatus("SOLD_OUT")
                .statusText("품절")
                .registrationDate("2025-09-10")
                .build();

        // Then
        assertAll(
                () -> assertThat(product.getSellingStatus()).isEqualTo("SOLD_OUT"),
                () -> assertThat(product.getStatusText()).isEqualTo("품절")
        );
    }

    @Test
    @DisplayName("Product 가격 0원 테스트")
    void builder_Product_FreePrice() {
        // When
        ArtistProductResponse.Product product = ArtistProductResponse.Product.builder()
                .productNumber("PROD-004")
                .productName("무료 작품")
                .price(0)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025-09-22")
                .build();

        // Then
        assertThat(product.getPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("Product 고가 작품 테스트")
    void builder_Product_HighPrice() {
        // When
        ArtistProductResponse.Product product = ArtistProductResponse.Product.builder()
                .productNumber("PROD-005")
                .productName("고가 작품")
                .price(10000000)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025-09-01")
                .build();

        // Then
        assertThat(product.getPrice()).isEqualTo(10000000);
    }

    @Test
    @DisplayName("List 기본 생성자 테스트")
    void constructor_List_Default() {
        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List();

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("List 전체 매개변수 생성자 테스트")
    void constructor_List_WithParameters() {
        // Given
        java.util.List<ArtistProductResponse.Product> products = Arrays.asList(
                ArtistProductResponse.Product.builder()
                        .productNumber("PROD-001")
                        .productName("작품1")
                        .price(100000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025-09-22")
                        .build(),
                ArtistProductResponse.Product.builder()
                        .productNumber("PROD-002")
                        .productName("작품2")
                        .price(200000)
                        .sellingStatus("SOLD_OUT")
                        .statusText("품절")
                        .registrationDate("2025-09-20")
                        .build()
        );

        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                products, 0, 10, 25L, 3, true, false
        );

        // Then
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getContent()).hasSize(2),
                () -> assertThat(response.getPage()).isEqualTo(0),
                () -> assertThat(response.getSize()).isEqualTo(10),
                () -> assertThat(response.getTotalElements()).isEqualTo(25L),
                () -> assertThat(response.getTotalPages()).isEqualTo(3),
                () -> assertThat(response.isHasNext()).isTrue(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("빈 상품 목록 테스트")
    void constructor_List_EmptyProducts() {
        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                Arrays.asList(), 0, 10, 0L, 0, false, false
        );

        // Then
        assertAll(
                () -> assertThat(response.getContent()).isEmpty(),
                () -> assertThat(response.getTotalElements()).isEqualTo(0L),
                () -> assertThat(response.getTotalPages()).isEqualTo(0),
                () -> assertThat(response.isHasNext()).isFalse(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("마지막 페이지 테스트")
    void constructor_List_LastPage() {
        // Given
        java.util.List<ArtistProductResponse.Product> products = Arrays.asList(
                ArtistProductResponse.Product.builder()
                        .productNumber("PROD-020")
                        .productName("마지막 작품")
                        .price(500000)
                        .sellingStatus("SELLING")
                        .statusText("판매중")
                        .registrationDate("2025-09-22")
                        .build()
        );

        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                products, 2, 10, 21L, 3, false, true
        );

        // Then
        assertAll(
                () -> assertThat(response.getPage()).isEqualTo(2),
                () -> assertThat(response.isHasNext()).isFalse(),
                () -> assertThat(response.isHasPrevious()).isTrue()
        );
    }

    @Test
    @DisplayName("API 명세와 동일한 구조 생성 테스트")
    void createApiResponseStructure() {
        // Given
        java.util.List<ArtistProductResponse.Product> products = Arrays.asList(
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
                        .build(),
                ArtistProductResponse.Product.builder()
                        .productNumber("ART-20250920-003")
                        .productName("추상화 시리즈 #3")
                        .price(1200000)
                        .sellingStatus("SOLD_OUT")
                        .statusText("품절")
                        .registrationDate("2025-09-20")
                        .build()
        );

        // When
        ArtistProductResponse.List response = new ArtistProductResponse.List(
                products, 0, 10, 50L, 5, true, false
        );

        // Then
        assertAll(
                () -> assertThat(response.getContent()).hasSize(3),
                () -> assertThat(response.getContent().get(0).getProductNumber()).isEqualTo("ART-20250922-001"),
                () -> assertThat(response.getContent().get(0).getProductName()).isEqualTo("디지털 아트 작품 #1"),
                () -> assertThat(response.getContent().get(0).getPrice()).isEqualTo(350000),
                () -> assertThat(response.getContent().get(1).getSellingStatus()).isEqualTo("STOP_SELLING"),
                () -> assertThat(response.getContent().get(2).getSellingStatus()).isEqualTo("SOLD_OUT"),
                () -> assertThat(response.getTotalElements()).isEqualTo(50L),
                () -> assertThat(response.getTotalPages()).isEqualTo(5),
                () -> assertThat(response.isHasNext()).isTrue(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("다양한 상품 정보 조합 테스트")
    void builder_Product_VariousData() {
        // When
        ArtistProductResponse.Product product1 = ArtistProductResponse.Product.builder()
                .productNumber("LONG-PRODUCT-NUMBER-123456789")
                .productName("매우 긴 상품명을 가진 특별한 한정판 아트웍 컬렉션 시리즈")
                .price(999999)
                .sellingStatus("SELLING")
                .statusText("판매중")
                .registrationDate("2025-01-01")
                .build();

        ArtistProductResponse.Product product2 = ArtistProductResponse.Product.builder()
                .productNumber("A")
                .productName("짧")
                .price(1)
                .sellingStatus("SOLD_OUT")
                .statusText("품절")
                .registrationDate("2025-12-31")
                .build();

        // Then
        assertAll(
                () -> assertThat(product1.getProductNumber()).isEqualTo("LONG-PRODUCT-NUMBER-123456789"),
                () -> assertThat(product1.getProductName()).contains("매우 긴 상품명"),
                () -> assertThat(product1.getPrice()).isEqualTo(999999),
                () -> assertThat(product2.getProductNumber()).isEqualTo("A"),
                () -> assertThat(product2.getProductName()).isEqualTo("짧"),
                () -> assertThat(product2.getPrice()).isEqualTo(1)
        );
    }
}
