package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * DashboardServiceImpl 테스트
 * 핵심 비즈니스 로직과 데이터 일관성에 집중
 * 2025.09.23 수정
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 서비스 구현체 테스트")
class DashboardServiceImplTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Test
    @DisplayName("계정 설정 조회 - 전체 정보 반환")
    void getAccountSettings_ReturnsCompleteInfo() {
        // When
        AccountResponse.Settings result = dashboardService.getAccountSettings(
                TEST_AUTHORIZATION, "profile,contact,security");

        // Then - 구조와 핵심 데이터 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.profile()).isNotNull(),
                () -> assertThat(result.contact()).isNotNull(),
                () -> assertThat(result.security()).isNotNull(),
                // 비즈니스 로직 검증
                () -> assertThat(result.profile().userId()).isEqualTo(10025L),
                () -> assertThat(result.contact().email()).contains("@"),
                () -> assertThat(result.contact().emailVerified()).isTrue(),
                () -> assertThat(result.security().lastPasswordChangedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("계정 설정 조회 - 부분 정보 처리")
    void getAccountSettings_HandlesPartialInfo() {
        // When - 프로필만 조회
        AccountResponse.Settings profileOnly = dashboardService.getAccountSettings(
                TEST_AUTHORIZATION, "profile");

        // When - 연락처만 조회
        AccountResponse.Settings contactOnly = dashboardService.getAccountSettings(
                TEST_AUTHORIZATION, "contact");

        // Then - 부분 조회 로직 검증
        assertAll(
                () -> assertThat(profileOnly.profile()).isNotNull(),
                () -> assertThat(profileOnly.contact()).isNull(),
                () -> assertThat(profileOnly.security()).isNull(),
                () -> assertThat(contactOnly.profile()).isNull(),
                () -> assertThat(contactOnly.contact()).isNotNull(),
                () -> assertThat(contactOnly.security()).isNull()
        );
    }

    @Test
    @DisplayName("작가 신청 목록 조회 - 페이징과 통계 일관성")
    void getArtistApplications_ReturnsConsistentData() {
        // When
        ArtistApplicationResponse.List result = dashboardService.getArtistApplications(
                TEST_AUTHORIZATION, 0, 10, "PENDING", null, null, "submittedAt", "DESC");

        // Then - 페이징과 통계 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 통계 데이터 일관성
                () -> assertThat(result.getSummary().total()).isEqualTo(2),
                () -> assertThat(result.getSummary().pending() + result.getSummary().approved() + result.getSummary().rejected())
                        .isEqualTo(result.getSummary().total()),
                // 페이징 데이터
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getTotalElements()).isEqualTo(2),
                () -> assertThat(result.getPage()).isNotNegative(),
                () -> assertThat(result.getSize()).isPositive()
        );
    }

    @Test
    @DisplayName("주문 목록 조회 - 복합 데이터 구조 검증")
    void getOrders_ReturnsComplexStructure() {
        // When
        OrderResponse.List result = dashboardService.getOrders(
                TEST_AUTHORIZATION, 0, 10, "PENDING", null, null, null, "MONTH", "orderDate", "DESC");

        // Then - 복합 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                () -> assertThat(result.getTimezone()).isEqualTo("Asia/Seoul"),
                () -> assertThat(result.getPeriod()).isNotNull(),
                // 통계 합계 검증
                () -> assertThat(result.getSummary().totalOrders()).isEqualTo(25),
                () -> assertThat(result.getSummary().pending()).isNotNegative(),
                () -> assertThat(result.getSummary().cancelRequested()).isNotNegative(),
                // 주문 구조 검증
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().getFirst().orderId()).isNotNull(),
                () -> assertThat(result.getContent().getFirst().representativeItem()).isNotNull(),
                () -> assertThat(result.getContent().getFirst().items()).isNotEmpty()
        );
    }

    @Test
    @DisplayName("팔로우 작가 목록 조회 - 관계 데이터 검증")
    void getFollowingArtists_ReturnsRelationshipData() {
        // When
        FollowingResponse.List result = dashboardService.getFollowingArtists(
                "abc123", TEST_AUTHORIZATION, 0, 8, null, "FOLLOWING", "followedAt", "DESC");

        // Then - 관계 데이터 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getProfile()).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 프로필 정보
                () -> assertThat(result.getProfile().userId()).isEqualTo("abc123"),
                () -> assertThat(result.getProfile().nickname()).isNotBlank(),
                // 팔로우 관계 검증
                () -> assertThat(result.getSummary().totalFollowing()).isEqualTo(5),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().getFirst().followRelation().status()).isEqualTo("FOLLOWING"),
                () -> assertThat(result.getContent().getFirst().followerCount()).isNotNegative()
        );
    }

    @Test
    @DisplayName("찜한 상품 목록 조회 - 기본 구조 검증")
    void getWishlist_ReturnsBasicStructure() {
        // When
        WishlistResponse.List result = dashboardService.getWishlist(
                TEST_AUTHORIZATION, 0, 10, null, null, null, "addedAt", "DESC");

        // Then - 기본 구조 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                () -> assertThat(result.getBulkActions()).isNotNull(),
                () -> assertThat(result.getSummary().totalWishItems()).isEqualTo(15),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getBulkActions()).hasSize(1)
        );
    }

    @Test
    @DisplayName("펀딩 참여 목록 조회 - 통계와 메타데이터 검증")
    void getFundingParticipations_ReturnsStatsAndMeta() {
        // When
        FundingResponse.List result = dashboardService.getFundingParticipations(
                TEST_AUTHORIZATION, 0, 10, null, null, "pledgedDate", "DESC");

        // Then - 통계와 메타데이터 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 통계 일관성 검증
                () -> assertThat(result.getSummary().totalParticipations()).isEqualTo(8),
                () -> assertThat(result.getSummary().active() + result.getSummary().ended() 
                        + result.getSummary().fulfilling() + result.getSummary().fulfilled())
                        .isLessThanOrEqualTo(result.getSummary().totalParticipations()),
                // 메타데이터 구조 검증
                () -> assertThat(result.getContent().getFirst().meta()).isNotNull(),
                () -> assertThat(result.getContent().getFirst().meta().userPledge()).isNotNull(),
                () -> assertThat(result.getContent().getFirst().meta().permissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("캐시 정보 조회 - 금액 일관성 검증")
    void getCashBalance_ReturnsValidBalance() {
        // When
        CashResponse.Balance result = dashboardService.getCashBalance(TEST_AUTHORIZATION);

        // Then - 금액 일관성 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.currentBalance()).isNotNegative(),
                () -> assertThat(result.currency()).isEqualTo("KRW"),
                () -> assertThat(result.updatedAt()).isBeforeOrEqualTo(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("캐시 내역 조회 - 거래 데이터 검증")
    void getCashHistory_ReturnsTransactionData() {
        // When
        CashResponse.HistoryList result = dashboardService.getCashHistory(
                TEST_AUTHORIZATION, 0, 10, null, null, null, null, "occurredAt", "DESC");

        // Then - 거래 데이터 검증
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getSummary()).isNotNull(),
                () -> assertThat(result.getContent()).isNotNull(),
                // 금액 일관성 검증
                () -> assertThat(result.getSummary().currentBalance()).isEqualTo(5),
                () -> assertThat(result.getSummary().periodTotalRecharge()).isNotNegative(),
                () -> assertThat(result.getSummary().periodTotalBonus()).isNotNegative(),
                // 거래 내역 검증
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().getFirst().amount()).isPositive(),
                () -> assertThat(result.getContent().getFirst().status()).isNotBlank(),
                () -> assertThat(result.getContent().getFirst().occurredAt()).isNotNull()
        );
    }
}
