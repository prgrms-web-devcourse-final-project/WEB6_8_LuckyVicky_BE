package com.back.domain.product.product.repository;

import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductCustomRepository {

    /**
     * 상품 목록 조회 (고객용)
     */
    ProductListResponse findProducts(
            Long categoryId, // 조회할 카테고리 ID
            List<Long> tagIds, // 태그 필터
            Integer minPrice, // 최소 가격 필터
            Integer maxPrice, // 최대 가격 필터
            String deliveryType, // 배송유형 필터
            String sort, // 정렬 기준 (newest, priceAsc, priceDesc, 이후 인기순도 추가)
            Pageable pageable // 페이징
    );

    /**
     * 작가의 상품 목록 조회 (대시보드용)
     * 
     * @param userId 작가 ID
     * @param keyword 검색 키워드 (상품명)
     * @param selling 판매 중 필터 (true: 판매중만, false: 전체, null: 전체)
     * @param sort 정렬 기준 (createDate, name, price, sellingStatus)
     * @param order 정렬 방향 (ASC, DESC)
     * @param pageable 페이징
     * @return 작가의 상품 목록
     */
    Page<Product> findProductsByArtist(
            Long userId,
            String keyword,
            Boolean selling,
            String sort,
            String order,
            Pageable pageable
    );

    // 검색(상품)
    List<Product> searchByProductNameOrBrandName(String keyword);
}
