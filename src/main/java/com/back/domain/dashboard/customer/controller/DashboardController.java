package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.dashboard.customer.service.DashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 고객용 대시보드 컨트롤러
 * 
 * 계정 설정, 작가 신청, 주문, 찜한 상품, 팔로우한 작가, 펀딩 참여 내역 조회
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "CustomerDashboardController", description = "고객용 대시보드 컨트롤러")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 계정 설정 조회
     * 
     * @param authorization Bearer 토큰
     * @param include 포함할 정보 (profile,contact,security)
     * @return 계정 설정 정보
     */
    @GetMapping("/account")
    @Operation(summary = "계정 설정 조회")
    public ResponseEntity<RsData<AccountResponse.Settings>> getAccountSettings(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "profile,contact,security") String include) {
        
        AccountResponse.Settings response = dashboardService.getAccountSettings(authorization, include);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "계정 설정 조회 성공",
                response
        ));
    }
    
    /**
     * 작가 신청 내역 목록 조회
     * 
     * @param authorization Bearer 토큰
     * @param page 페이지 번호 (0-base)
     * @param size 페이지 크기
     * @param status 상태 필터 (PENDING, APPROVED, REJECTED)
     * @param startDate 신청일 시작 (yyyy-MM-dd)
     * @param endDate 신청일 종료 (yyyy-MM-dd)
     * @param sort 정렬 기준
     * @param order 정렬 방향
     * @return 작가 신청 내역 목록
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "작가 신청 내역 목록 조회")
    public ResponseEntity<RsData<ArtistApplicationResponse.List>> getArtistApplications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "submittedAt") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        
        ArtistApplicationResponse.List response = dashboardService.getArtistApplications(
                authorization, page, size, status, startDate, endDate, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "작가 신청 내역 조회 성공",
                response
        ));
    }
    
    /**
     * 입점 신청 상세 조회
     * 
     * @param authorization Bearer 토큰
     * @param applicationId 신청 ID
     * @return 입점 신청 상세 정보
     */
    @GetMapping("/artist-applications/{applicationId}")
    @Operation(summary = "입점 신청 상세 조회")
    public ResponseEntity<RsData<ArtistApplicationResponse.Detail>> getArtistApplicationDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long applicationId) {
        
        ArtistApplicationResponse.Detail response = dashboardService.getArtistApplicationDetail(
                authorization, applicationId);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "입점 신청 상세 조회 성공",
                response
        ));
    }
    
    /**
     * 주문 목록 조회
     * 
     * @param authorization Bearer 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param status 주문 상태 필터
     * @param period 기간 필터
     * @param sort 정렬 기준
     * @param order 정렬 방향
     * @return 주문 목록
     */
    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회")
    public ResponseEntity<RsData<OrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(defaultValue = "orderDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        
        OrderResponse.List response = dashboardService.getOrders(
                authorization, page, size, status, period, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "주문 목록 조회 성공",
                response
        ));
    }
    
    /**
     * 내가 팔로우한 작가 목록 조회
     * 
     * @param authorization Bearer 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param keyword 검색 키워드
     * @param sort 정렬 기준
     * @param order 정렬 방향
     * @return 팔로우한 작가 목록
     */
    @GetMapping("/following-artists")
    @Operation(summary = "내가 팔로우한 작가 목록 조회")
    public ResponseEntity<RsData<FollowingResponse.List>> getFollowingArtists(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "followedAt") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        
        FollowingResponse.List response = dashboardService.getFollowingArtists(
                authorization, page, size, keyword, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "팔로우한 작가 목록 조회 성공",
                response
        ));
    }
    
    /**
     * 찜한 상품 목록 조회
     * 
     * @param authorization Bearer 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param keyword 검색 키워드
     * @param artistId 특정 작가 필터
     * @param categoryId 카테고리 필터
     * @param sort 정렬 기준
     * @param order 정렬 방향
     * @return 찜한 상품 목록
     */
    @GetMapping("/wishlist")
    @Operation(summary = "찜한 상품 목록 조회")
    public ResponseEntity<RsData<WishlistResponse.List>> getWishlist(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String artistId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "addedAt") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        
        WishlistResponse.List response = dashboardService.getWishlist(
                authorization, page, size, keyword, artistId, categoryId, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "찜한 상품 목록 조회 성공",
                response
        ));
    }
    
    /**
     * 참여한 펀딩 목록 조회
     * 
     * @param authorization Bearer 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param status 펀딩 상태 필터
     * @param keyword 검색 키워드
     * @param sort 정렬 기준
     * @param order 정렬 방향
     * @return 참여한 펀딩 목록
     */
    @GetMapping("/funding")
    @Operation(summary = "참여한 펀딩 목록 조회")
    public ResponseEntity<RsData<FundingResponse.List>> getFundingParticipations(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "endDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {
        
        FundingResponse.List response = dashboardService.getFundingParticipations(
                authorization, page, size, status, keyword, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "참여한 펀딩 목록 조회 성공",
                response
        ));
    }
}
