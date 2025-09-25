package com.back.domain.funding.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record FundingCreateRequest(
        @NotBlank(message = "펀딩 제목은 필수입니다.")
        @Size(max = 50, message = "펀딩 제목은 최대 50자까지 가능합니다.")
        String title,

        @NotBlank(message = "펀딩 설명은 필수입니다.")
        String description,

        @NotBlank(message = "대표 이미지 URL은 필수입니다.")
        String imageUrl,

//        @NotNull(message = "카테고리는 필수입니다.")
//        Long categoryId, // 상위 카테고리만 가능 (parent == null)

        @NotNull(message = "목표 금액은 필수입니다.")
        @Positive(message = "목표 금액은 0보다 커야 합니다.")
        long targetAmount,

        @NotNull(message = "시작일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,

        @NotNull(message = "종료일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate,

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
            Integer stock,

            @NotNull(message = "정렬 순서는 필수입니다.")
            Integer sortOrder
    ) {}
}