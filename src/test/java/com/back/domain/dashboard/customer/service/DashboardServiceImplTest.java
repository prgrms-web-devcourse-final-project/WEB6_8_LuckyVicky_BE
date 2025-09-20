package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * DashboardServiceImpl 테스트
 * 
 * Service 레이어의 비즈니스 로직을 테스트합니다.
 *2025.09.20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 서비스 구현체 테스트")
class DashboardServiceImplTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private static final String TEST_AUTHORIZATION = "Bearer test-token";

    @Nested
    @DisplayName("계정 설정 조회 테스트")
    class GetAccountSettingsTest {

        @Test
        @DisplayName("전체 계정 설정 조회 성공")
        void getAccountSettings_Success_AllInfo() {
            // Given
            String include = "profile,contact,security";

            // When
            AccountResponse.Settings result = dashboardService.getAccountSettings(TEST_AUTHORIZATION, include);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getContact()).isNotNull(),
                    () -> assertThat(result.getSecurity()).isNotNull(),
                    () -> assertThat(result.getProfile().getUserId()).isEqualTo(10025L),
                    () -> assertThat(result.getProfile().getNickname()).isEqualTo("닉네임입니다"),
                    () -> assertThat(result.getContact().getEmail()).isEqualTo("user@example.com"),
                    () -> assertThat(result.getContact().getEmailVerified()).isTrue(),
                    () -> assertThat(result.getSecurity().getLastPasswordChangedAt()).isNotNull()
            );
        }

        @Test
        @DisplayName("프로필만 조회 성공")
        void getAccountSettings_Success_ProfileOnly() {
            // Given
            String include = "profile";

            // When
            AccountResponse.Settings result = dashboardService.getAccountSettings(TEST_AUTHORIZATION, include);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getContact()).isNull(),
                    () -> assertThat(result.getSecurity()).isNull()
            );
        }

        @Test
        @DisplayName("연락처만 조회 성공")
        void getAccountSettings_Success_ContactOnly() {
            // Given
            String include = "contact";

            // When
            AccountResponse.Settings result = dashboardService.getAccountSettings(TEST_AUTHORIZATION, include);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNull(),
                    () -> assertThat(result.getContact()).isNotNull(),
                    () -> assertThat(result.getSecurity()).isNull(),
                    () -> assertThat(result.getContact().getPhone()).isEqualTo("+821012345678")
            );
        }
    }

    @Nested
    @DisplayName("작가 신청 내역 조회 테스트")
    class GetArtistApplicationsTest {

        @Test
        @DisplayName("작가 신청 목록 조회 성공")
        void getArtistApplications_Success() {
            // Given
            int page = 0, size = 10;
            String status = "PENDING", sort = "submittedAt", order = "DESC";

            // When
            ArtistApplicationResponse.List result = dashboardService.getArtistApplications(
                    TEST_AUTHORIZATION, page, size, status, null, null, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    () -> assertThat(result.getSummary().getTotal()).isEqualTo(2),
                    () -> assertThat(result.getSummary().getPending()).isEqualTo(0),
                    () -> assertThat(result.getSummary().getApproved()).isEqualTo(1),
                    () -> assertThat(result.getSummary().getRejected()).isEqualTo(1),
                    () -> assertThat(result.getContent()).hasSize(2),
                    () -> assertThat(result.getPage()).isEqualTo(0),
                    () -> assertThat(result.getSize()).isEqualTo(10),
                    () -> assertThat(result.getTotalElements()).isEqualTo(2)
            );
        }

        @Test
        @DisplayName("작가 신청 상세 조회 성공")
        void getArtistApplicationDetail_Success() {
            // Given
            Long applicationId = 1L;

            // When
            ArtistApplicationResponse.Detail result = dashboardService.getArtistApplicationDetail(
                    TEST_AUTHORIZATION, applicationId);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getApplication()).isNotNull(),
                    () -> assertThat(result.getApplicant()).isNotNull(),
                    () -> assertThat(result.getContact()).isNotNull(),
                    () -> assertThat(result.getBusiness()).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getPermissions()).isNotNull(),
                    () -> assertThat(result.getApplication().getApplicationId()).isEqualTo(2L),
                    () -> assertThat(result.getApplication().getStatus()).isEqualTo("APPROVED"),
                    () -> assertThat(result.getApplicant().getArtistName()).isEqualTo("모리모리모리"),
                    () -> assertThat(result.getBusiness().getRegistrationNo()).isEqualTo("123-45-67890")
            );
        }
    }

    @Nested
    @DisplayName("주문 목록 조회 테스트")
    class GetOrdersTest {

        @Test
        @DisplayName("주문 목록 조회 성공")
        void getOrders_Success() {
            // Given
            int page = 0, size = 10;
            String status = "PENDING", period = "MONTH", sort = "orderDate", order = "DESC";

            // When
            OrderResponse.List result = dashboardService.getOrders(
                    TEST_AUTHORIZATION, page, size, status, period, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    () -> assertThat(result.getSummary().getTotalOrders()).isEqualTo(42),
                    () -> assertThat(result.getSummary().getPending()).isEqualTo(3),
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).getOrderId()).isNotNull(),
                    () -> assertThat(result.getContent().get(0).getRepresentativeItem()).isNotNull(),
                    () -> assertThat(result.getContent().get(0).getItems()).isNotNull(),
                    () -> assertThat(result.getContent().get(0).getItems()).hasSize(2)
            );
        }
    }

    @Nested
    @DisplayName("팔로우 작가 목록 조회 테스트")
    class GetFollowingArtistsTest {

        @Test
        @DisplayName("팔로우 작가 목록 조회 성공")
        void getFollowingArtists_Success() {
            // Given
            int page = 0, size = 10;
            String keyword = null, sort = "followedAt", order = "DESC";

            // When
            FollowingResponse.List result = dashboardService.getFollowingArtists(
                    TEST_AUTHORIZATION, page, size, keyword, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    () -> assertThat(result.getSummary().getTotalFollowing()).isEqualTo(2),
                    () -> assertThat(result.getSummary().getNewFollowing7d()).isEqualTo(1),
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).getArtistId()).isEqualTo("asdfd331"),
                    () -> assertThat(result.getContent().get(0).getBadges().getVerified()).isTrue(),
                    () -> assertThat(result.getContent().get(0).getPermissions().getCanUnfollow()).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("찜한 상품 목록 조회 테스트")
    class GetWishlistTest {

        @Test
        @DisplayName("찜한 상품 목록 조회 성공")
        void getWishlist_Success() {
            // Given
            int page = 0, size = 10;
            String keyword = null, artistId = null, sort = "addedAt", order = "DESC";
            Long categoryId = null;

            // When
            WishlistResponse.List result = dashboardService.getWishlist(
                    TEST_AUTHORIZATION, page, size, keyword, artistId, categoryId, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    () -> assertThat(result.getBulkActions()).isNotNull(),
                    () -> assertThat(result.getSummary().getTotalWishItems()).isEqualTo(57),
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getBulkActions()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).getWishId()).isEqualTo("w-20250918-0001"),
                    () -> assertThat(result.getContent().get(0).getArtist().getName()).isEqualTo("작가명입니다 작가명입니다"),
                    () -> assertThat(result.getBulkActions().get(0).getAction()).isEqualTo("BULK_UNWISH")
            );
        }
    }

    @Nested
    @DisplayName("펀딩 참여 목록 조회 테스트")
    class GetFundingParticipationsTest {

        @Test
        @DisplayName("펀딩 참여 목록 조회 성공")
        void getFundingParticipations_Success() {
            // Given
            int page = 0, size = 10;
            String status = null, keyword = null, sort = "endDate", order = "DESC";

            // When
            FundingResponse.List result = dashboardService.getFundingParticipations(
                    TEST_AUTHORIZATION, page, size, status, keyword, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    () -> assertThat(result.getSummary().getTotalParticipations()).isEqualTo(12),
                    () -> assertThat(result.getSummary().getActive()).isEqualTo(4),
                    () -> assertThat(result.getSummary().getSuccess()).isEqualTo(7),
                    () -> assertThat(result.getSummary().getFailed()).isEqualTo(1),
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).getParticipationId()).isEqualTo("FP-20250918-0001"),
                    () -> assertThat(result.getContent().get(0).getFunding().getTitle()).isEqualTo("펀딩 제목입니다 펀딩 제목입니다"),
                    () -> assertThat(result.getContent().get(0).getUserPledge().getPledgedAmount()).isEqualTo(30000)
            );
        }
    }
}
