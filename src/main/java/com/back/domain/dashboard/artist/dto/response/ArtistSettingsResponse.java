package com.back.domain.dashboard.artist.dto.response;

import lombok.*;

import java.util.List;

/**
 * 작가 설정 정보 응답 DTO
 * <p>
 * 작가의 프로필, 사업자 정보, 정산 계좌 정보를 포함
 * 2025.09.24 생성
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistSettingsResponse {

    /**
     * 프로필 정보
     */
    private Profile profile;
    /**
     * 사업자 정보
     */
    private Business business;
    /**
     * 정산 계좌 정보
     */
    private Payout payout;
    /**
     * 권한 정보
     */
    private Permissions permissions;

    /**
     * 프로필 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        /**
         * 닉네임
         */
        private String nickname;
        /**
         * 소개글
         */
        private String bio;
        /**
         * SNS 정보
         */
        private List<Sns> sns;
        /**
         * 프로필 이미지 URL
         */
        private String profileImageUrl;
    }

    /**
     * SNS 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sns {
        /**
         * SNS 플랫폼
         */
        private String platform;
        /**
         * SNS 핸들
         */
        private String handle;
    }

    /**
     * 사업자 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Business {
        /**
         * 사업자 주소
         */
        private String address;
        /**
         * 사업자등록번호
         */
        private String businessRegistrationNo;
        /**
         * 통신판매업신고번호
         */
        private String telemarketingReportNo;
        /**
         * 인증 상태
         */
        private boolean verified;
    }

    /**
     * 정산 계좌 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payout {
        /**
         * 은행 코드
         */
        private String bankCode;
        /**
         * 은행명
         */
        private String bankName;
        /**
         * 예금주명
         */
        private String accountHolder;
        /**
         * 마스킹된 계좌번호
         */
        private String accountMasked;
        /**
         * 인증 상태
         */
        private String status; // PENDING | VERIFIED | REJECTED
    }

    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permissions {
        /**
         * 프로필 편집 권한
         */
        private boolean canEditProfile;
        /**
         * 사업자 정보 편집 권한
         */
        private boolean canEditBusiness;
        /**
         * 정산 계좌 편집 권한
         */
        private boolean canEditPayout;
    }
}