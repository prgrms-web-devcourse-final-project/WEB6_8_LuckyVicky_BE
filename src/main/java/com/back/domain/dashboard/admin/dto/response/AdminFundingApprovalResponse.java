package com.back.domain.dashboard.admin.dto.response;

import java.util.List;

/**
 * 관리자 펀딩 승인 대기 목록 응답 DTO
 * PENDING 상태 펀딩 목록
 * 2025.10.13 신규 생성
 */
public record AdminFundingApprovalResponse(
        /** 펀딩 목록 */
        List<FundingApproval> content,
        /** 현재 페이지 */
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
     * 펀딩 승인 대기 정보
     */
    public record FundingApproval(
            /** 펀딩 ID */
            Long fundingId,
            /** 펀딩 제목 */
            String title,
            /** 작가 정보 */
            Artist artist,
            /** 목표 금액 */
            long targetAmount,
            /** 펀딩 시작일 */
            String startDate,
            /** 펀딩 종료일 */
            String endDate,
            /** 펀딩 신청일 */
            String registeredAt,
            /** 메인 이미지 */
            String mainImage
    ) {}

    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            Long id,
            /** 작가명 */
            String name,
            /** 이메일 */
            String email
    ) {}
}
