package com.back.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 리뷰 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewListResponseDto {

    private List<ReviewResponseDto> reviews; // 리뷰 목록
    private Integer totalCount; // 전체 리뷰 수
    private Integer photoReviewCount; // 포토리뷰 수
    private Integer generalReviewCount; // 일반리뷰 수
    private Double averageRating; // 평균 평점
    private Boolean hasNext; // 다음 페이지 존재 여부
    private Integer currentPage; // 현재 페이지
    private Integer totalPages; // 전체 페이지 수
}
