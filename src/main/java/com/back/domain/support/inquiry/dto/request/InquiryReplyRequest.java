package com.back.domain.support.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 문의 댓글 작성/수정 요청 DTO
 */
@Schema(description = "문의 댓글 작성/수정 요청")
public record InquiryReplyRequest(

        @Schema(description = "댓글 내용", example = "답변 감사합니다.")
        @NotBlank(message = "댓글 내용은 필수입니다")
        String content,

        @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "1")
        Long parentReplyId
) {
}