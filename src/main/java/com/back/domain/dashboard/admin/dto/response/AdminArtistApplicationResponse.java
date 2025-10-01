package com.back.domain.dashboard.admin.dto.response;

import java.util.List;

/**
 * 관리자 입점 신청 목록 응답 DTO
 * 2025.09.28 생성
 */
public record AdminArtistApplicationResponse(
        /** 요약 정보 */
        Summary summary,
        /** 신청 목록 */
        List<Application> content,
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
     * 입점 신청 요약 정보
     */
    public record Summary(
            /** 전체 신청 수 */
            int totalApplications,
            /** 심사 대기 수 */
            int pending,
            /** 승인 수 */
            int approved,
            /** 거절 수 */
            int rejected
    ) {}

    /**
     * 입점 신청 정보
     */
    public record Application(
            /** 신청 ID */
            long applicationId,
            /** 작가 정보 */
            Artist artist,
            /** 신청 상태 */
            String status,
            /** 신청일 */
            String submittedAt,
            /** 권한 정보 */
            Permissions permissions
    ) {}

    /**
     * 작가 정보
     */
    public record Artist(
            /** 회원 ID */
            String memberId,
            /** 작가명 */
            String name
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 승인 가능 여부 */
            boolean canApprove,
            /** 거절 가능 여부 */
            boolean canReject
    ) {}
}
