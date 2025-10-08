package com.back.domain.product.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 카테고리 등록, 수정용 DTO
 */
public record CategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Schema(description = "카테고리명", example = "스티커")
        String categoryName,

        @Schema(description = "상위 카테고리 Id (null이면 상위 카테고리)", example = "null")
        Long parentId // null이면 상위 카테고리, null이 아니면 해당 parentId를 상위 카테고리로 가지는 하위 카테고리
) {
}
