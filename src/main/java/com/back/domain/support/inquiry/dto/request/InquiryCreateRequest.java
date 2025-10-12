package com.back.domain.support.inquiry.dto.request;

import com.back.domain.support.inquiry.entity.InquiryCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문의 작성 요청 DTO
 */
@Schema(description = "문의 작성 요청")
public record InquiryCreateRequest(

        @Schema(description = "문의 카테고리", example = "DELIVERY")
        @NotNull(message = "카테고리는 필수입니다")
        InquiryCategory category,

        @Schema(description = "문의 제목", example = "배송 관련 문의드립니다")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
        String title,

        @Schema(description = "문의 내용", example = "주문한 상품의 배송이 지연되고 있습니다.")
        @NotBlank(message = "내용은 필수입니다")
        String content,

        @Schema(description = "비밀문의 여부", example = "true", defaultValue = "false")
        Boolean isSecret,  // 비밀문의 여부 (null이면 false)

        @Schema(description = "첨부파일 (최대 3개)")
        List<MultipartFile> files  // 첨부파일 (선택, 최대 3개)
) {
}