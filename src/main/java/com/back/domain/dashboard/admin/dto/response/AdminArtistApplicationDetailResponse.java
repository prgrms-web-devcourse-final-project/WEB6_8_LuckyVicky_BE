package com.back.domain.dashboard.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 입점 신청 상세 조회 응답 DTO
 * 2025.09.28 신규 생성
 */
public record AdminArtistApplicationDetailResponse(
        /** 신청 ID */
        long applicationId,
        /** 신청 상태 */
        String status,
        /** 신청일시 */
        LocalDateTime submittedAt,
        /** 작가 정보 */
        Artist artist,
        /** 연락처 정보 */
        Contact contact,
        /** 사업자 정보 */
        Business business,
        /** 프로필 정보 */
        Profile profile,
        /** 검토 정보 */
        Review review,
        /** 결정 정보 */
        Decision decision,
        /** 권한 정보 */
        Permissions permissions
) {

    /**
     * 작가 정보
     */
    public record Artist(
            /** 사용자 ID */
            long userId,
            /** 회원 ID */
            String memberId,
            /** 작가명 */
            String name,
            /** 아바타 URL */
            String avatarUrl
    ) {}

    /**
     * 연락처 정보
     */
    public record Contact(
            /** 이메일 */
            String email,
            /** 전화번호 */
            String phone
    ) {}

    /**
     * 사업자 정보
     */
    public record Business(
            /** 사업자등록번호 */
            String registrationNo,
            /** 통신판매업 신고번호 */
            String telemarketingReportNo,
            /** 주소 */
            String address
    ) {}

    /**
     * 프로필 정보
     */
    public record Profile(
            /** 주요 카테고리 */
            List<String> mainCategories,
            /** SNS 정보 */
            List<SnsInfo> sns,
            /** 포트폴리오 */
            List<PortfolioFile> portfolio
    ) {}

    /**
     * SNS 정보
     */
    public record SnsInfo(
            /** 플랫폼 */
            String platform,
            /** 핸들 */
            String handle
    ) {}

    /**
     * 포트폴리오 파일
     */
    public record PortfolioFile(
            /** 파일 ID */
            String fileId,
            /** 파일명 */
            String fileName,
            /** URL */
            String url
    ) {}

    /**
     * 검토 정보
     */
    public record Review(
            /** 신청자 메모 */
            String notesFromApplicant,
            /** 검증 결과 */
            Verifications verifications
    ) {}

    /**
     * 검증 결과
     */
    public record Verifications(
            /** 사업자 정보 검증 */
            boolean businessVerified,
            /** 연락처 검증 */
            boolean contactVerified
    ) {}

    /**
     * 결정 정보
     */
    public record Decision(
            /** 결정 상태 */
            String status,
            /** 결정 사유 */
            String reason,
            /** 결정 일시 */
            LocalDateTime decidedAt,
            /** 결정자 */
            String decidedBy
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
