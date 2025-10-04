package com.back.domain.funding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "펀딩 새소식 작성 요청")
public record FundingNewsCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        String imageUrl
) {}