package com.back.domain.funding.dto.request;


import com.back.global.s3.S3FileRequest;
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

        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId, // 상위 카테고리만 가능 (parent == null)

        String imageUrl,

        @NotNull(message = "목표 금액은 필수입니다.")
        @Positive(message = "목표 금액은 0보다 커야 합니다.")
        long targetAmount,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        long price,

        Integer stock, // null이면 무제한

        @NotNull(message = "시작일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,

        @NotNull(message = "종료일은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate,

        List<S3FileRequest> images
) {}