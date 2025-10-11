package com.back.domain.support.faq.dto.request;

import com.back.domain.support.faq.entity.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "FAQ 생성 요청")
public record FaqCreateRequest(

        @Schema(description = "질문", example = "회원가입은 어떻게 하나요?")
        @NotBlank(message = "질문은 필수입니다")
        @Size(max = 200, message = "질문은 200자를 초과할 수 없습니다")
        String question,

        @Schema(description = "답변", example = "회원가입 버튼을 클릭하여 이메일 또는 소셜 계정으로 가입할 수 있습니다.")
        @NotBlank(message = "답변은 필수입니다")
        String answer,

        @Schema(description = "카테고리", example = "ACCOUNT")
        @NotNull(message = "카테고리는 필수입니다")
        FaqCategory category
) {
}
