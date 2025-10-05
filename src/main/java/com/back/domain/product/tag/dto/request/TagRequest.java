package com.back.domain.product.tag.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 태그 등록,수정용 DTO */
public record TagRequest(
        @NotBlank(message = "태그 이름은 필수입니다.")
        @Schema(description = "태그명", example = "심플")
        String tagName
) {
}
