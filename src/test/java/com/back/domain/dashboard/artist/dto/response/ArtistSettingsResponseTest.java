package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistSettingsResponse DTO 테스트
 * 작가 설정 구조와 권한 로직에 집중
 * 2025.09.24 생성
 */
@DisplayName("ArtistSettingsResponse DTO 테스트")
public class ArtistSettingsResponseTest {

    @Test
    @DisplayName("전체 설정 구조 생성 및 검증")
    void createCompleteSettings_Success() {
        // When
        ArtistSettingsResponse settings = createSampleSettings();

        // Then - 전체 구조 검증
        assertAll(
                () -> assertThat(settings).isNotNull(),
                () -> assertThat(settings.getProfile()).isNotNull(),
                () -> assertThat(settings.getBusiness()).isNotNull(),
                () -> assertThat(settings.getPayout()).isNotNull(),
                () -> assertThat(settings.getPermissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("프로필 정보 구조 및 SNS 검증")
    void validateProfileStructure_Success() {
        // Given
        ArtistSettingsResponse.Profile profile = createSampleProfile();

        // Then - 프로필 구조 검증
        assertAll(
                () -> assertThat(profile.getNickname()).isNotBlank(),
                () -> assertThat(profile.getBio()).isNotBlank(),
                () -> assertThat(profile.getSns()).isNotEmpty(),
                () -> assertThat(profile.getSns().get(0).getPlatform()).isNotBlank(),
                () -> assertThat(profile.getSns().get(0).getHandle()).startsWith("@"),
                () -> assertThat(profile.getProfileImageUrl()).startsWith("https://")
        );
    }

    @Test
    @DisplayName("사업자 정보 검증 및 인증 상태")
    void validateBusinessInfo_Success() {
        // Given
        ArtistSettingsResponse.Business verifiedBusiness = createVerifiedBusiness();
        ArtistSettingsResponse.Business unverifiedBusiness = createUnverifiedBusiness();

        // Then - 사업자 정보 검증
        assertAll(
                // 인증된 사업자 정보
                () -> assertThat(verifiedBusiness.getAddress()).isNotBlank(),
                () -> assertThat(verifiedBusiness.getBusinessRegistrationNo()).matches("\\d{3}-\\d{2}-\\d{5}"),
                () -> assertThat(verifiedBusiness.getTelemarketingReportNo()).contains("서울"),
                () -> assertThat(verifiedBusiness.isVerified()).isTrue(),
                // 미인증 사업자 정보
                () -> assertThat(unverifiedBusiness.isVerified()).isFalse()
        );
    }

    @Test
    @DisplayName("정산 계좌 정보 및 상태별 검증")
    void validatePayoutInfo_Success() {
        // Given
        ArtistSettingsResponse.Payout verifiedPayout = createPayoutWithStatus("VERIFIED");
        ArtistSettingsResponse.Payout pendingPayout = createPayoutWithStatus("PENDING");
        ArtistSettingsResponse.Payout rejectedPayout = createPayoutWithStatus("REJECTED");

        // Then - 정산 계좌 상태별 검증
        assertAll(
                // 인증된 계좌
                () -> assertThat(verifiedPayout.getBankCode()).matches("\\d{3}"),
                () -> assertThat(verifiedPayout.getBankName()).isNotBlank(),
                () -> assertThat(verifiedPayout.getAccountHolder()).isNotBlank(),
                () -> assertThat(verifiedPayout.getAccountMasked()).contains("****"),
                () -> assertThat(verifiedPayout.getStatus()).isEqualTo("VERIFIED"),
                // 대기중 계좌
                () -> assertThat(pendingPayout.getStatus()).isEqualTo("PENDING"),
                // 거부된 계좌
                () -> assertThat(rejectedPayout.getStatus()).isEqualTo("REJECTED")
        );
    }

    @Test
    @DisplayName("권한 정보 일관성 검증")
    void validatePermissions_Success() {
        // Given
        ArtistSettingsResponse.Permissions allAllowedPermissions = createAllAllowedPermissions();
        ArtistSettingsResponse.Permissions restrictedPermissions = createRestrictedPermissions();

        // Then - 권한 검증
        assertAll(
                // 모든 권한 허용
                () -> assertThat(allAllowedPermissions.isCanEditProfile()).isTrue(),
                () -> assertThat(allAllowedPermissions.isCanEditBusiness()).isTrue(),
                () -> assertThat(allAllowedPermissions.isCanEditPayout()).isTrue(),
                // 제한된 권한
                () -> assertThat(restrictedPermissions.isCanEditProfile()).isTrue(),
                () -> assertThat(restrictedPermissions.isCanEditBusiness()).isFalse(),
                () -> assertThat(restrictedPermissions.isCanEditPayout()).isFalse()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistSettingsResponse response = ArtistSettingsResponse.builder()
                .profile(ArtistSettingsResponse.Profile.builder()
                        .nickname("작가명입니다")
                        .bio("자신을 소개하는 글을 입력해주세요.")
                        .sns(Arrays.asList(
                                ArtistSettingsResponse.Sns.builder()
                                        .platform("Instagram")
                                        .handle("@mori_official")
                                        .build()
                        ))
                        .profileImageUrl("https://cdn.example.com/u/5/profile.jpg")
                        .build())
                .business(ArtistSettingsResponse.Business.builder()
                        .address("서울특별시 강남구 테헤란로 123 2층")
                        .businessRegistrationNo("123-45-67890")
                        .telemarketingReportNo("2025-서울강남-1234")
                        .verified(true)
                        .build())
                .payout(ArtistSettingsResponse.Payout.builder()
                        .bankCode("088")
                        .bankName("신한")
                        .accountHolder("홍길동")
                        .accountMasked("****-****-**3456")
                        .status("VERIFIED")
                        .build())
                .permissions(ArtistSettingsResponse.Permissions.builder()
                        .canEditProfile(true)
                        .canEditBusiness(true)
                        .canEditPayout(true)
                        .build())
                .build();

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.getProfile().getNickname()).isEqualTo("작가명입니다"),
                () -> assertThat(response.getBusiness().getBusinessRegistrationNo()).isEqualTo("123-45-67890"),
                () -> assertThat(response.getPayout().getBankName()).isEqualTo("신한"),
                () -> assertThat(response.getPermissions().isCanEditProfile()).isTrue()
        );
    }

    @Test
    @DisplayName("다중 SNS 플랫폼 처리")
    void handleMultipleSNSPlatforms_Success() {
        // Given
        List<ArtistSettingsResponse.Sns> multipleSns = Arrays.asList(
                ArtistSettingsResponse.Sns.builder()
                        .platform("Instagram")
                        .handle("@artist_insta")
                        .build(),
                ArtistSettingsResponse.Sns.builder()
                        .platform("Twitter")
                        .handle("@artist_twitter")
                        .build(),
                ArtistSettingsResponse.Sns.builder()
                        .platform("YouTube")
                        .handle("@artist_youtube")
                        .build()
        );

        ArtistSettingsResponse.Profile profile = ArtistSettingsResponse.Profile.builder()
                .nickname("멀티 작가")
                .bio("다양한 플랫폼에서 활동하는 작가입니다.")
                .sns(multipleSns)
                .profileImageUrl("https://cdn.example.com/profile.jpg")
                .build();

        // Then - 다중 SNS 검증
        assertAll(
                () -> assertThat(profile.getSns()).hasSize(3),
                () -> assertThat(profile.getSns().get(0).getPlatform()).isEqualTo("Instagram"),
                () -> assertThat(profile.getSns().get(1).getPlatform()).isEqualTo("Twitter"),
                () -> assertThat(profile.getSns().get(2).getPlatform()).isEqualTo("YouTube")
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private ArtistSettingsResponse createSampleSettings() {
        return ArtistSettingsResponse.builder()
                .profile(createSampleProfile())
                .business(createVerifiedBusiness())
                .payout(createPayoutWithStatus("VERIFIED"))
                .permissions(createAllAllowedPermissions())
                .build();
    }

    private ArtistSettingsResponse.Profile createSampleProfile() {
        return ArtistSettingsResponse.Profile.builder()
                .nickname("테스트 작가")
                .bio("테스트용 작가 소개입니다.")
                .sns(Arrays.asList(
                        ArtistSettingsResponse.Sns.builder()
                                .platform("Instagram")
                                .handle("@test_artist")
                                .build()
                ))
                .profileImageUrl("https://cdn.example.com/test-profile.jpg")
                .build();
    }

    private ArtistSettingsResponse.Business createVerifiedBusiness() {
        return ArtistSettingsResponse.Business.builder()
                .address("서울특별시 강남구 테헤란로 123 2층")
                .businessRegistrationNo("123-45-67890")
                .telemarketingReportNo("2025-서울강남-1234")
                .verified(true)
                .build();
    }

    private ArtistSettingsResponse.Business createUnverifiedBusiness() {
        return ArtistSettingsResponse.Business.builder()
                .address("서울특별시 강남구 테헤란로 456 3층")
                .businessRegistrationNo("987-65-43210")
                .telemarketingReportNo("2025-서울강남-5678")
                .verified(false)
                .build();
    }

    private ArtistSettingsResponse.Payout createPayoutWithStatus(String status) {
        return ArtistSettingsResponse.Payout.builder()
                .bankCode("088")
                .bankName("신한")
                .accountHolder("홍길동")
                .accountMasked("****-****-**3456")
                .status(status)
                .build();
    }

    private ArtistSettingsResponse.Permissions createAllAllowedPermissions() {
        return ArtistSettingsResponse.Permissions.builder()
                .canEditProfile(true)
                .canEditBusiness(true)
                .canEditPayout(true)
                .build();
    }

    private ArtistSettingsResponse.Permissions createRestrictedPermissions() {
        return ArtistSettingsResponse.Permissions.builder()
                .canEditProfile(true)
                .canEditBusiness(false)
                .canEditPayout(false)
                .build();
    }
}