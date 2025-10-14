package com.back.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 리뷰 통계 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatsResponseDto {

    private Integer totalReviewCount; // 전체 리뷰 수
    private Integer photoReviewCount; // 포토리뷰 수
    private Integer generalReviewCount; // 일반리뷰 수
    private Double averageRating; // 평균 평점
    private Map<Integer, Integer> ratingDistribution; // 평점별 분포 (1점: 개수, 2점: 개수, ...)
}
