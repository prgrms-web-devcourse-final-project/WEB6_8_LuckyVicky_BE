package com.back.domain.support.inquiry.dto.request;

import com.back.domain.support.inquiry.entity.InquiryCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문의 수정 요청 DTO
 */
@Schema(description = "문의 수정 요청")
public record InquiryUpdateRequest(

        @Schema(description = "문의 카테고리", example = "PAYMENT")
        InquiryCategory category,

        @Schema(description = "문의 제목", example = "배송 관련 문의드립니다 (수정)")
        @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
        String title,

        @Schema(description = "문의 내용", example = "배송 현황을 확인하고 싶습니다.")
        String content,

        @Schema(description = "비밀문의 여부", example = "false")
        Boolean isSecret,

        @Schema(description = "삭제할 첨부파일 ID 목록", example = "[1, 2, 3]")
        List<Long> deleteFileIds,

        @Schema(description = "새로 추가할 첨부파일 (최대 3개)")
        List<MultipartFile> files
) {
}