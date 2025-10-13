package com.back.domain.support.inquiry.dto.response;

import com.back.domain.support.inquiry.entity.Inquiry;
import com.back.domain.support.inquiry.entity.InquiryCategory;
import com.back.domain.support.inquiry.entity.InquiryDocument;
import com.back.domain.support.inquiry.entity.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 상세 응답 DTO
 */
@Schema(description = "문의 상세 응답")
public record InquiryDetailResponse(

        @Schema(description = "문의 ID", example = "1")
        Long id,

        @Schema(description = "카테고리", example = "DELIVERY")
        InquiryCategory category,

        @Schema(description = "문의 제목", example = "배송 관련 문의드립니다")
        String title,

        @Schema(description = "문의 내용", example = "주문한 상품의 배송이 지연되고 있습니다.")
        String content,

        @Schema(description = "문의 상태", example = "ANSWERED")
        InquiryStatus status,

        @Schema(description = "비밀문의 여부", example = "false")
        Boolean isSecret,

        @Schema(description = "작성자 ID", example = "123")
        Long authorId,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "조회수", example = "15")
        Long viewCount,

        @Schema(description = "첨부파일 목록")
        List<InquiryDocumentDto> documents,

        @Schema(description = "댓글 목록")
        List<InquiryReplyResponse> replies,

        @Schema(description = "작성일시", example = "2024-10-10T14:30:00")
        LocalDateTime createDate,

        @Schema(description = "수정일시", example = "2024-10-10T15:20:00")
        LocalDateTime modifyDate
) {
    public static InquiryDetailResponse from(Inquiry inquiry) {
        List<InquiryDocumentDto> documents = inquiry.getDocuments()
                .stream()
                .map(InquiryDocumentDto::from)
                .toList();

        List<InquiryReplyResponse> replies = inquiry.getReplies()
                .stream()
                .filter(reply -> reply.getParentReply() == null)
                .map(InquiryReplyResponse::from)
                .toList();

        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getCategory(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getIsSecret(),
                inquiry.getUser().getId(),
                inquiry.getUser().getName(),
                inquiry.getViewCount(),
                documents,
                replies,
                inquiry.getCreateDate(),
                inquiry.getModifyDate()
        );
    }
}

/**
 * 첨부파일 DTO
 */
@Schema(description = "첨부파일 정보")
record InquiryDocumentDto(

        @Schema(description = "파일 ID", example = "1")
        Long id,

        @Schema(description = "파일명", example = "receipt.pdf")
        String fileName,

        @Schema(description = "파일 URL", example = "https://s3.amazonaws.com/...")
        String fileUrl
) {
    public static InquiryDocumentDto from(InquiryDocument document) {
        return new InquiryDocumentDto(
                document.getId(),
                document.getFileName(),
                document.getFileUrl()
        );
    }
}
