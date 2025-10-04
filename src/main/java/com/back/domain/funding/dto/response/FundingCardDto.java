package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.Funding;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "펀딩 목록 응답")
public record FundingCardDto(
        Long id,              // 펀딩 ID
        String title,         // 펀딩 제목
        String imageUrl,      // 썸네일 이미지
//        String categoryName,  // 카테고리 이름
        String authorName,    // 작성자명
        long targetAmount,    // 목표 금액
        long currentAmount,   // 현재 금액
        double progress,      // 달성률
        int remainingDays     // 남은 일수 (D-10 표시용)
) {
    public FundingCardDto(Funding funding, long currentAmount, double progress, int remainingDays) {
        this(
                funding.getId(),
                funding.getTitle(),
                funding.getImageUrl(),
//                funding.getCategory().getName(),
                funding.getUser().getName(),
                funding.getTargetAmount(),
                currentAmount,
                progress,
                remainingDays
        );
    }
}