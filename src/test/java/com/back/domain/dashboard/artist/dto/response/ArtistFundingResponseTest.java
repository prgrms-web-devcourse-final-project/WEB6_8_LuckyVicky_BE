package com.back.domain.dashboard.artist.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * ArtistFundingResponse DTO 테스트
 * 펀딩 목록과 비즈니스 로직에 집중
 * 2025.09.30 펀딩 실제 DB 연동에 맞춰 테스트 수정
 */
@DisplayName("ArtistFundingResponse DTO 테스트")
public class ArtistFundingResponseTest {

    @Test
    @DisplayName("펀딩 정보 구조 생성 및 검증")
    void createFunding_Success() {
        // When
        ArtistFundingResponse.Funding funding = createSampleFunding();

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(funding).isNotNull(),
                () -> assertThat(funding.fundingId()).isNotNull(),
                () -> assertThat(funding.title()).isNotBlank(),
                () -> assertThat(funding.status()).isNotBlank(),
                () -> assertThat(funding.statusText()).isNotBlank(),
                () -> assertThat(funding.targetAmount()).isNotNegative(),
                () -> assertThat(funding.currentAmount()).isNotNegative(),
                () -> assertThat(funding.achievementRate()).isNotNegative(),
                () -> assertThat(funding.participantCount()).isNotNegative(),
                () -> assertThat(funding.permissions()).isNotNull(),
                () -> assertThat(funding.flags()).isNotNull()
        );
    }

    @Test
    @DisplayName("펀딩 상태별 검증")
    void validateFundingStatuses_Success() {
        // Given
        List<ArtistFundingResponse.Funding> fundings = Arrays.asList(
                createFundingWithStatus("OPEN", "진행중"),
                createFundingWithStatus("CLOSED", "마감"),
                createFundingWithStatus("SUCCESS", "성공"),
                createFundingWithStatus("FAILED", "실패"),
                createFundingWithStatus("CANCELED", "취소됨")
        );

        // Then - 상태별 검증
        assertAll(
                () -> assertThat(fundings.get(0).status()).isEqualTo("OPEN"),
                () -> assertThat(fundings.get(0).statusText()).isEqualTo("진행중"),
                () -> assertThat(fundings.get(1).status()).isEqualTo("CLOSED"),
                () -> assertThat(fundings.get(1).statusText()).isEqualTo("마감"),
                () -> assertThat(fundings.get(2).status()).isEqualTo("SUCCESS"),
                () -> assertThat(fundings.get(2).statusText()).isEqualTo("성공"),
                () -> assertThat(fundings.get(3).status()).isEqualTo("FAILED"),
                () -> assertThat(fundings.get(3).statusText()).isEqualTo("실패"),
                () -> assertThat(fundings.get(4).status()).isEqualTo("CANCELED"),
                () -> assertThat(fundings.get(4).statusText()).isEqualTo("취소됨")
        );
    }

    @Test
    @DisplayName("달성률과 플래그 일관성 검증")
    void validateAchievementLogic_Success() {
        // Given
        ArtistFundingResponse.Funding achievedFunding = createFundingWithAchievement(150.0);
        ArtistFundingResponse.Funding notAchievedFunding = createFundingWithAchievement(75.0);

        // Then - 달성률과 목표달성 플래그 일관성
        assertAll(
                // 목표 달성한 펀딩
                () -> assertThat(achievedFunding.achievementRate()).isEqualTo(150.0),
                () -> assertThat(achievedFunding.flags().goalAchieved()).isTrue(),
                // 목표 미달성한 펀딩
                () -> assertThat(notAchievedFunding.achievementRate()).isEqualTo(75.0),
                () -> assertThat(notAchievedFunding.flags().goalAchieved()).isFalse()
        );
    }

    @Test
    @DisplayName("권한 로직 검증")
    void validatePermissions_Success() {
        // Given
        ArtistFundingResponse.Permissions fullPermissions = createFullPermissions();
        ArtistFundingResponse.Permissions restrictedPermissions = createRestrictedPermissions();

        // Then - 권한 검증
        assertAll(
                // 전체 권한
                () -> assertThat(fullPermissions.canEdit()).isTrue(),
                () -> assertThat(fullPermissions.canCancel()).isTrue(),
                () -> assertThat(fullPermissions.canCreateNews()).isTrue(),
                // 제한된 권한
                () -> assertThat(restrictedPermissions.canEdit()).isTrue(),
                () -> assertThat(restrictedPermissions.canCancel()).isFalse(),
                () -> assertThat(restrictedPermissions.canCreateNews()).isFalse()
        );
    }

    @Test
    @DisplayName("펀딩 플래그 상태 검증")
    void validateFlags_Success() {
        // Given
        ArtistFundingResponse.Flags activeFlags = createActiveFlags();
        ArtistFundingResponse.Flags endedFlags = createEndedFlags();

        // Then - 플래그 상태 검증
        assertAll(
                // 진행중 펀딩 플래그
                () -> assertThat(activeFlags.goalAchieved()).isTrue(),
                () -> assertThat(activeFlags.dueSoon()).isTrue(),
                () -> assertThat(activeFlags.ended()).isFalse(),
                // 종료된 펀딩 플래그
                () -> assertThat(endedFlags.goalAchieved()).isTrue(),
                () -> assertThat(endedFlags.dueSoon()).isFalse(),
                () -> assertThat(endedFlags.ended()).isTrue()
        );
    }

    @Test
    @DisplayName("펀딩 목록 페이징 구조 검증")
    void validatePagination_Success() {
        // Given
        ArtistFundingResponse.Summary summary = createSampleSummary();
        List<ArtistFundingResponse.Funding> fundings = Arrays.asList(
                createSampleFunding(),
                createSampleFunding(),
                createSampleFunding()
        );

        // When
        ArtistFundingResponse.List response = new ArtistFundingResponse.List(
                summary, fundings, 0, 20, 15L, 1, false, false
        );

        // Then - 페이징 로직 검증
        assertAll(
                () -> assertThat(response.getSummary()).isNotNull(),
                () -> assertThat(response.getContent()).hasSize(3),
                () -> assertThat(response.getPage()).isNotNegative(),
                () -> assertThat(response.getSize()).isPositive(),
                () -> assertThat(response.getTotalElements()).isNotNegative(),
                () -> assertThat(response.getTotalPages()).isPositive(),
                () -> assertThat(response.isHasNext()).isFalse(),
                () -> assertThat(response.isHasPrevious()).isFalse()
        );
    }

    @Test
    @DisplayName("요약 정보와 목록 일관성 검증")
    void validateSummaryConsistency_Success() {
        // Given
        ArtistFundingResponse.Summary summary = new ArtistFundingResponse.Summary(15, 8, 6, 1);

        // Then - 요약 정보 일관성 검증
        assertAll(
                () -> assertThat(summary.openFundings() + summary.successFundings() + summary.failedFundings())
                        .isLessThanOrEqualTo(summary.totalFundings()),
                () -> assertThat(summary.totalFundings()).isEqualTo(15),
                () -> assertThat(summary.openFundings()).isNotNegative(),
                () -> assertThat(summary.successFundings()).isNotNegative(),
                () -> assertThat(summary.failedFundings()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistFundingResponse.Funding funding = new ArtistFundingResponse.Funding(
                456789L,
                "펀딩 제목입니다 펀딩 제목입니다",
                "OPEN",
                "진행중",
                900000L,
                900000L,
                100.0,
                800,
                "2025-08-01",
                "2025-09-18",
                "2025-09-01",
                "https://example.com/image.jpg",
                new ArtistFundingResponse.Category(1L, "스티커"),
                new ArtistFundingResponse.Permissions(true, true, true),
                new ArtistFundingResponse.Flags(true, false, true)
        );

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(funding.fundingId()).isEqualTo(456789L),
                () -> assertThat(funding.title()).isEqualTo("펀딩 제목입니다 펀딩 제목입니다"),
                () -> assertThat(funding.status()).isEqualTo("OPEN"),
                () -> assertThat(funding.statusText()).isEqualTo("진행중"),
                () -> assertThat(funding.targetAmount()).isEqualTo(900000L),
                () -> assertThat(funding.currentAmount()).isEqualTo(900000L),
                () -> assertThat(funding.achievementRate()).isEqualTo(100.0),
                () -> assertThat(funding.category().name()).isEqualTo("스티커"),
                () -> assertThat(funding.permissions().canCreateNews()).isTrue(),
                () -> assertThat(funding.flags().goalAchieved()).isTrue()
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private ArtistFundingResponse.Funding createSampleFunding() {
        return new ArtistFundingResponse.Funding(
                123L,
                "테스트 펀딩",
                "OPEN",
                "진행중",
                500000L,
                600000L,
                120.0,
                50,
                "2025-08-01",
                "2025-09-30",
                "2025-08-01",
                "https://example.com/test.jpg",
                new ArtistFundingResponse.Category(1L, "테스트 카테고리"),
                createFullPermissions(),
                createActiveFlags()
        );
    }

    private ArtistFundingResponse.Funding createFundingWithStatus(String status, String statusText) {
        return new ArtistFundingResponse.Funding(
                100L,
                "상태 테스트 펀딩",
                status,
                statusText,
                100000L,
                50000L,
                50.0,
                10,
                "2025-08-01",
                "2025-09-30",
                "2025-08-01",
                null,
                new ArtistFundingResponse.Category(1L, "테스트"),
                createFullPermissions(),
                createActiveFlags()
        );
    }

    private ArtistFundingResponse.Funding createFundingWithAchievement(double achievementRate) {
        long targetAmount = 100000L;
        long currentAmount = (long) (targetAmount * achievementRate / 100.0);
        
        return new ArtistFundingResponse.Funding(
                200L,
                "달성률 테스트 펀딩",
                "OPEN",
                "진행중",
                targetAmount,
                currentAmount,
                achievementRate,
                10,
                "2025-08-01",
                "2025-09-30",
                "2025-08-01",
                null,
                new ArtistFundingResponse.Category(1L, "테스트"),
                createFullPermissions(),
                new ArtistFundingResponse.Flags(achievementRate >= 100.0, false, false)
        );
    }

    private ArtistFundingResponse.Permissions createFullPermissions() {
        return new ArtistFundingResponse.Permissions(true, true, true);
    }

    private ArtistFundingResponse.Permissions createRestrictedPermissions() {
        return new ArtistFundingResponse.Permissions(true, false, false);
    }

    private ArtistFundingResponse.Flags createActiveFlags() {
        return new ArtistFundingResponse.Flags(true, true, false);
    }

    private ArtistFundingResponse.Flags createEndedFlags() {
        return new ArtistFundingResponse.Flags(true, false, true);
    }

    private ArtistFundingResponse.Summary createSampleSummary() {
        return new ArtistFundingResponse.Summary(15, 8, 6, 1);
    }
}
