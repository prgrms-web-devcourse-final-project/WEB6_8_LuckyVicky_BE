package com.back.domain.dashboard.admin.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 관리자 사용자 목록 조회 요청 DTO
 * 2025.09.26 신규 생성
 */
public record AdminUserSearchRequest(
        /** 페이지 번호 (0-based) */
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
        Integer page,

        /** 페이지 크기 */
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
        Integer size,

        /** 통합 검색 키워드 (회원ID/닉네임/작가명/이메일) */
        String keyword,

        /** 회원 역할 */
        @Pattern(regexp = "^(USER|ARTIST|ADMIN)$",
                message = "role은 USER, ARTIST, ADMIN 중 하나여야 합니다")
        String role,

        /** 계정 상태 */
        @Pattern(regexp = "^(ACTIVE|SUSPENDED|BLACKLISTED)$",
                message = "accountStatus는 ACTIVE, SUSPENDED, BLACKLISTED 중 하나여야 합니다")
        String accountStatus,

        /** 회원 등급 */
        @Pattern(regexp = "^(SEED|BRONZE|SILVER|GOLD|PLATINUM)$",
                message = "grade는 SEED, BRONZE, SILVER, GOLD, PLATINUM 중 하나여야 합니다")
        String grade,

        /** 가입일 시작 (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "joinedStartDate는 yyyy-MM-dd 형식이어야 합니다")
        String joinedStartDate,

        /** 가입일 종료 (yyyy-MM-dd) */
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "joinedEndDate는 yyyy-MM-dd 형식이어야 합니다")
        String joinedEndDate,

        /** 작가 ID 필터 */
        Long artistId,

        /** 정렬 기준 */
        @Pattern(regexp = "^(memberId|nickname|artistName|grade|accountStatus|joinedAt)$",
                message = "sort는 memberId, nickname, artistName, grade, accountStatus, joinedAt 중 하나여야 합니다")
        String sort,

        /** 정렬 순서 */
        @Pattern(regexp = "^(ASC|DESC)$",
                message = "order는 ASC 또는 DESC여야 합니다")
        String order
) {
    /**
     * 기본값이 적용된 생성자
     */
    public AdminUserSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sort == null) sort = "joinedAt";
        if (order == null) order = "DESC";
    }
}
