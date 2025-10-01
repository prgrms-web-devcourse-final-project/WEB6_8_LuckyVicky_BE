package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 사용자 목록 조회 응답 DTO
 *
 * 관리자가 전체 사용자를 조회하고 관리할 수 있는 정보를 포함
 * 2025.09.26 생성
 */
public record AdminUserResponse(
        /** 사용자 요약 정보 */
        Summary summary,
        /** 사용자 목록 */
        List<User> content,
        /** 페이지 번호 */
        int page,
        /** 페이지 크기 */
        int size,
        /** 전체 요소 수 */
        long totalElements,
        /** 전체 페이지 수 */
        int totalPages,
        /** 다음 페이지 존재 여부 */
        boolean hasNext,
        /** 이전 페이지 존재 여부 */
        boolean hasPrevious
) {

    /**
     * 사용자 요약 정보
     */
    public record Summary(
            /** 전체 사용자 수 */
            int totalUsers,
            /** 활동중 사용자 수 */
            int activeUsers,
            /** 활동정지 사용자 수 */
            int suspendedUsers,
            /** 블랙리스트 사용자 수 */
            int blacklistedUsers,
            /** 작가 사용자 수 */
            int artistUsers
    ) {}

    /**
     * 사용자 정보
     */
    public record User(
            /** 사용자 ID */
            long userId,
            /** 회원 ID */
            String memberId,
            /** 닉네임 */
            String nickname,
            /** 역할 */
            String role,
            /** 작가 정보 */
            Artist artist,
            /** 회원 등급 */
            Grade grade,
            /** 계정 상태 */
            String accountStatus,
            /** 가입일 */
            LocalDate joinedAt,
            /** 마지막 활동 시간 */
            LocalDateTime lastActiveAt,
            /** 권한 정보 */
            Permissions permissions
    ) {}

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID (일반 회원이면 null) */
            Long id,
            /** 작가명 (일반 회원이면 null) */
            String name
    ) {}

    /**
     * 회원 등급 정보
     */
    public record Grade(
            /** 등급 코드 */
            String code,
            /** 등급 라벨 */
            String label
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 블랙리스트 추가 권한 */
            boolean canBlacklist,
            /** 블랙리스트 해제 권한 */
            boolean canUnblacklist
    ) {}
}
