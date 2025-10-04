package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.request.*;
import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.dashboard.customer.service.DashboardService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 고객용 대시보드 컨트롤러
 * 고객이 자신의 계정 정보, 주문 내역, 작가 신청 현황 등을 조회할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * 제공 기능:
 * <ul>
 *   <li>계정 설정 조회 (프로필, 연락처, 보안)</li>
 *   <li>작가 신청 내역 조회 (목록, 상세)</li>
 *   <li>주문 내역 조회</li>
 *   <li>팔로우한 작가 목록 조회</li>
 *   <li>찜한 상품 목록 조회</li>
 *   <li>참여한 펀딩 목록 조회</li>
 * </ul>
 * <p>
 * 2025.10.02 JWT 표준 패턴 적용 - @AuthenticationPrincipal 사용
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CustomerDashboardController", description = "고객용 대시보드 컨트롤러")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 계정 설정 조회
     */
    @GetMapping("/account")
    @Operation(summary = "계정 설정 조회", description = "사용자의 계정 설정 정보를 조회합니다")
    public ResponseEntity<RsData<AccountResponse.Settings>> getAccountSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "profile,contact,security")
            @Pattern(regexp = "^(profile|contact|security)(,(profile|contact|security))*$",
                    message = "include는 profile, contact, security 중 하나 이상이어야 합니다")
            String include) {

        log.info("계정 설정 조회 - userId: {}, include: {}", userDetails.getUserId(), include);

        AccountResponse.Settings response = dashboardService.getAccountSettings(
                userDetails.getUserId(), include);
        return ResponseEntity.ok(RsData.ok("계정 설정 조회 성공", response));
    }

    /**
     * 작가 신청 내역 목록 조회
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "작가 신청 내역 목록 조회", description = "작가 입점 신청 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.List>> getArtistApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute ArtistApplicationSearchRequest request) {

        log.info("작가 신청 내역 조회 - userId: {}, page: {}, size: {}, status: {}",
                userDetails.getUserId(), request.page(), request.size(), request.status());

        ArtistApplicationResponse.List response = dashboardService.getArtistApplications(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("작가 신청 내역 조회 성공", response));
    }

    /**
     * 입점 신청 상세 조회
     */
    @GetMapping("/artist-applications/{applicationId}")
    @Operation(summary = "입점 신청 상세 조회", description = "특정 작가 입점 신청의 상세 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.Detail>> getArtistApplicationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @Min(value = 1, message = "신청 ID는 1 이상이어야 합니다")
            Long applicationId) {

        log.info("입점 신청 상세 조회 - userId: {}, applicationId: {}",
                userDetails.getUserId(), applicationId);

        ArtistApplicationResponse.Detail response = dashboardService.getArtistApplicationDetail(
                userDetails.getUserId(), applicationId);
        return ResponseEntity.ok(RsData.ok("입점 신청 상세 조회 성공", response));
    }

    /**
     * 주문 목록 조회
     */
    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<OrderResponse.List>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute OrderSearchRequest request) {

        log.info("주문 목록 조회 - userId: {}, page: {}, size: {}, keyword: {}, sort: {}, order: {}",
                userDetails.getUserId(), request.page(), request.size(), 
                request.keyword(), request.sort(), request.order());

        OrderResponse.List response = dashboardService.getOrders(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("주문 목록 조회 성공", response));
    }

    /**
     * 내가 팔로우한 작가 목록 조회
     */
    @GetMapping("/following-artists")
    @Operation(summary = "내가 팔로우한 작가 목록 조회", description = "팔로우하고 있는 작가들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FollowingResponse.List>> getFollowingArtists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute FollowingSearchRequest request) {

        log.info("팔로우한 작가 목록 조회 - userId: {}, page: {}, size: {}",
                userDetails.getUserId(), request.page(), request.size());

        FollowingResponse.List response = dashboardService.getFollowingArtists(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("팔로우한 작가 목록 조회 성공", response));
    }

    /**
     * 찜한 상품 목록 조회
     */
    @GetMapping("/wishlist")
    @Operation(summary = "찜한 상품 목록 조회", description = "찜한 상품들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<WishlistResponse.List>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute WishlistSearchRequest request) {

        log.info("찜한 상품 목록 조회 - userId: {}, page: {}, size: {}, keyword: {}",
                userDetails.getUserId(), request.page(), request.size(), request.keyword());

        WishlistResponse.List response = dashboardService.getWishlist(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("찜한 상품 목록 조회 성공", response));
    }

    /**
     * 참여한 펀딩 목록 조회
     */
    @GetMapping("/funding")
    @Operation(summary = "참여한 펀딩 목록 조회", description = "참여한 펀딩들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FundingResponse.List>> getFundingParticipations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute FundingSearchRequest request) {

        log.info("참여한 펀딩 목록 조회 - userId: {}, page: {}, size: {}, status: {}",
                userDetails.getUserId(), request.page(), request.size(), request.status());

        FundingResponse.List response = dashboardService.getFundingParticipations(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("참여한 펀딩 목록 조회 성공", response));
    }

    /**
     * 교환/반품 폼 데이터 조회
     */
    @GetMapping("/returns/{returnId}/form-data")
    @Operation(summary = "교환/반품 폼 데이터 조회", description = "특정 교환/반품 신청의 폼 데이터를 조회합니다")
    public ResponseEntity<RsData<ReturnResponse.FormData>> getReturnFormData(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable
            @Min(value = 1, message = "교환/반품 ID는 1 이상이어야 합니다")
            Long returnId) {

        log.info("교환/반품 폼 데이터 조회 - userId: {}, returnId: {}",
                userDetails.getUserId(), returnId);

        ReturnResponse.FormData response = dashboardService.getReturnFormData(
                userDetails.getUserId(), returnId);
        return ResponseEntity.ok(RsData.ok("교환/반품 폼 데이터 조회 성공", response));
    }

    /**
     * 캐시 정보 조회
     */
    @GetMapping("/cash")
    @Operation(summary = "캐시 정보 조회", description = "현재 보유 캐시 정보를 조회합니다")
    public ResponseEntity<RsData<CashResponse.Balance>> getCashBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("캐시 정보 조회 - userId: {}", userDetails.getUserId());

        CashResponse.Balance response = dashboardService.getCashBalance(userDetails.getUserId());
        return ResponseEntity.ok(RsData.ok("캐시 정보 조회 성공", response));
    }

    /**
     * 캐시 충전 내역 조회
     */
    @GetMapping("/cash/history")
    @Operation(summary = "캐시 충전 내역 조회", description = "캐시 충전 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<CashResponse.HistoryList>> getCashHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CashHistorySearchRequest request) {

        log.info("캐시 충전 내역 조회 - userId: {}, page: {}, size: {}, method: {}",
                userDetails.getUserId(), request.page(), request.size(), request.method());

        CashResponse.HistoryList response = dashboardService.getCashHistory(
                userDetails.getUserId(), request);

        return ResponseEntity.ok(RsData.ok("캐시 충전 내역 조회 성공", response));
    }
}
