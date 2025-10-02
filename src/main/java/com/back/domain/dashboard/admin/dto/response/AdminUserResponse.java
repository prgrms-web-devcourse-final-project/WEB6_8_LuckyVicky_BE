package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 사용자 목록 조회 응답 DTO
 *
 * 관리자가 전체 사용자를 조회하고 관리할 수 있는 정보를 포함
 * 2025.09.26 생성
 * 2025.10.01 Summary 제거 (화면에서 사용하지 않음)
 */
public record AdminUserResponse(
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
     * 사용자 정보
     */
    public record User(
            /** 사용자 ID */
            Long userId,
            /** 회원 ID */
            String memberId,
            /** 닉네임 */
            String nickname,
            /** 작가명 (작가인 경우만, 아니면 null) */
            String artistName,
            /** 수수료율 (%) - 작가인 경우만 */
            Integer commissionRate,
            /** 회원 등급 코드 */
            String grade,
            /** 계정 상태 */
            String accountStatus,
            /** 가입일 */
            LocalDate joinedAt
    ) {}
}
