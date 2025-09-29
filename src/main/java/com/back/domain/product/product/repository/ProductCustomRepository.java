package com.back.domain.product.product.repository;

import com.back.domain.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

// JPA 기본 CRUD메서드만으로는 복잡한 동적 조건 처리 (정렬, 필터링 등)
public interface ProductCustomRepository {

    //상품 목록 조회
    ProductListResponse findProducts(
            Long categoryId, // 조회할 카테고리 ID
            List<Long> tagIds, // 태그 필터
            Integer minPrice, // 최소 가격 필터
            Integer maxPrice, // 최대 가격 필터
            String deliveryType, // 배송유형 필터
            String sort, // 정렬 기준 (newest, priceAsc, priceDesc, 이후 인기순도 추가)
            Pageable pageable // 페이징
    );
}