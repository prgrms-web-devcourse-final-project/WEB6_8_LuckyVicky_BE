package com.back.domain.product.tag.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 태그 등록,수정용 DTO */
public record TagRequest(
        @NotBlank(message = "태그 이름은 필수입니다.")
        String tagName
) {
}
