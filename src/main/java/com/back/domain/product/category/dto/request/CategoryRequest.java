package com.back.domain.product.category.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 카테고리 등록, 수정용 DTO
 */
public record CategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String categoryName,

        Long parentId // null이면 상위 카테고리, null이 아니면 해당 parentId를 상위 카테고리로 가지는 하위 카테고리
) {
}
