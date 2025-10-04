package com.back.domain.funding.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "펀딩 커뮤니티 글 작성 요청")
public record FundingCommunityCreateRequest(
        @NotBlank String content
) {}