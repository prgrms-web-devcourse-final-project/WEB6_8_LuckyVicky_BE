package com.back.domain.faq.dto.response;

import com.back.domain.faq.entity.Faq;
import com.back.domain.faq.entity.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "FAQ 목록 조회 응답")
public record FaqListResponse(

        @Schema(description = "FAQ 목록")
        List<FaqItemDto> faqs,

        @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
        int currentPage,

        @Schema(description = "페이지 크기", example = "10")
        int pageSize,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "전체 FAQ 수", example = "47")
        long totalElements
) {
    /**
     * Page<Faq> -> FaqListResponse 변환
     */
    public static FaqListResponse from(Page<Faq> faqPage) {
        List<FaqItemDto> items = faqPage.getContent().stream()
                .map(FaqItemDto::from)
                .collect(Collectors.toList());

        return new FaqListResponse(
                items,
                faqPage.getNumber() + 1, // 1페이지부터 시작하기 위한 연산
                faqPage.getSize(),
                faqPage.getTotalPages(),
                faqPage.getTotalElements()
        );
    }

    /**
     * FAQ 목록 항목 DTO
     */
    @Schema(description = "FAQ 항목")
    public record FaqItemDto(
            @Schema(description = "FAQ ID", example = "1")
            Long id,

            @Schema(description = "질문", example = "회원가입은 어떻게 하나요?")
            String question,

            @Schema(description = "카테고리", example = "ACCOUNT")
            FaqCategory category,

            @Schema(description = "카테고리 표시명", example = "회원/가입")
            String categoryDisplayName,

            @Schema(description = "조회수", example = "150")
            Long viewCount,

            @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
            LocalDateTime createDate
    ) {
        /**
         * Entity -> DTO 변환
         */
        public static FaqItemDto from(Faq faq) {
            return new FaqItemDto(
                    faq.getId(),
                    faq.getQuestion(),
                    faq.getCategory(),
                    faq.getCategory().getDisplayName(),
                    faq.getViewCount(),
                    faq.getCreateDate()
            );
        }
    }
}