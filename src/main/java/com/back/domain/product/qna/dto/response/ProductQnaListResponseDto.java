package com.back.domain.product.qna.dto.response;

import com.back.domain.product.qna.entity.ProductQna;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 상품 Q&A 목록 조회 응답 DTO
 */
public record ProductQnaListResponseDto(
        @Schema(description = "현재 페이지 번호", example = "1")
        int currentPage,
        @Schema(description = "총 페이지 수", example = "5")
        int totalPages,
        @Schema(description = "한 페이지당 항목 수", example = "10")
        int pageSize,
        @Schema(description = "총 항목 수", example = "45")
        long totalElements,
        @Schema(description = "상품 Q&A 목록")
        List<ProductQnaResponseDto> qnaList
) {
    public static ProductQnaListResponseDto fromPage(Page<ProductQna> page, List<ProductQnaResponseDto> qnaList) {
        return new ProductQnaListResponseDto(
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                qnaList
        );
    }
}
