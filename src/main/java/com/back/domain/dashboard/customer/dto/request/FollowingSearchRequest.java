package com.back.domain.dashboard.customer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 팔로우한 작가 목록 검색 요청 DTO
 * 2025.09.25 생성
 * 2025.10.14 수정 - 팔로우 기능 실제 db로 연동
 */
public record FollowingSearchRequest(
        /** 페이지 번호 (0부터 시작) */
        @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 (1-100) */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size
) {
    /**
     * 기본값이 적용된 생성자
     */
    public FollowingSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 8;  // Figma 디자인: 한 페이지당 8개 (4x2 그리드)
    }
}
