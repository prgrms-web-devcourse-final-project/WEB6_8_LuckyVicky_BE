package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.request.*;
import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.dashboard.customer.service.DashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 고객용 대시보드 컨트롤러
 * 
 * 고객이 자신의 계정 정보, 주문 내역, 작가 신청 현황 등을 조회할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * 
 * 제공 기능:
 * <ul>
 *   <li>계정 설정 조회 (프로필, 연락처, 보안)</li>
 *   <li>작가 신청 내역 조회 (목록, 상세)</li>
 *   <li>주문 내역 조회</li>
 *   <li>팔로우한 작가 목록 조회</li>
 *   <li>찜한 상품 목록 조회</li>
 *   <li>참여한 펀딩 목록 조회</li>
 * </ul>
 * 
 * 2025.09.25 Request DTO 패턴 적용 및 코드 정리
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
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "profile,contact,security")
            @Pattern(regexp = "^(profile|contact|security)(,(profile|contact|security))*$",
                    message = "include는 profile, contact, security 중 하나 이상이어야 합니다")
            String include) {
        
        log.info("계정 설정 조회 - include: {}", include);
        
        AccountResponse.Settings response = dashboardService.getAccountSettings(authorization, include);
        return ResponseEntity.ok(RsData.ok("계정 설정 조회 성공", response));
    }
    
    /**
     * 작가 신청 내역 목록 조회
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "작가 신청 내역 목록 조회", description = "작가 입점 신청 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.List>> getArtistApplications(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistApplicationSearchRequest request) {
        
        log.info("작가 신청 내역 조회 - page: {}, size: {}, status: {}", 
                request.page(), request.size(), request.status());
        
        ArtistApplicationResponse.List response = dashboardService.getArtistApplications(
                authorization, request.page(), request.size(), request.status(), 
                request.startDate(), request.endDate(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("작가 신청 내역 조회 성공", response));
    }
    
    /**
     * 입점 신청 상세 조회
     */
    @GetMapping("/artist-applications/{applicationId}")
    @Operation(summary = "입점 신청 상세 조회", description = "특정 작가 입점 신청의 상세 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.Detail>> getArtistApplicationDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable 
            @Min(value = 1, message = "신청 ID는 1 이상이어야 합니다") 
            Long applicationId) {
        
        log.info("입점 신청 상세 조회 - applicationId: {}", applicationId);
        
        ArtistApplicationResponse.Detail response = dashboardService.getArtistApplicationDetail(
                authorization, applicationId);
        return ResponseEntity.ok(RsData.ok("입점 신청 상세 조회 성공", response));
    }
    
    /**
     * 주문 목록 조회
     */
    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<OrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute OrderSearchRequest request) {
        
        log.info("주문 목록 조회 - page: {}, size: {}, status: {}", 
                request.page(), request.size(), request.status());
        
        OrderResponse.List response = dashboardService.getOrders(
                authorization, request.page(), request.size(), request.status(), request.aftersalesStatus(),
                request.from(), request.to(), request.period(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("주문 목록 조회 성공", response));
    }
    
    /**
     * 내가 팔로우한 작가 목록 조회
     */
    @GetMapping("/following-artists/{userId}")
    @Operation(summary = "내가 팔로우한 작가 목록 조회", description = "팔로우하고 있는 작가들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FollowingResponse.List>> getFollowingArtists(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute FollowingSearchRequest request) {
        
        log.info("팔로우한 작가 목록 조회 - userId: {}, page: {}, size: {}", 
                userId, request.page(), request.size());
        
        FollowingResponse.List response = dashboardService.getFollowingArtists(
                userId, authorization, request.page(), request.size(), request.keyword(),
                request.status(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("팔로우한 작가 목록 조회 성공", response));
    }
    
    /**
     * 찜한 상품 목록 조회
     */
    @GetMapping("/wishlist")
    @Operation(summary = "찜한 상품 목록 조회", description = "찜한 상품들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<WishlistResponse.List>> getWishlist(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute WishlistSearchRequest request) {
        
        log.info("찜한 상품 목록 조회 - page: {}, size: {}, keyword: {}", 
                request.page(), request.size(), request.keyword());
        
        WishlistResponse.List response = dashboardService.getWishlist(
                authorization, request.page(), request.size(), request.keyword(),
                request.artistId(), request.categoryId(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("찜한 상품 목록 조회 성공", response));
    }
    
    /**
     * 참여한 펀딩 목록 조회
     */
    @GetMapping("/funding")
    @Operation(summary = "참여한 펀딩 목록 조회", description = "참여한 펀딩들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FundingResponse.List>> getFundingParticipations(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute FundingSearchRequest request) {
        
        log.info("참여한 펀딩 목록 조회 - page: {}, size: {}, status: {}", 
                request.page(), request.size(), request.status());
        
        FundingResponse.List response = dashboardService.getFundingParticipations(
                authorization, request.page(), request.size(), request.status(),
                request.keyword(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("참여한 펀딩 목록 조회 성공", response));
    }
    
    /**
     * 교환/반품 폼 데이터 조회
     */
    @GetMapping("/returns/{returnId}/form-data")
    @Operation(summary = "교환/반품 폼 데이터 조회", description = "특정 교환/반품 신청의 폼 데이터를 조회합니다")
    public ResponseEntity<RsData<ReturnResponse.FormData>> getReturnFormData(
            @RequestHeader("Authorization") String authorization,
            @PathVariable 
            @Min(value = 1, message = "교환/반품 ID는 1 이상이어야 합니다") 
            Long returnId) {
        
        log.info("교환/반품 폼 데이터 조회 - returnId: {}", returnId);
        
        ReturnResponse.FormData response = dashboardService.getReturnFormData(authorization, returnId);
        return ResponseEntity.ok(RsData.ok("교환/반품 폼 데이터 조회 성공", response));
    }
    
    /**
     * 캐시 정보 조회
     */
    @GetMapping("/cash")
    @Operation(summary = "캐시 정보 조회", description = "현재 보유 캐시 정보를 조회합니다")
    public ResponseEntity<RsData<CashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {
        
        log.info("캐시 정보 조회 요청");
        
        CashResponse.Balance response = dashboardService.getCashBalance(authorization);
        return ResponseEntity.ok(RsData.ok("캐시 정보 조회 성공", response));
    }
    
    /**
     * 캐시 충전 내역 조회
     */
    @GetMapping("/cash/history")
    @Operation(summary = "캐시 충전 내역 조회", description = "캐시 충전 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<CashResponse.HistoryList>> getCashHistory(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute CashHistorySearchRequest request) {
        
        log.info("캐시 충전 내역 조회 - page: {}, size: {}, method: {}", 
                request.page(), request.size(), request.method());
        
        CashResponse.HistoryList response = dashboardService.getCashHistory(
                authorization, request.page(), request.size(), request.method(), request.status(),
                request.dateFrom(), request.dateTo(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("캐시 충전 내역 조회 성공", response));
    }
}
