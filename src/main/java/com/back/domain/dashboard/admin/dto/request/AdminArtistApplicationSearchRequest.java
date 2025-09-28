package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 입점 신청 목록 조회 요청 DTO
 * 2025.09.28 생성
 */
public record AdminArtistApplicationSearchRequest(
        /** 페이지 번호 (0-based) */
        @Min(value = 0, message = "page는 0 이상이어야 합니다")
        int page,

        /** 페이지 크기 */
        @Min(value = 1, message = "size는 1 이상이어야 합니다")
        @Max(value = 100, message = "size는 100 이하여야 합니다")
        int size,

        /** 검색 키워드 (작가ID/작가명/이메일) */
        String keyword,

        /** 신청 상태 */
        @Pattern(regexp = "^(PENDING|APPROVED|REJECTED)$",
                message = "status는 PENDING, APPROVED, REJECTED 중 하나여야 합니다")
        String status,

        /** 신청일 From (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "submittedFrom은 yyyy-MM-dd 형식이어야 합니다")
        String submittedFrom,

        /** 신청일 To (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "submittedTo는 yyyy-MM-dd 형식이어야 합니다")
        String submittedTo,

        /** 정렬 기준 */
        @Pattern(regexp = "^(artistId|artistName|submittedAt|status)$",
                message = "sort는 artistId, artistName, submittedAt, status 중 하나여야 합니다")
        String sort,

        /** 정렬 순서 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminArtistApplicationSearchRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sort == null) sort = "submittedAt";
        if (order == null) order = "DESC";
    }
}
