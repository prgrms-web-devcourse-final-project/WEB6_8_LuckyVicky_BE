package com.back.domain.funding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "펀딩 수정 요청")
public record FundingUpdateRequest(

        @Schema(description = "펀딩 제목", example = "펀딩 제목 수정")
        @Size(max = 50, message = "제목은 50자까지 가능합니다.")
        String title,

        @Schema(description = "펀딩 설명", example = "펀딩 설명 수정")
        String description,

        @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "목표 금액", example = "1200000")
        @Positive(message = "목표 금액은 0보다 커야 합니다.")
        Long targetAmount,

        @Schema(description = "펀딩 종료일", example = "2025-12-31T12:30:00")
        @Future
        LocalDateTime endDate,

        @Schema(description = "펀딩 옵션 목록 (없으면 미변경)")
        List<FundingOptionRequest> options
) {

    @Schema(description = "펀딩 옵션 수정 요청")
    public record FundingOptionRequest(

            @Schema(description = "기존 옵션 ID (없으면 신규 추가)", example = "10")
            Long id,

            @Schema(description = "옵션 이름", example = "옵션 A")
            String name,

            @Schema(description = "옵션 가격", example = "50000")
            @Positive
            long price,

            @Schema(description = "옵션 재고", example = "100")
            @Positive
            Integer stock,

            @Schema(description = "옵션 정렬 순서", example = "1")
            Integer sortOrder
    ) {}
}