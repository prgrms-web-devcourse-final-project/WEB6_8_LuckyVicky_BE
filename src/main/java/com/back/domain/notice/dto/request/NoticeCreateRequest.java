package com.back.domain.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공지사항 생성 요청 DTO
 */
@Schema(description = "공지사항 생성 요청")
public record NoticeCreateRequest(

        @Schema(description = "공지사항 제목", example = "2024년 설날 연휴 배송 안내")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
        String title,

        @Schema(description = "공지사항 내용", example = "설날 연휴 기간에는 배송이 지연될 수 있습니다.")
        @NotBlank(message = "내용은 필수입니다")
        String content,

        @Schema(description = "중요 공지 여부", example = "true")
        Boolean isImportant,

        @Schema(description = "첨부파일 목록 (최대 5개)")
        List<MultipartFile> files
) {
    /**
     * 기본값 설정 생성자
     */
    public NoticeCreateRequest {
        if (isImportant == null) {
            isImportant = false;
        }
    }
}