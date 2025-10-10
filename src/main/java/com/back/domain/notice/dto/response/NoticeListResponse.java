package com.back.domain.notice.dto.response;

import com.back.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 공지사항 목록 조회 응답 DTO
 */
@Schema(description = "공지사항 목록 조회 응답")
public record NoticeListResponse(

        @Schema(description = "공지사항 목록")
        List<NoticeItemDto> notices,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int currentPage,

        @Schema(description = "페이지 크기", example = "10")
        int pageSize,

        @Schema(description = "전체 공지사항 개수", example = "50")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean isLast
) {
    /**
     * Page<Notice> -> NoticeListResponse 변환
     */
    public static NoticeListResponse from(Page<Notice> noticePage) {
        List<NoticeItemDto> noticeItems = noticePage.getContent().stream()
                .map(NoticeItemDto::from)
                .collect(Collectors.toList());

        return new NoticeListResponse(
                noticeItems,
                noticePage.getNumber(),
                noticePage.getSize(),
                noticePage.getTotalElements(),
                noticePage.getTotalPages(),
                noticePage.isLast()
        );
    }

    /**
     * 공지사항 목록 아이템 DTO
     */
    @Schema(description = "공지사항 목록 아이템")
    public record NoticeItemDto(
            @Schema(description = "공지사항 ID", example = "1")
            Long id,

            @Schema(description = "제목", example = "2024년 설날 연휴 배송 안내")
            String title,

            @Schema(description = "중요 공지 여부", example = "true")
            Boolean isImportant,

            @Schema(description = "조회수", example = "150")
            Long viewCount,

            @Schema(description = "첨부파일 개수", example = "2")
            int documentCount,

            @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
            LocalDateTime createDate
    ) {
        public static NoticeItemDto from(Notice notice) {
            return new NoticeItemDto(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getIsImportant(),
                    notice.getViewCount(),
                    notice.getDocuments().size(),
                    notice.getCreateDate()
            );
        }
    }
}