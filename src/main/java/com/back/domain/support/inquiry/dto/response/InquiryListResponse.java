package com.back.domain.support.inquiry.dto.response;

import com.back.domain.support.inquiry.entity.Inquiry;
import com.back.domain.support.inquiry.entity.InquiryCategory;
import com.back.domain.support.inquiry.entity.InquiryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 목록 응답 DTO
 */
@Schema(description = "문의 목록 응답")
public record InquiryListResponse(

        @Schema(description = "문의 목록")
        List<InquiryItemDto> inquiries,

        @Schema(description = "현재 페이지 번호", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "10")
        int totalPages,

        @Schema(description = "전체 문의 개수", example = "95")
        long totalElements
) {
    public static InquiryListResponse from(Page<Inquiry> inquiryPage) {
        List<InquiryItemDto> inquiries = inquiryPage.getContent()
                .stream()
                .map(InquiryItemDto::from)
                .toList();

        return new InquiryListResponse(
                inquiries,
                inquiryPage.getNumber(),
                inquiryPage.getTotalPages(),
                inquiryPage.getTotalElements()
        );
    }
}
/**
 * 문의 목록 항목 DTO
 */
@Schema(description = "문의 항목")
record InquiryItemDto(

        @Schema(description = "글번호 (문의 ID)", example = "1")
        Long id,

        @Schema(description = "카테고리", example = "DELIVERY")
        InquiryCategory category,

        @Schema(description = "문의 제목", example = "배송 관련 문의드립니다")
        String title,

        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,

        @Schema(description = "작성일시", example = "2024-10-10T14:30:00")
        LocalDateTime createDate,

        @Schema(description = "조회수", example = "15")
                Long viewCount,

        @Schema(description = "문의 상태", example = "PENDING")
        InquiryStatus status,

        @Schema(description = "비밀문의 여부", example = "false")
        Boolean isSecret,

        @Schema(description = "댓글 개수", example = "3")
        int replyCount

) {
    public static InquiryItemDto from(Inquiry inquiry) {
        return new InquiryItemDto(
                inquiry.getId(),                  // 글번호
                inquiry.getCategory(),            // 카테고리
                inquiry.getTitle(),               // 제목
                inquiry.getUser().getName(),      // 작성자
                inquiry.getCreateDate(),          // 작성일
                inquiry.getViewCount(),           // 조회수
                inquiry.getStatus(),              // 상태 (추가)
                inquiry.getIsSecret(),            // 비밀문의 여부 (추가)
                inquiry.getReplies().size()
        );
    }
}