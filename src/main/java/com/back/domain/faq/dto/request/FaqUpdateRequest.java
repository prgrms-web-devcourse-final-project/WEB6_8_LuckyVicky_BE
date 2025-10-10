package com.back.domain.faq.dto.request;

import com.back.domain.faq.entity.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "FAQ 수정 요청")
public record FaqUpdateRequest(

        @Schema(description = "질문", example = "회원가입은 어떻게 하나요?")
        @Size(max = 200, message = "질문은 200자를 초과할 수 없습니다")
        String question,

        @Schema(description = "답변", example = "회원가입 버튼을 클릭하여 이메일 또는 소셜 계정으로 가입할 수 있습니다.")
        String answer,

        @Schema(description = "카테고리", example = "ACCOUNT")
        FaqCategory category
) {}
