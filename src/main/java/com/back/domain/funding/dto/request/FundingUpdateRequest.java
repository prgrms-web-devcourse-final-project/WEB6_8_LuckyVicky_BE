package com.back.domain.funding.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FundingUpdateRequest(
        @Size(max = 50) String title,
        String description,
        @Positive long targetAmount,
        String imageUrl,
        List<FundingOptionRequest> options // 옵션 수정(없으면 미변경)
) {
    public record FundingOptionRequest(
            Long id,                 // 기존 옵션 식별자(없으면 신규 추가)
            String name,
            @Positive long price,
            @Positive Integer stock
    ) {}
}