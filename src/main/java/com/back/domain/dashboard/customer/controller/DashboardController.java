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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 고객용 대시보드 컨트롤러
 * 
 * 고객이 자신의 계정 정보, 주문 내역, 작가 신청 현황 등을 조회할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요(추후 인증/인가 되면 추가)
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
 * 2025.09.25 Request DTO 패턴 적용
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "CustomerDashboardController", description = "고객용 대시보드 컨트롤러")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 계정 설정 조회
     * 사용자의 계정 설정 정보를 조회합니다. include 파라미터를 통해 필요한 정보만 선택적으로 가져올 수 있다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param include 포함할 정보 (profile,contact,security 중 선택, 기본값: 전체)
     * @return 계정 설정 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException include 파라미터가 잘못된 경우
     */
    @GetMapping("/account")
    @Operation(summary = "계정 설정 조회", description = "사용자의 계정 설정 정보를 조회합니다")
    public ResponseEntity<RsData<AccountResponse.Settings>> getAccountSettings(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "profile,contact,security")
            @Pattern(regexp = "^(profile|contact|security)(,(profile|contact|security))*$",
                    message = "include는 profile, contact, security 중 하나 이상이어야 합니다")
            String include) {
        
        AccountResponse.Settings response = dashboardService.getAccountSettings(authorization, include);
        return ResponseEntity.ok(RsData.ok("계정 설정 조회 성공", response));
    }
    
    /**
     * 작가 신청 내역 목록 조회
     * 사용자가 신청한 작가 입점 신청 내역을 페이지 단위로 조회
     * 상태별 필터링과 날짜 범위 검색이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 신청 내역 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "작가 신청 내역 목록 조회", description = "작가 입점 신청 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.List>> getArtistApplications(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistApplicationSearchRequest request) {
        
        ArtistApplicationResponse.List response = dashboardService.getArtistApplications(
                authorization, request.page(), request.size(), request.status(), 
                request.startDate(), request.endDate(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("작가 신청 내역 조회 성공", response));
    }
    
    /**
     * 입점 신청 상세 조회
     * 특정 작가 입점 신청의 상세 정보를 조회
     * 신청서 내용, 첨부 파일, 심사 결과 등을 포함
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param applicationId 신청 ID (필수)
     * @return 입점 신청 상세 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 신청 ID가 유효하지 않은 경우
     */
    @GetMapping("/artist-applications/{applicationId}")
    @Operation(summary = "입점 신청 상세 조회", description = "특정 작가 입점 신청의 상세 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.Detail>> getArtistApplicationDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable 
            @Min(value = 1, message = "신청 ID는 1 이상이어야 합니다") 
            Long applicationId) {
        
        ArtistApplicationResponse.Detail response = dashboardService.getArtistApplicationDetail(
                authorization, applicationId);
        return ResponseEntity.ok(RsData.ok("입점 신청 상세 조회 성공", response));
    }
    
    /**
     * 주문 목록 조회
     * 사용자의 주문 내역을 페이지 단위로 조회
     * 주문 상태별, A/S 상태별 필터링과 기간별 검색이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 주문 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<OrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute OrderSearchRequest request) {
        
        OrderResponse.List response = dashboardService.getOrders(
                authorization, request.page(), request.size(), request.status(), request.aftersalesStatus(),
                request.from(), request.to(), request.period(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("주문 목록 조회 성공", response));
    }
    
    /**
     * 내가 팔로우한 작가 목록 조회
     * 사용자가 팔로우하고 있는 작가들의 목록을 페이지 단위로 조회
     * 작가명 검색과 다양한 정렬 기준을 지원
     * 
     * @param userId 사용자 ID ("me" 또는 실제 사용자 ID)
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 팔로우한 작가 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/following-artists/{userId}")
    @Operation(summary = "내가 팔로우한 작가 목록 조회", description = "팔로우하고 있는 작가들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FollowingResponse.List>> getFollowingArtists(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute FollowingSearchRequest request) {
        
        FollowingResponse.List response = dashboardService.getFollowingArtists(
                userId, authorization, request.page(), request.size(), request.keyword(),
                request.status(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("팔로우한 작가 목록 조회 성공", response));
    }
    
    /**
     * 찜한 상품 목록 조회
     * 사용자가 찜한 상품들의 목록을 페이지 단위로 조회
     * 상품명/작가명 검색, 특정 작가/카테고리 필터링이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 찜한 상품 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/wishlist")
    @Operation(summary = "찜한 상품 목록 조회", description = "찜한 상품들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<WishlistResponse.List>> getWishlist(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute WishlistSearchRequest request) {
        
        WishlistResponse.List response = dashboardService.getWishlist(
                authorization, request.page(), request.size(), request.keyword(),
                request.artistId(), request.categoryId(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("찜한 상품 목록 조회 성공", response));
    }
    
    /**
     * 참여한 펀딩 목록 조회
     * 사용자가 참여한 펀딩들의 목록을 페이지 단위로 조회
     * 펀딩 상태별 필터링과 제목/작가명 검색이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 참여한 펀딩 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/funding")
    @Operation(summary = "참여한 펀딩 목록 조회", description = "참여한 펀딩들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FundingResponse.List>> getFundingParticipations(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute FundingSearchRequest request) {
        
        FundingResponse.List response = dashboardService.getFundingParticipations(
                authorization, request.page(), request.size(), request.status(),
                request.keyword(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("참여한 펀딩 목록 조회 성공", response));
    }
    
    /**
     * 교환/반품 폼 데이터 조회
     * 특정 교환/반품 신청의 폼 데이터를 조회합니다.
     * 주문 요약 정보, 신청 내용, 권한 정보를 포함합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param returnId 교환/반품 ID (필수)
     * @return 교환/반품 폼 데이터
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 교환/반품 ID가 유효하지 않은 경우
     */
    @GetMapping("/returns/{returnId}/form-data")
    @Operation(summary = "교환/반품 폼 데이터 조회", description = "특정 교환/반품 신청의 폼 데이터를 조회합니다")
    public ResponseEntity<RsData<ReturnResponse.FormData>> getReturnFormData(
            @RequestHeader("Authorization") String authorization,
            @PathVariable 
            @Min(value = 1, message = "교환/반품 ID는 1 이상이어야 합니다") 
            Long returnId) {
        
        ReturnResponse.FormData response = dashboardService.getReturnFormData(authorization, returnId);
        return ResponseEntity.ok(RsData.ok("교환/반품 폼 데이터 조회 성공", response));
    }
    
    /**
     * 캐시 정보 조회
     * 현재 보유 캐시 정보를 조회합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @return 캐시 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     */
    @GetMapping("/cash")
    @Operation(summary = "캐시 정보 조회", description = "현재 보유 캐시 정보를 조회합니다")
    public ResponseEntity<RsData<CashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {
        
        CashResponse.Balance response = dashboardService.getCashBalance(authorization);
        return ResponseEntity.ok(RsData.ok("캐시 정보 조회 성공", response));
    }
    
    /**
     * 캐시 충전 내역 조회
     * 캐시 충전 내역을 페이지 단위로 조회합니다.
     * 결제 수단, 상태, 기간별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 검색 조건 (페이징, 필터링, 정렬)
     * @return 캐시 충전 내역 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/cash/history")
    @Operation(summary = "캐시 충전 내역 조회", description = "캐시 충전 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<CashResponse.HistoryList>> getCashHistory(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute CashHistorySearchRequest request) {
        
        CashResponse.HistoryList response = dashboardService.getCashHistory(
                authorization, request.page(), request.size(), request.method(), request.status(),
                request.dateFrom(), request.dateTo(), request.sort(), request.order());
        
        return ResponseEntity.ok(RsData.ok("캐시 충전 내역 조회 성공", response));
    }
}
