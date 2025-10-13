package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 펀딩 승인 대기 목록 조회 요청 DTO
 * PENDING 상태 펀딩을 관리자가 승인(APPROVED)하기 위한 조회
 * 2025.10.13 신규 생성
 */
public record AdminFundingApprovalSearchRequest(
        /** 페이지 번호 (0-based) */
        @Min(value = 0, message = "page는 0 이상이어야 합니다")
        int page,

        /** 페이지 크기 */
        @Min(value = 1, message = "size는 1 이상이어야 합니다")
        @Max(value = 100, message = "size는 100 이하여야 합니다")
        int size,

        /** 검색 키워드 (펀딩제목/작가명) */
        String keyword,

        /** 작가 ID */
        Long artistId,

        /** 정렬 기준 */
        @Pattern(regexp = "^(artistId|artistName|title|registeredAt)$",
                message = "sort는 artistId, artistName, title, registeredAt 중 하나여야 합니다")
        String sort,

        /** 정렬 순서 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminFundingApprovalSearchRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sort == null) sort = "registeredAt";
        if (order == null) order = "DESC";
    }
}
