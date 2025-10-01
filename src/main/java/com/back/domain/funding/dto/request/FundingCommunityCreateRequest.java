package com.back.domain.funding.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FundingCommunityCreateRequest(
        @NotBlank String content
) {}