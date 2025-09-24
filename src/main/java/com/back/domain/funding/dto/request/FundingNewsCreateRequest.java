package com.back.domain.funding.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FundingNewsCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        String imageUrl
) {}