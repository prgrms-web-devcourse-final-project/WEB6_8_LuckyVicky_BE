package com.back.domain.faq.dto.response;

import com.back.domain.faq.entity.Faq;
import com.back.domain.faq.entity.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "FAQ 상세 조회 응답")
public record FaqDetailResponse(

        @Schema(description = "FAQ ID", example = "1")
        Long id,

        @Schema(description = "질문", example = "회원가입은 어떻게 하나요?")
        String question,

        @Schema(description = "답변", example = "회원가입 버튼을 클릭하여 이메일 또는 소셜 계정으로 가입할 수 있습니다.")
        String answer,

        @Schema(description = "카테고리", example = "ACCOUNT")
        FaqCategory category,

        @Schema(description = "카테고리 표시명", example = "회원/가입")
        String categoryDisplayName,

        @Schema(description = "조회수", example = "150")
        Long viewCount,

        @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createDate,

        @Schema(description = "수정일시", example = "2024-01-16T14:20:00")
        LocalDateTime modifyDate
) {
    /**
     * Entity -> DTO 변환
     */
    public static FaqDetailResponse from(Faq faq) {
        return new FaqDetailResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory(),
                faq.getCategory().getDisplayName(),
                faq.getViewCount(),
                faq.getCreateDate(),
                faq.getModifyDate()
        );
    }
}