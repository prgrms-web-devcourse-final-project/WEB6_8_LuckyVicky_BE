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
 * 2025.09.24 생성
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
                () -> assertThat(funding.getFundingId()).isNotNull(),
                () -> assertThat(funding.getTitle()).isNotBlank(),
                () -> assertThat(funding.getStatus()).isNotBlank(),
                () -> assertThat(funding.getTargetAmount()).isNotNegative(),
                () -> assertThat(funding.getCurrentAmount()).isNotNegative(),
                () -> assertThat(funding.getAchievementRate()).isNotNegative(),
                () -> assertThat(funding.getSupporterCount()).isNotNegative(),
                () -> assertThat(funding.getCategory()).isNotNull(),
                () -> assertThat(funding.getPermissions()).isNotNull(),
                () -> assertThat(funding.getFlags()).isNotNull()
        );
    }

    @Test
    @DisplayName("펀딩 상태별 검증")
    void validateFundingStatuses_Success() {
        // Given
        List<ArtistFundingResponse.Funding> fundings = Arrays.asList(
                createFundingWithStatus("ACTIVE"),
                createFundingWithStatus("COMPLETED"),
                createFundingWithStatus("CANCELLED"),
                createFundingWithStatus("PAUSED"),
                createFundingWithStatus("PENDING")
        );

        // Then - 상태별 검증
        assertAll(
                () -> assertThat(fundings.get(0).getStatus()).isEqualTo("ACTIVE"),
                () -> assertThat(fundings.get(1).getStatus()).isEqualTo("COMPLETED"),
                () -> assertThat(fundings.get(2).getStatus()).isEqualTo("CANCELLED"),
                () -> assertThat(fundings.get(3).getStatus()).isEqualTo("PAUSED"),
                () -> assertThat(fundings.get(4).getStatus()).isEqualTo("PENDING")
        );
    }

    @Test
    @DisplayName("달성률과 플래그 일관성 검증")
    void validateAchievementLogic_Success() {
        // Given
        ArtistFundingResponse.Funding achievedFunding = createFundingWithAchievement(150);
        ArtistFundingResponse.Funding notAchievedFunding = createFundingWithAchievement(75);

        // Then - 달성률과 목표달성 플래그 일관성
        assertAll(
                // 목표 달성한 펀딩
                () -> assertThat(achievedFunding.getAchievementRate()).isEqualTo(150),
                () -> assertThat(achievedFunding.getFlags().isGoalAchieved()).isTrue(),
                // 목표 미달성한 펀딩
                () -> assertThat(notAchievedFunding.getAchievementRate()).isEqualTo(75),
                () -> assertThat(notAchievedFunding.getFlags().isGoalAchieved()).isFalse()
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
                () -> assertThat(fullPermissions.isCanEdit()).isTrue(),
                () -> assertThat(fullPermissions.isCanCancel()).isTrue(),
                () -> assertThat(fullPermissions.isCanRequestSale()).isTrue(),
                // 제한된 권한
                () -> assertThat(restrictedPermissions.isCanEdit()).isTrue(),
                () -> assertThat(restrictedPermissions.isCanCancel()).isFalse(),
                () -> assertThat(restrictedPermissions.isCanRequestSale()).isFalse()
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
                () -> assertThat(activeFlags.isGoalAchieved()).isTrue(),
                () -> assertThat(activeFlags.isDueSoon()).isTrue(),
                () -> assertThat(activeFlags.isEnded()).isFalse(),
                // 종료된 펀딩 플래그
                () -> assertThat(endedFlags.isGoalAchieved()).isTrue(),
                () -> assertThat(endedFlags.isDueSoon()).isFalse(),
                () -> assertThat(endedFlags.isEnded()).isTrue()
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
        List<ArtistFundingResponse.BulkAction> bulkActions = Arrays.asList(createSampleBulkAction());

        // When
        ArtistFundingResponse.List response = new ArtistFundingResponse.List(
                summary, fundings, bulkActions, 0, 20, 15L, 1, false, false
        );

        // Then - 페이징 로직 검증
        assertAll(
                () -> assertThat(response.getSummary()).isNotNull(),
                () -> assertThat(response.getContent()).hasSize(3),
                () -> assertThat(response.getBulkActions()).hasSize(1),
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
        ArtistFundingResponse.Summary summary = ArtistFundingResponse.Summary.builder()
                .totalFundings(15)
                .activeFundings(8)
                .completedFundings(6)
                .cancelledFundings(1)
                .build();

        // Then - 요약 정보 일관성 검증
        assertAll(
                () -> assertThat(summary.getActiveFundings() + summary.getCompletedFundings() + summary.getCancelledFundings())
                        .isEqualTo(summary.getTotalFundings()),
                () -> assertThat(summary.getTotalFundings()).isEqualTo(15),
                () -> assertThat(summary.getActiveFundings()).isNotNegative(),
                () -> assertThat(summary.getCompletedFundings()).isNotNegative(),
                () -> assertThat(summary.getCancelledFundings()).isNotNegative()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        ArtistFundingResponse.Funding funding = ArtistFundingResponse.Funding.builder()
                .fundingId(456789L)
                .title("펀딩 제목입니다 펀딩 제목입니다")
                .status("ACTIVE")
                .targetAmount(900000)
                .currentAmount(900000)
                .achievementRate(100)
                .supporterCount(800)
                .startDate("2025-08-01")
                .endDate("2025-09-18")
                .registeredAt("2025-09-01")
                .mainImage("https://example.com/image.jpg")
                .category(ArtistFundingResponse.Category.builder()
                        .id(1L)
                        .name("스티커")
                        .build())
                .permissions(ArtistFundingResponse.Permissions.builder()
                        .canEdit(true)
                        .canCancel(true)
                        .canRequestSale(true)
                        .build())
                .flags(ArtistFundingResponse.Flags.builder()
                        .goalAchieved(true)
                        .dueSoon(false)
                        .ended(true)
                        .build())
                .build();

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(funding.getFundingId()).isEqualTo(456789L),
                () -> assertThat(funding.getTitle()).isEqualTo("펀딩 제목입니다 펀딩 제목입니다"),
                () -> assertThat(funding.getStatus()).isEqualTo("ACTIVE"),
                () -> assertThat(funding.getTargetAmount()).isEqualTo(900000),
                () -> assertThat(funding.getCurrentAmount()).isEqualTo(900000),
                () -> assertThat(funding.getAchievementRate()).isEqualTo(100),
                () -> assertThat(funding.getCategory().getName()).isEqualTo("스티커"),
                () -> assertThat(funding.getPermissions().isCanRequestSale()).isTrue(),
                () -> assertThat(funding.getFlags().isGoalAchieved()).isTrue()
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private ArtistFundingResponse.Funding createSampleFunding() {
        return ArtistFundingResponse.Funding.builder()
                .fundingId(123L)
                .title("테스트 펀딩")
                .status("ACTIVE")
                .targetAmount(500000)
                .currentAmount(600000)
                .achievementRate(120)
                .supporterCount(50)
                .startDate("2025-08-01")
                .endDate("2025-09-30")
                .registeredAt("2025-08-01")
                .mainImage("https://example.com/test.jpg")
                .category(ArtistFundingResponse.Category.builder()
                        .id(1L)
                        .name("테스트 카테고리")
                        .build())
                .permissions(createFullPermissions())
                .flags(createActiveFlags())
                .build();
    }

    private ArtistFundingResponse.Funding createFundingWithStatus(String status) {
        return ArtistFundingResponse.Funding.builder()
                .fundingId(100L)
                .title("상태 테스트 펀딩")
                .status(status)
                .targetAmount(100000)
                .currentAmount(50000)
                .achievementRate(50)
                .supporterCount(10)
                .startDate("2025-08-01")
                .endDate("2025-09-30")
                .registeredAt("2025-08-01")
                .category(ArtistFundingResponse.Category.builder()
                        .id(1L)
                        .name("테스트")
                        .build())
                .permissions(createFullPermissions())
                .flags(createActiveFlags())
                .build();
    }

    private ArtistFundingResponse.Funding createFundingWithAchievement(int achievementRate) {
        return ArtistFundingResponse.Funding.builder()
                .fundingId(200L)
                .title("달성률 테스트 펀딩")
                .status("ACTIVE")
                .targetAmount(100000)
                .currentAmount(achievementRate * 1000)
                .achievementRate(achievementRate)
                .supporterCount(10)
                .category(ArtistFundingResponse.Category.builder()
                        .id(1L)
                        .name("테스트")
                        .build())
                .permissions(createFullPermissions())
                .flags(ArtistFundingResponse.Flags.builder()
                        .goalAchieved(achievementRate >= 100)
                        .dueSoon(false)
                        .ended(false)
                        .build())
                .build();
    }

    private ArtistFundingResponse.Permissions createFullPermissions() {
        return ArtistFundingResponse.Permissions.builder()
                .canEdit(true)
                .canCancel(true)
                .canRequestSale(true)
                .build();
    }

    private ArtistFundingResponse.Permissions createRestrictedPermissions() {
        return ArtistFundingResponse.Permissions.builder()
                .canEdit(true)
                .canCancel(false)
                .canRequestSale(false)
                .build();
    }

    private ArtistFundingResponse.Flags createActiveFlags() {
        return ArtistFundingResponse.Flags.builder()
                .goalAchieved(true)
                .dueSoon(true)
                .ended(false)
                .build();
    }

    private ArtistFundingResponse.Flags createEndedFlags() {
        return ArtistFundingResponse.Flags.builder()
                .goalAchieved(true)
                .dueSoon(false)
                .ended(true)
                .build();
    }

    private ArtistFundingResponse.Summary createSampleSummary() {
        return ArtistFundingResponse.Summary.builder()
                .totalFundings(15)
                .activeFundings(8)
                .completedFundings(6)
                .cancelledFundings(1)
                .build();
    }

    private ArtistFundingResponse.BulkAction createSampleBulkAction() {
        return ArtistFundingResponse.BulkAction.builder()
                .action("REQUEST_SALE")
                .label("판매 요청")
                .requiresConfirmation(true)
                .build();
    }
}