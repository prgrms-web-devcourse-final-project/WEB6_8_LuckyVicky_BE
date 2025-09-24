package com.back.domain.funding.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FundingCreateRequest(
        @NotBlank(message = "펀딩 제목은 필수입니다.")
        @Size(max = 50, message = "펀딩 제목은 최대 50자까지 가능합니다.")
        String title,

        @NotBlank(message = "펀딩 설명은 필수입니다.")
        String description,

        @NotNull(message = "목표 금액은 필수입니다.")
        @Positive(message = "목표 금액은 0보다 커야 합니다.")
        long targetAmount,

        @NotBlank(message = "대표 이미지 URL은 필수입니다.")
        String imageUrl,

        @NotNull(message = "펀딩 옵션은 최소 1개 이상 필요합니다.")
        @Size(min = 1, message = "펀딩 옵션은 최소 1개 이상 등록해야 합니다.")
        List<FundingOptionRequest> options
) {
    public record FundingOptionRequest(
            @NotBlank(message = "옵션명은 필수입니다.")
            String name,

            @NotNull(message = "가격은 필수입니다.")
            @Positive(message = "가격은 0보다 커야 합니다.")
            long price,

            @NotNull(message = "재고는 필수입니다.")
            @Positive(message = "재고는 0보다 커야 합니다.")
            Integer stock
    ) {}
}