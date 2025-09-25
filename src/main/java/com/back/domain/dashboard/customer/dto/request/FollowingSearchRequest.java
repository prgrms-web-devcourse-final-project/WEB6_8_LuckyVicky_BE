package com.back.domain.dashboard.customer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 팔로우한 작가 목록 검색 요청 DTO
 * 2025.09.25 생성
 */
public record FollowingSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 검색 키워드 (작가ID/작가명) */
        String keyword,

        /** 관계 상태 (FOLLOWING 고정) */
        @Pattern(regexp = "^FOLLOWING$", 
                 message = "status는 FOLLOWING이어야 합니다")
        String status,

        /** 정렬 기준 */
        @Pattern(regexp = "^(followedAt|artistName|followerCount|lastPublishedAt)$", 
                 message = "sort는 followedAt, artistName, followerCount, lastPublishedAt 중 하나여야 합니다")
        String sort,

        /** 정렬 방향 */
        @Pattern(regexp = "^(ASC|DESC)$", 
                 message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public FollowingSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (status == null) status = "FOLLOWING";
        if (sort == null) sort = "followedAt";
        if (order == null) order = "DESC";
    }
}
