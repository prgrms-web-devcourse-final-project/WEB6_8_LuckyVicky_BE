package com.back.domain.support.inquiry.dto.response;

import com.back.domain.support.inquiry.entity.InquiryReply;
import com.back.domain.support.inquiry.entity.ReplyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 댓글 응답 DTO
 */
@Schema(description = "문의 댓글 응답")
public record InquiryReplyResponse(

        @Schema(description = "댓글 ID", example = "1")
        Long id,

        @Schema(description = "댓글 내용", example = "답변드립니다.")
        String content,

        @Schema(description = "댓글 유형", example = "ADMIN")
        ReplyType replyType,

        @Schema(description = "작성자 ID", example = "456")
        Long authorId,

        @Schema(description = "작성자 이름", example = "관리자")
        String authorName,

        @Schema(description = "대댓글 목록")
        List<InquiryReplyResponse> childReplies,

        @Schema(description = "작성일시", example = "2024-10-10T16:00:00")
        LocalDateTime createDate,

        @Schema(description = "수정일시", example = "2024-10-10T16:30:00")
        LocalDateTime modifyDate
) {
    public static InquiryReplyResponse from(InquiryReply reply) {
        List<InquiryReplyResponse> childReplies = reply.getChildReplies()
                .stream()
                .map(InquiryReplyResponse::from)
                .toList();

        return new InquiryReplyResponse(
                reply.getId(),
                reply.getContent(),
                reply.getReplyType(),
                reply.getUser().getId(),
                reply.getUser().getName(),
                childReplies,
                reply.getCreateDate(),
                reply.getModifyDate()
        );
    }
}