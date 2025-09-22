package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * DashboardServiceImpl 테스트
 * 
 * Service 레이어의 비즈니스 로직을 테스트
 *2025.09.22 수정
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
                    // Application 검증
                    () -> assertThat(result.getApplication().getApplicationId()).isEqualTo(2L),
                    () -> assertThat(result.getApplication().getStatus()).isEqualTo("REJECTED"),
                    () -> assertThat(result.getApplication().getStatusText()).isEqualTo("입점 거절"),
                    () -> assertThat(result.getApplication().getRejectionReason()).isEqualTo("브랜드 컨셉 불일치"),
                    () -> assertThat(result.getApplication().getReviewer()).isNotNull(),
                    () -> assertThat(result.getApplication().getReviewer().getId()).isEqualTo("admin_001"),
                    () -> assertThat(result.getApplication().getReviewer().getName()).isEqualTo("관리자A"),
                    // Applicant 검증
                    () -> assertThat(result.getApplicant().getArtistName()).isEqualTo("모리모리모리"),
                    () -> assertThat(result.getApplicant().getMemberId()).isEqualTo("abc123"),
                    // Business 검증
                    () -> assertThat(result.getBusiness().getRegistrationNo()).isEqualTo("123-45-67890"),
                    () -> assertThat(result.getBusiness().getTelemarketingReportNo()).isEqualTo("2025-서울강남-1234"),
                    // Profile 검증
                    () -> assertThat(result.getProfile().getDescription()).isEqualTo("작가 소개입니다."),
                    () -> assertThat(result.getProfile().getMainCategories()).containsExactly("스티커", "메모지"),
                    () -> assertThat(result.getProfile().getSns()).hasSize(1),
                    () -> assertThat(result.getProfile().getSns().get(0).getPlatform()).isEqualTo("Instagram"),
                    // Permissions 검증
                    () -> assertThat(result.getPermissions().getCanEdit()).isFalse(),
                    () -> assertThat(result.getPermissions().getCanCancel()).isFalse(),
                    () -> assertThat(result.getPermissions().getCanAppeal()).isTrue()
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
            String userId = "abc123";
            int page = 0, size = 8;
            String keyword = null, status = "FOLLOWING", sort = "followedAt", order = "DESC";

            // When
            FollowingResponse.List result = dashboardService.getFollowingArtists(
                    userId, TEST_AUTHORIZATION, page, size, keyword, status, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getProfile()).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    // Profile 검증
                    () -> assertThat(result.getProfile().getUserId()).isEqualTo("abc123"),
                    () -> assertThat(result.getProfile().getNickname()).isEqualTo("사용자닉네임"),
                    () -> assertThat(result.getProfile().getProfileImageUrl()).isNotNull(),
                    // Summary 검증
                    () -> assertThat(result.getSummary().getTotalFollowing()).isEqualTo(8),
                    // Content 검증
                    () -> assertThat(result.getContent()).hasSize(2),
                    // 첫 번째 작가 검증
                    () -> assertThat(result.getContent().get(0).getArtistId()).isEqualTo("artist_001"),
                    () -> assertThat(result.getContent().get(0).getArtistName()).isEqualTo("작가명입니다"),
                    () -> assertThat(result.getContent().get(0).getFollowerCount()).isEqualTo(500),
                    () -> assertThat(result.getContent().get(0).getFollowRelation().getStatus()).isEqualTo("FOLLOWING"),
                    () -> assertThat(result.getContent().get(0).getBadges().getVerified()).isTrue(),
                    // 페이징 검증
                    () -> assertThat(result.getPage()).isEqualTo(0),
                    () -> assertThat(result.getSize()).isEqualTo(8),
                    () -> assertThat(result.getTotalElements()).isEqualTo(8),
                    () -> assertThat(result.getTotalPages()).isEqualTo(1),
                    () -> assertThat(result.isHasNext()).isFalse(),
                    () -> assertThat(result.isHasPrevious()).isFalse()
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
            String status = null, keyword = null, sort = "pledgedDate", order = "DESC";

            // When
            FundingResponse.List result = dashboardService.getFundingParticipations(
                    TEST_AUTHORIZATION, page, size, status, keyword, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    // Summary 검증
                    () -> assertThat(result.getSummary().getTotalParticipations()).isEqualTo(12),
                    () -> assertThat(result.getSummary().getActive()).isEqualTo(4),
                    () -> assertThat(result.getSummary().getEnded()).isEqualTo(5),
                    () -> assertThat(result.getSummary().getFulfilling()).isEqualTo(1),
                    () -> assertThat(result.getSummary().getFulfilled()).isEqualTo(2),
                    // Content 검증
                    () -> assertThat(result.getContent()).hasSize(4),
                    // 첫 번째 참여 정보 검증
                    () -> assertThat(result.getContent().get(0).getParticipationNumber()).isEqualTo("00010"),
                    () -> assertThat(result.getContent().get(0).getParticipationId()).isEqualTo("FP-20250918-0001"),
                    () -> assertThat(result.getContent().get(0).getTitle()).isEqualTo("펀딩 제목입니다 펀딩 제목입니다"),
                    () -> assertThat(result.getContent().get(0).getArtist().getName()).isEqualTo("작가명입니다"),
                    () -> assertThat(result.getContent().get(0).getPledgedAmount()).isEqualTo(1000),
                    () -> assertThat(result.getContent().get(0).getStatus()).isEqualTo("ACTIVE"),
                    () -> assertThat(result.getContent().get(0).getStatusText()).isEqualTo("진행중"),
                    () -> assertThat(result.getContent().get(0).getPledgedDate()).isEqualTo("2025-09-18"),
                    // Meta 정보 검증 (첫 번째 항목)
                    () -> assertThat(result.getContent().get(0).getMeta()).isNotNull(),
                    () -> assertThat(result.getContent().get(0).getMeta().getFundingId()).isEqualTo(456789L),
                    () -> assertThat(result.getContent().get(0).getMeta().getAchievementRate()).isEqualTo(100),
                    () -> assertThat(result.getContent().get(0).getMeta().getUserPledge().getRewardTierName()).isEqualTo("리워드 A"),
                    () -> assertThat(result.getContent().get(0).getMeta().getPermissions().getCanCancelPledge()).isTrue(),
                    // 페이징 검증
                    () -> assertThat(result.getPage()).isEqualTo(0),
                    () -> assertThat(result.getSize()).isEqualTo(10),
                    () -> assertThat(result.getTotalElements()).isEqualTo(12),
                    () -> assertThat(result.getTotalPages()).isEqualTo(2),
                    () -> assertThat(result.isHasNext()).isFalse(),
                    () -> assertThat(result.isHasPrevious()).isFalse()
            );
        }
    }
    
    @Nested
    @DisplayName("교환/반품 폼 데이터 조회 테스트")
    class GetReturnFormDataTest {

        @Test
        @DisplayName("교환/반품 폼 데이터 조회 성공")
        void getReturnFormData_Success() {
            // Given
            Long returnId = 1L;

            // When
            ReturnResponse.FormData result = dashboardService.getReturnFormData(
                    TEST_AUTHORIZATION, returnId);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getForm()).isNotNull(),
                    () -> assertThat(result.getPermissions()).isNotNull(),
                    // Summary 검증
                    () -> assertThat(result.getSummary().getOrderNo()).isEqualTo("0123157"),
                    () -> assertThat(result.getSummary().getBrandName()).isEqualTo("브랜드명"),
                    () -> assertThat(result.getSummary().getTitle()).isEqualTo("상품명입니다"),
                    () -> assertThat(result.getSummary().getPrice()).isEqualTo(1000),
                    () -> assertThat(result.getSummary().getQuantity()).isEqualTo(1),
                    () -> assertThat(result.getSummary().getThumbnailUrl()).isNotNull(),
                    // Form 검증
                    () -> assertThat(result.getForm().getType()).isEqualTo("EXCHANGE"),
                    () -> assertThat(result.getForm().getMethod()).isEqualTo("PICKUP"),
                    () -> assertThat(result.getForm().getReasonCode()).isEqualTo("DEFECT"),
                    () -> assertThat(result.getForm().getDetail()).isEqualTo("스티커 구김 현상 발견"),
                    () -> assertThat(result.getForm().getImages()).hasSize(1),
                    () -> assertThat(result.getForm().getImages().get(0).getFileId()).isEqualTo("img-1"),
                    () -> assertThat(result.getForm().getImages().get(0).getFileName()).isEqualTo("photo_1.jpg"),
                    // Pickup 검증
                    () -> assertThat(result.getForm().getPickup()).isNotNull(),
                    () -> assertThat(result.getForm().getPickup().getZip()).isEqualTo("06245"),
                    () -> assertThat(result.getForm().getPickup().getAddress1()).isEqualTo("서울 강남구 테헤란로 123"),
                    () -> assertThat(result.getForm().getPickup().getAddress2()).isEqualTo("3층"),
                    () -> assertThat(result.getForm().getPickup().getName()).isEqualTo("홍길동"),
                    () -> assertThat(result.getForm().getPickup().getPhone()).isEqualTo("010-1234-5678"),
                    // Permissions 검증
                    () -> assertThat(result.getPermissions().getCanEdit()).isTrue(),
                    () -> assertThat(result.getPermissions().getCanCancel()).isTrue()
            );
        }
    }
    
    @Nested
    @DisplayName("캐시 정보 조회 테스트")
    class GetCashBalanceTest {

        @Test
        @DisplayName("캐시 정보 조회 성공")
        void getCashBalance_Success() {
            // Given
            // When
            CashResponse.Balance result = dashboardService.getCashBalance(TEST_AUTHORIZATION);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getCurrentBalance()).isEqualTo(5900),
                    () -> assertThat(result.getCurrency()).isEqualTo("KRW"),
                    () -> assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 9, 22, 10, 15))
            );
        }
    }

    @Nested
    @DisplayName("캐시 충전 내역 조회 테스트")
    class GetCashHistoryTest {

        @Test
        @DisplayName("캐시 충전 내역 조회 성공")
        void getCashHistory_Success() {
            // Given
            int page = 0, size = 10;
            String method = null, status = null, dateFrom = null, dateTo = null;
            String sort = "occurredAt", order = "DESC";

            // When
            CashResponse.HistoryList result = dashboardService.getCashHistory(
                    TEST_AUTHORIZATION, page, size, method, status, dateFrom, dateTo, sort, order);

            // Then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getSummary()).isNotNull(),
                    () -> assertThat(result.getContent()).isNotNull(),
                    // Summary 검증
                    () -> assertThat(result.getSummary().getCurrentBalance()).isEqualTo(720),
                    () -> assertThat(result.getSummary().getPeriodTotalRecharge()).isEqualTo(18000),
                    () -> assertThat(result.getSummary().getPeriodTotalBonus()).isEqualTo(480),
                    // Content 검증
                    () -> assertThat(result.getContent()).hasSize(2),
                    // 첫 번째 거래 검증
                    () -> assertThat(result.getContent().get(0).getTxId()).isEqualTo("RC-20250131-2303-0001"),
                    () -> assertThat(result.getContent().get(0).getCategory()).isEqualTo("캐시 충전"),
                    () -> assertThat(result.getContent().get(0).getAmount()).isEqualTo(2000),
                    () -> assertThat(result.getContent().get(0).getBonusPoint()).isEqualTo(60),
                    () -> assertThat(result.getContent().get(0).getMethod()).isEqualTo("NAVERPAY"),
                    () -> assertThat(result.getContent().get(0).getMethodText()).isEqualTo("네이버페이"),
                    () -> assertThat(result.getContent().get(0).getStatus()).isEqualTo("COMPLETED"),
                    () -> assertThat(result.getContent().get(0).getOccurredAt()).isEqualTo(LocalDateTime.of(2025, 1, 31, 23, 3)),
                    () -> assertThat(result.getContent().get(0).getLinks().getReceipt()).isNull(),
                    // 페이징 검증
                    () -> assertThat(result.getPage()).isEqualTo(0),
                    () -> assertThat(result.getSize()).isEqualTo(10),
                    () -> assertThat(result.getTotalElements()).isEqualTo(9),
                    () -> assertThat(result.getTotalPages()).isEqualTo(1),
                    () -> assertThat(result.isHasNext()).isFalse(),
                    () -> assertThat(result.isHasPrevious()).isFalse()
            );
        }
    }
}
