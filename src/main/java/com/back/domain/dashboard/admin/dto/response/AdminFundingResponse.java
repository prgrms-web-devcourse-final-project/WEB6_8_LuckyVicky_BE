package com.back.domain.dashboard.admin.dto.response;

import java.util.List;

/**
 * 관리자 펀딩 모니터링 목록 응답 DTO
 * 2025.10.01 Summary 제거 (화면에서 사용하지 않음)
 */
public record AdminFundingResponse(
        /** 펀딩 목록 */
        List<Funding> content,
        /** 현재 페이지 */
        int page,
        /** 페이지 크기 */
        int size,
        /** 전체 요소 수 */
        int totalElements,
        /** 전체 페이지 수 */
        int totalPages,
        /** 다음 페이지 존재 여부 */
        boolean hasNext,
        /** 이전 페이지 존재 여부 */
        boolean hasPrevious
) {

    /**
     * 펀딩 정보 (화면 표시 필드만, 평면 구조)
     */
    public record Funding(
            /** 펀딩 ID (기본키) */
            Long fundingId,
            /** 작가 ID */
            Long artistId,
            /** 작가명 */
            String artistName,
            /** 펀딩 제목 */
            String title,
            /** 달성률 (%) */
            int achievementRate,
            /** 펀딩 상태 */
            String status,
            /** 마감일 */
            String endDate
    ) {}
}
