package com.back.domain.product.category.dto.response;

import java.util.List;

/**
 * 카테고리 조회용 DTO
 */
public record CategoryResponse(
        Long id,
        String categoryName,
        List<CategoryResponse> subCategories // 하위 카테고리 리스트
) {
}
