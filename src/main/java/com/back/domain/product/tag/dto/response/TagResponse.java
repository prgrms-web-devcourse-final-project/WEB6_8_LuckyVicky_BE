package com.back.domain.product.tag.dto.response;

/** 태그 조회용 DTO */
public record TagResponse(
        Long id,
        String tagName
) {
}
