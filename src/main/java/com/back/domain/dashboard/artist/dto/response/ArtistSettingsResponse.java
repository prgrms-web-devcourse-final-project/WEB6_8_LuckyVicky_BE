package com.back.domain.dashboard.artist.dto.response;

import java.util.List;

/**
 * 작가 설정 정보 응답 DTO
 * <p>
 * 작가의 프로필, 사업자 정보, 정산 계좌 정보를 포함
 * 2025.09.25 수정
 */
public record ArtistSettingsResponse(
        /**
         * 프로필 정보
         */
        Profile profile,
        /**
         * 사업자 정보
         */
        Business business,
        /**
         * 정산 계좌 정보
         */
        Payout payout,
        /**
         * 권한 정보
         */
        Permissions permissions
) {

    /**
     * 프로필 정보
     */
    public record Profile(
            /**
             * 닉네임
             */
            String nickname,
            /**
             * 소개글
             */
            String bio,
            /**
             * SNS 정보
             */
            List<Sns> sns,
            /**
             * 프로필 이미지 URL
             */
            String profileImageUrl
    ) {}

    /**
     * SNS 정보
     */
    public record Sns(
            /**
             * SNS 플랫폼
             */
            String platform,
            /**
             * SNS 핸들
             */
            String handle
    ) {}

    /**
     * 사업자 정보
     */
    public record Business(
            /**
             * 사업자 주소
             */
            String address,
            /**
             * 사업자등록번호
             */
            String businessRegistrationNo,
            /**
             * 통신판매업신고번호
             */
            String telemarketingReportNo,
            /**
             * 인증 상태
             */
            boolean verified
    ) {}

    /**
     * 정산 계좌 정보
     */
    public record Payout(
            /**
             * 은행 코드
             */
            String bankCode,
            /**
             * 은행명
             */
            String bankName,
            /**
             * 예금주명
             */
            String accountHolder,
            /**
             * 마스킹된 계좌번호
             */
            String accountMasked,
            /**
             * 인증 상태
             */
            String status // PENDING | VERIFIED | REJECTED
    ) {}

    /**
     * 권한 정보
     */
    public record Permissions(
            /**
             * 프로필 편집 권한
             */
            boolean canEditProfile,
            /**
             * 사업자 정보 편집 권한
             */
            boolean canEditBusiness,
            /**
             * 정산 계좌 편집 권한
             */
            boolean canEditPayout
    ) {}
}
