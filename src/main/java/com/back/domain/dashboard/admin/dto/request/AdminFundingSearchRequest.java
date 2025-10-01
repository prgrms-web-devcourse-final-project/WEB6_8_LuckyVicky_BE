package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 펀딩 모니터링 목록 조회 요청 DTO
 * 2025.09.28 신규 생성
 */
public record AdminFundingSearchRequest(
        /** 페이지 번호 (0-based) */
        @Min(value = 0, message = "page는 0 이상이어야 합니다")
        int page,

        /** 페이지 크기 */
        @Min(value = 1, message = "size는 1 이상이어야 합니다")
        @Max(value = 100, message = "size는 100 이하여야 합니다")
        int size,

        /** 검색 키워드 (펀딩제목/작가명/작가ID) */
        String keyword,

        /** 펀딩 상태 */
        @Pattern(regexp = "^(OPEN|CLOSED|SUCCESS|FAILED|CANCELED)$",
                message = "status는 OPEN, CLOSED, SUCCESS, FAILED, CANCELED 중 하나여야 합니다")
        String status,

        /** 카테고리 ID */
        Long categoryId,

        /** 작가 ID */
        Long artistId,

        /** 최소 달성률 (0-100%) */
        @Min(value = 0, message = "minAchievement는 0 이상이어야 합니다")
        @Max(value = 100, message = "minAchievement는 100 이하여야 합니다")
        Integer minAchievement,

        /** 최대 달성률 (0-100%) */
        @Min(value = 0, message = "maxAchievement는 0 이상이어야 합니다")
        @Max(value = 100, message = "maxAchievement는 100 이하여야 합니다")
        Integer maxAchievement,

        /** 등록일 From (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "registeredFrom은 yyyy-MM-dd 형식이어야 합니다")
        String registeredFrom,

        /** 등록일 To (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "registeredTo는 yyyy-MM-dd 형식이어야 합니다")
        String registeredTo,

        /** 마감일 From (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "dueFrom은 yyyy-MM-dd 형식이어야 합니다")
        String dueFrom,

        /** 마감일 To (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "dueTo는 yyyy-MM-dd 형식이어야 합니다")
        String dueTo,

        /** 정렬 기준 */
        @Pattern(regexp = "^(title|artistId|artistName|achievementRate|status|remainingDays|endDate|currentAmount|supporterCount|registeredAt)$",
                message = "sort는 title, artistId, artistName, achievementRate, status, remainingDays, endDate, currentAmount, supporterCount, registeredAt 중 하나여야 합니다")
        String sort,

        /** 정렬 순서 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminFundingSearchRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sort == null) sort = "endDate";
        if (order == null) order = "ASC";
    }
}
