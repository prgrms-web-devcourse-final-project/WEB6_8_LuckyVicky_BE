package com.back.domain.dashboard.customer.controller;

import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.dashboard.customer.service.DashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * 고객용 대시보드 컨트롤러
 * 
 * 고객이 자신의 계정 정보, 주문 내역, 작가 신청 현황 등을 조회할 수 있는
 * 대시보드 기능을 제공합니다. 모든 API는 JWT 인증이 필요합니다. (추후 인증/인가 되면 추가)
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
 *  2025.09.20 수정
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "CustomerDashboardController", description = "고객용 대시보드 컨트롤러")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 계정 설정 조회
     * 사용자의 계정 설정 정보를 조회합니다. include 파라미터를 통해
     * 필요한 정보만 선택적으로 가져올 수 있습니다.
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
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "계정 설정 조회 성공",
                response
        ));
    }
    
    /**
     * 작가 신청 내역 목록 조회
     * 사용자가 신청한 작가 입점 신청 내역을 페이지 단위로 조회합니다.
     * 상태별 필터링과 날짜 범위 검색이 가능합니다.
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param status 상태 필터 (PENDING, APPROVED, REJECTED 중 선택, 선택사항)
     * @param startDate 신청일 시작 날짜 (yyyy-MM-dd 형식, 선택사항)
     * @param endDate 신청일 종료 날짜 (yyyy-MM-dd 형식, 선택사항)
     * @param sort 정렬 기준 (applicationId, artistName, submittedAt, status 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 작가 신청 내역 목록 (페이징 정보 포함)
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 페이지 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "작가 신청 내역 목록 조회", description = "작가 입점 신청 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistApplicationResponse.List>> getArtistApplications(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다") 
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) 
            @Pattern(regexp = "^(PENDING|APPROVED|REJECTED)$", message = "status는 PENDING, APPROVED, REJECTED 중 하나여야 합니다")
            String status,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String startDate,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String endDate,
            @RequestParam(defaultValue = "submittedAt") 
            @Pattern(regexp = "^(applicationId|artistName|submittedAt|status)$", 
                    message = "sort는 applicationId, artistName, submittedAt, status 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
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
     * 특정 작가 입점 신청의 상세 정보를 조회합니다.
     * 신청서 내용, 첨부 파일, 심사 결과 등을 포함합니다.
     * @param authorization Bearer 토큰 (필수)
     * @param applicationId 신청 ID (필수)
     * @return 입점 신청 상세 정보
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws NoSuchElementException 해당 신청 ID가 존재하지 않는 경우
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
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "입점 신청 상세 조회 성공",
                response
        ));
    }
    
    /**
     * 주문 목록 조회
     * 사용자의 주문 내역을 페이지 단위로 조회합니다.
     * 주문 상태별 필터링과 기간별 검색이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param status 주문 상태 필터 (PENDING, PREPARING, SHIPPED, DELIVERED, CANCELED 중 선택)
     * @param period 기간 필터 (TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM 중 선택)
     * @param sort 정렬 기준 (orderDate, orderNumber, status, totalAmount 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 주문 목록 (페이징 정보 포함)
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/orders")
    @Operation(summary = "주문 목록 조회", description = "사용자의 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<OrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다") 
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) 
            @Pattern(regexp = "^(PENDING|PREPARING|SHIPPED|DELIVERED|CANCELED)$", 
                    message = "status는 PENDING, PREPARING, SHIPPED, DELIVERED, CANCELED 중 하나여야 합니다")
            String status,
            @RequestParam(defaultValue = "MONTH") 
            @Pattern(regexp = "^(TODAY|WEEK|MONTH|QUARTER|YEAR|CUSTOM)$", 
                    message = "period는 TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM 중 하나여야 합니다")
            String period,
            @RequestParam(defaultValue = "orderDate") 
            @Pattern(regexp = "^(orderDate|orderNumber|status|totalAmount)$", 
                    message = "sort는 orderDate, orderNumber, status, totalAmount 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
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
     * 사용자가 팔로우하고 있는 작가들의 목록을 페이지 단위로 조회합니다.
     * 작가명 검색과 다양한 정렬 기준을 지원합니다.
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param keyword 검색 키워드 (작가ID/작가명, 선택사항)
     * @param sort 정렬 기준 (followedAt, artistName, followerCount, lastPublishedAt 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 팔로우한 작가 목록 (페이징 정보 포함)
     *
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/following-artists")
    @Operation(summary = "내가 팔로우한 작가 목록 조회", description = "팔로우하고 있는 작가들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FollowingResponse.List>> getFollowingArtists(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다") 
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "followedAt") 
            @Pattern(regexp = "^(followedAt|artistName|followerCount|lastPublishedAt)$", 
                    message = "sort는 followedAt, artistName, followerCount, lastPublishedAt 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
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
     * 사용자가 찜한 상품들의 목록을 페이지 단위로 조회합니다.
     * 상품명/작가명 검색, 특정 작가/카테고리 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param keyword 검색 키워드 (상품명/작가명, 선택사항)
     * @param artistId 특정 작가 필터 (선택사항)
     * @param categoryId 카테고리 필터 (선택사항)
     * @param sort 정렬 기준 (addedAt, productName, artistName, price 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 찜한 상품 목록 (페이징 정보 포함)
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/wishlist")
    @Operation(summary = "찜한 상품 목록 조회", description = "찜한 상품들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<WishlistResponse.List>> getWishlist(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다") 
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String artistId,
            @RequestParam(required = false) 
            @Min(value = 1, message = "카테고리 ID는 1 이상이어야 합니다")
            Long categoryId,
            @RequestParam(defaultValue = "addedAt") 
            @Pattern(regexp = "^(addedAt|productName|artistName|price)$", 
                    message = "sort는 addedAt, productName, artistName, price 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
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
     * 사용자가 참여한 펀딩들의 목록을 페이지 단위로 조회합니다.
     * 펀딩 상태별 필터링과 제목/작가명 검색이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param status 펀딩 상태 필터 (ACTIVE, SUCCESS, FAILED, CANCELED, REFUNDED 중 선택)
     * @param keyword 검색 키워드 (펀딩 제목/작가명, 선택사항)
     * @param sort 정렬 기준 (endDate, achievementRate, pledgedAt, title, artistName 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 참여한 펀딩 목록 (페이징 정보 포함)
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/funding")
    @Operation(summary = "참여한 펀딩 목록 조회", description = "참여한 펀딩들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<FundingResponse.List>> getFundingParticipations(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다") 
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) 
            @Pattern(regexp = "^(ACTIVE|SUCCESS|FAILED|CANCELED|REFUNDED)$", 
                    message = "status는 ACTIVE, SUCCESS, FAILED, CANCELED, REFUNDED 중 하나여야 합니다")
            String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "endDate") 
            @Pattern(regexp = "^(endDate|achievementRate|pledgedAt|title|artistName)$", 
                    message = "sort는 endDate, achievementRate, pledgedAt, title, artistName 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
        FundingResponse.List response = dashboardService.getFundingParticipations(
                authorization, page, size, status, keyword, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "참여한 펀딩 목록 조회 성공",
                response
        ));
    }
}
