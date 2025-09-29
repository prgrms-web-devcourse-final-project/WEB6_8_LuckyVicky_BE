package com.back.domain.dashboard.admin.dto.response;

import java.util.List;

/**
 * 관리자 펀딩 모니터링 목록 응답 DTO
 * 2025.09.28 신규 생성
 */
public record AdminFundingResponse(
        /** 요약 정보 */
        Summary summary,
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
     * 펀딩 요약 정보
     */
    public record Summary(
            /** 전체 펀딩 수 */
            int totalFundings,
            /** 진행중 펀딩 수 */
            int activeFundings,
            /** 일시정지 펀딩 수 */
            int pausedFundings,
            /** 완료 펀딩 수 */
            int completedFundings,
            /** 취소 펀딩 수 */
            int cancelledFundings
    ) {}

    /**
     * 펀딩 정보
     */
    public record Funding(
            /** 펀딩 ID */
            long fundingId,
            /** 펀딩 제목 */
            String title,
            /** 작가 정보 */
            Artist artist,
            /** 카테고리 정보 */
            Category category,
            /** 펀딩 상태 */
            String status,
            /** 목표 금액 */
            long targetAmount,
            /** 현재 금액 */
            long currentAmount,
            /** 달성률 (%) */
            int achievementRate,
            /** 후원자 수 */
            int supporterCount,
            /** 마감일 */
            String endDate,
            /** 등록일 */
            String registeredAt,
            /** 남은 일수 */
            int remainingDays,
            /** 메인 이미지 */
            String mainImage,
            /** 권한 정보 */
            Permissions permissions,
            /** 플래그 정보 */
            Flags flags
    ) {}

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            long id,
            /** 회원 ID */
            String memberId,
            /** 작가명 */
            String name
    ) {}

    /**
     * 카테고리 정보
     */
    public record Category(
            /** 카테고리 ID */
            long id,
            /** 카테고리명 */
            String name
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /** 일시정지 가능 여부 */
            boolean canPause,
            /** 판매 전환 승인 가능 여부 */
            boolean canApproveSale
    ) {}

    /**
     * 플래그 정보
     */
    public record Flags(
            /** 목표 달성 여부 */
            boolean goalAchieved,
            /** 마감 임박 여부 */
            boolean dueSoon
    ) {}
}
