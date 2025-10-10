package com.back.domain.notice.dto.response;

import com.back.domain.notice.entity.Notice;
import com.back.domain.notice.entity.NoticeDocument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공지사항 상세 조회 응답 DTO
 */
@Schema(description = "공지사항 상세 조회 응답")
public record NoticeDetailResponse(

        @Schema(description = "공지사항 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "2024년 설날 연휴 배송 안내")
        String title,

        @Schema(description = "내용", example = "설날 연휴 기간에는 배송이 지연될 수 있습니다.")
        String content,

        @Schema(description = "중요 공지 여부", example = "true")
        Boolean isImportant,

        @Schema(description = "조회수", example = "150")
        Long viewCount,

        @Schema(description = "첨부파일 목록")
        List<NoticeDocumentDto> documents,

        @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
        LocalDateTime createDate,

        @Schema(description = "수정일시", example = "2024-01-16T14:20:00")
        LocalDateTime modifyDate
) {
    /**
     * Entity -> DTO 변환
     */
    public static NoticeDetailResponse from(Notice notice) {
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getIsImportant(),
                notice.getViewCount(),
                notice.getDocuments().stream()
                        .map(NoticeDocumentDto::from)
                        .collect(Collectors.toList()),
                notice.getCreateDate(),
                notice.getModifyDate()
        );
    }

    /**
     * 첨부파일 정보 DTO
     */
    @Schema(description = "공지사항 첨부파일 정보")
    public record NoticeDocumentDto(
            @Schema(description = "첨부파일 ID", example = "1")
            Long id,

            @Schema(description = "파일명", example = "공지사항_첨부파일.pdf")
            String fileName,

            @Schema(description = "파일 URL", example = "https://s3.amazonaws.com/bucket/notice/file.pdf")
            String fileUrl
    ) {
        public static NoticeDocumentDto from(NoticeDocument document) {
            return new NoticeDocumentDto(
                    document.getId(),
                    document.getFileName(),
                    document.getFileUrl()
            );
        }
    }
}