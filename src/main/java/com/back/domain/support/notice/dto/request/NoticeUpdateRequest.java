package com.back.domain.support.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공지사항 수정 요청 DTO
 */
@Schema(description = "공지사항 수정 요청")
public record NoticeUpdateRequest(

        @Schema(description = "공지사항 제목", example = "2024년 설날 연휴 배송 안내 (수정)")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
        String title,

        @Schema(description = "공지사항 내용", example = "수정된 내용입니다.")
        @NotBlank(message = "내용은 필수입니다")
        String content,

        @Schema(description = "중요 공지 여부", example = "false")
        Boolean isImportant,

        @Schema(description = "새로 추가할 첨부파일 목록")
        List<MultipartFile> files,

        @Schema(description = "삭제할 기존 첨부파일 ID 목록", example = "[1, 2, 3]")
        List<Long> deleteFileIds
) {
    /**
     * 기본값 설정 생성자
     */
    public NoticeUpdateRequest {
        if (isImportant == null) {
            isImportant = false;
        }
    }
}
