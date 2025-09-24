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
                () -> assertThat(result.getProfile()).isNotNull(),
                () -> assertThat(result.getContact()).isNotNull(),
                () -> assertThat(result.getSecurity()).isNotNull(),
                // 비즈니스 로직 검증
                () -> assertThat(result.getProfile().getUserId()).isEqualTo(10025L),
                () -> assertThat(result.getContact().getEmail()).contains("@"),
                () -> assertThat(result.getContact().getEmailVerified()).isTrue(),
                () -> assertThat(result.getSecurity().getLastPasswordChangedAt()).isNotNull()
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
                () -> assertThat(profileOnly.getProfile()).isNotNull(),
                () -> assertThat(profileOnly.getContact()).isNull(),
                () -> assertThat(profileOnly.getSecurity()).isNull(),
                () -> assertThat(contactOnly.getProfile()).isNull(),
                () -> assertThat(contactOnly.getContact()).isNotNull(),
                () -> assertThat(contactOnly.getSecurity()).isNull()
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
                () -> assertThat(result.getSummary().getTotal()).isEqualTo(2),
                () -> assertThat(result.getSummary().getPending() + result.getSummary().getApproved() + result.getSummary().getRejected())
                        .isEqualTo(result.getSummary().getTotal()),
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
                () -> assertThat(result.getSummary().getTotalOrders()).isEqualTo(25),
                () -> assertThat(result.getSummary().getPending()).isNotNegative(),
                () -> assertThat(result.getSummary().getCancelRequested()).isNotNegative(),
                // 주문 구조 검증
                () -> assertThat(result.getContent()).hasSize(1),
                () -> assertThat(result.getContent().get(0).getOrderId()).isNotNull(),
                () -> assertThat(result.getContent().get(0).getRepresentativeItem()).isNotNull(),
                () -> assertThat(result.getContent().get(0).getItems()).isNotEmpty()
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
                () -> assertThat(result.getProfile().getUserId()).isEqualTo("abc123"),
                () -> assertThat(result.getProfile().getNickname()).isNotBlank(),
                // 팔로우 관계 검증
                () -> assertThat(result.getSummary().getTotalFollowing()).isEqualTo(5),
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).getFollowRelation().getStatus()).isEqualTo("FOLLOWING"),
                () -> assertThat(result.getContent().get(0).getFollowerCount()).isNotNegative()
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
                () -> assertThat(result.getSummary().getTotalWishItems()).isEqualTo(15),
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
                () -> assertThat(result.getSummary().getTotalParticipations()).isEqualTo(8),
                () -> assertThat(result.getSummary().getActive() + result.getSummary().getEnded() 
                        + result.getSummary().getFulfilling() + result.getSummary().getFulfilled())
                        .isLessThanOrEqualTo(result.getSummary().getTotalParticipations()),
                // 메타데이터 구조 검증
                () -> assertThat(result.getContent().get(0).getMeta()).isNotNull(),
                () -> assertThat(result.getContent().get(0).getMeta().getUserPledge()).isNotNull(),
                () -> assertThat(result.getContent().get(0).getMeta().getPermissions()).isNotNull()
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
                () -> assertThat(result.getCurrentBalance()).isNotNegative(),
                () -> assertThat(result.getCurrency()).isEqualTo("KRW"),
                () -> assertThat(result.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now())
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
                () -> assertThat(result.getSummary().getCurrentBalance()).isEqualTo(5),
                () -> assertThat(result.getSummary().getPeriodTotalRecharge()).isNotNegative(),
                () -> assertThat(result.getSummary().getPeriodTotalBonus()).isNotNegative(),
                // 거래 내역 검증
                () -> assertThat(result.getContent()).hasSize(2),
                () -> assertThat(result.getContent().get(0).getAmount()).isPositive(),
                () -> assertThat(result.getContent().get(0).getStatus()).isNotBlank(),
                () -> assertThat(result.getContent().get(0).getOccurredAt()).isNotNull()
        );
    }
}
