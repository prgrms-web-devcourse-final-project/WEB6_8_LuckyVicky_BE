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
 * 2025.09.25 수정
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
                () -> assertThat(settings.profile()).isNotNull(),
                () -> assertThat(settings.business()).isNotNull(),
                () -> assertThat(settings.payout()).isNotNull(),
                () -> assertThat(settings.permissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("프로필 정보 구조 및 SNS 검증")
    void validateProfileStructure_Success() {
        // Given
        ArtistSettingsResponse.Profile profile = createSampleProfile();

        // Then - 프로필 구조 검증
        assertAll(
                () -> assertThat(profile.nickname()).isNotBlank(),
                () -> assertThat(profile.bio()).isNotBlank(),
                () -> assertThat(profile.sns()).isNotEmpty(),
                () -> assertThat(profile.sns().get(0).platform()).isNotBlank(),
                () -> assertThat(profile.sns().get(0).handle()).startsWith("@"),
                () -> assertThat(profile.profileImageUrl()).startsWith("https://")
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
                () -> assertThat(verifiedBusiness.address()).isNotBlank(),
                () -> assertThat(verifiedBusiness.businessRegistrationNo()).matches("\\d{3}-\\d{2}-\\d{5}"),
                () -> assertThat(verifiedBusiness.telemarketingReportNo()).contains("서울"),
                () -> assertThat(verifiedBusiness.verified()).isTrue(),
                // 미인증 사업자 정보
                () -> assertThat(unverifiedBusiness.verified()).isFalse()
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
                () -> assertThat(verifiedPayout.bankCode()).matches("\\d{3}"),
                () -> assertThat(verifiedPayout.bankName()).isNotBlank(),
                () -> assertThat(verifiedPayout.accountHolder()).isNotBlank(),
                () -> assertThat(verifiedPayout.accountMasked()).contains("****"),
                () -> assertThat(verifiedPayout.status()).isEqualTo("VERIFIED"),
                // 대기중 계좌
                () -> assertThat(pendingPayout.status()).isEqualTo("PENDING"),
                // 거부된 계좌
                () -> assertThat(rejectedPayout.status()).isEqualTo("REJECTED")
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
                () -> assertThat(allAllowedPermissions.canEditProfile()).isTrue(),
                () -> assertThat(allAllowedPermissions.canEditBusiness()).isTrue(),
                () -> assertThat(allAllowedPermissions.canEditPayout()).isTrue(),
                // 제한된 권한
                () -> assertThat(restrictedPermissions.canEditProfile()).isTrue(),
                () -> assertThat(restrictedPermissions.canEditBusiness()).isFalse(),
                () -> assertThat(restrictedPermissions.canEditPayout()).isFalse()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistSettingsResponse response = new ArtistSettingsResponse(
                new ArtistSettingsResponse.Profile(
                        "작가명입니다",
                        "자신을 소개하는 글을 입력해주세요.",
                        Arrays.asList(new ArtistSettingsResponse.Sns("Instagram", "@mori_official")),
                        "https://cdn.example.com/u/5/profile.jpg"
                ),
                new ArtistSettingsResponse.Business(
                        "서울특별시 강남구 테헤란로 123 2층",
                        "123-45-67890",
                        "2025-서울강남-1234",
                        true
                ),
                new ArtistSettingsResponse.Payout(
                        "088",
                        "신한",
                        "홍길동",
                        "****-****-**3456",
                        "VERIFIED"
                ),
                new ArtistSettingsResponse.Permissions(true, true, true)
        );

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.profile().nickname()).isEqualTo("작가명입니다"),
                () -> assertThat(response.business().businessRegistrationNo()).isEqualTo("123-45-67890"),
                () -> assertThat(response.payout().bankName()).isEqualTo("신한"),
                () -> assertThat(response.permissions().canEditProfile()).isTrue()
        );
    }

    @Test
    @DisplayName("다중 SNS 플랫폼 처리")
    void handleMultipleSNSPlatforms_Success() {
        // Given
        List<ArtistSettingsResponse.Sns> multipleSns = Arrays.asList(
                new ArtistSettingsResponse.Sns("Instagram", "@artist_insta"),
                new ArtistSettingsResponse.Sns("Twitter", "@artist_twitter"),
                new ArtistSettingsResponse.Sns("YouTube", "@artist_youtube")
        );

        ArtistSettingsResponse.Profile profile = new ArtistSettingsResponse.Profile(
                "멀티 작가",
                "다양한 플랫폼에서 활동하는 작가입니다.",
                multipleSns,
                "https://cdn.example.com/profile.jpg"
        );

        // Then - 다중 SNS 검증
        assertAll(
                () -> assertThat(profile.sns()).hasSize(3),
                () -> assertThat(profile.sns().get(0).platform()).isEqualTo("Instagram"),
                () -> assertThat(profile.sns().get(1).platform()).isEqualTo("Twitter"),
                () -> assertThat(profile.sns().get(2).platform()).isEqualTo("YouTube")
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private ArtistSettingsResponse createSampleSettings() {
        return new ArtistSettingsResponse(
                createSampleProfile(),
                createVerifiedBusiness(),
                createPayoutWithStatus("VERIFIED"),
                createAllAllowedPermissions()
        );
    }

    private ArtistSettingsResponse.Profile createSampleProfile() {
        return new ArtistSettingsResponse.Profile(
                "테스트 작가",
                "테스트용 작가 소개입니다.",
                Arrays.asList(new ArtistSettingsResponse.Sns("Instagram", "@test_artist")),
                "https://cdn.example.com/test-profile.jpg"
        );
    }

    private ArtistSettingsResponse.Business createVerifiedBusiness() {
        return new ArtistSettingsResponse.Business(
                "서울특별시 강남구 테헤란로 123 2층",
                "123-45-67890",
                "2025-서울강남-1234",
                true
        );
    }

    private ArtistSettingsResponse.Business createUnverifiedBusiness() {
        return new ArtistSettingsResponse.Business(
                "서울특별시 강남구 테헤란로 456 3층",
                "987-65-43210",
                "2025-서울강남-5678",
                false
        );
    }

    private ArtistSettingsResponse.Payout createPayoutWithStatus(String status) {
        return new ArtistSettingsResponse.Payout(
                "088",
                "신한",
                "홍길동",
                "****-****-**3456",
                status
        );
    }

    private ArtistSettingsResponse.Permissions createAllAllowedPermissions() {
        return new ArtistSettingsResponse.Permissions(true, true, true);
    }

    private ArtistSettingsResponse.Permissions createRestrictedPermissions() {
        return new ArtistSettingsResponse.Permissions(true, false, false);
    }
}