package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.service.ArtistDashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 작가용 대시보드 컨트롤러
 * 추후 도메인 되면 GA 사용법 적용하기.
 * 작가가 자신의 판매 현황, 통계, 트렌드 등을 조회할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * 
 * 제공 기능:
 * <ul>
 *   <li>메인 현황 조회 (프로필, 통계, 트렌드, 알림)</li>
 *   <li>매출/주문/팔로워 트렌드 분석</li>
 *   <li>실시간 알림 확인</li>
 * </ul>
 * 2025.09.22 생성
 */
@RestController
@RequestMapping("/api/dashboard/artist")
@RequiredArgsConstructor
@Tag(name = "ArtistDashboardController", description = "작가용 대시보드 컨트롤러")
public class ArtistDashboardController {
    
    private final ArtistDashboardService artistDashboardService;
    
    /**
     * 작가 대시보드 메인 현황 조회
     * 작가의 프로필, 통계, 트렌드, 알림 정보를 조회
     * 기간별 필터링과 트렌드 분석이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param range 기간 범위 (1M, 3M, 6M, 1Y, ALL 중 선택, 기본값: 1M)
     * @param from 시작일 (yyyy-MM-dd 형식, 직접 범위 지정 시 사용)
     * @param to 종료일 (yyyy-MM-dd 형식, 직접 범위 지정 시 사용)
     * @param interval 트렌드 간격 (AUTO, DAY, WEEK, MONTH 중 선택, 기본값: AUTO)
     * @param tz 타임존 (기본값: Asia/Seoul)
     * @return 작가 대시보드 메인 현황 정보
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/main")
    @Operation(summary = "작가 대시보드 메인 현황 조회", description = "작가의 프로필, 통계, 트렌드, 알림 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistMainResponse>> getMainStats(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1M") 
            @Pattern(regexp = "^(1M|3M|6M|1Y|ALL)$", 
                    message = "range는 1M, 3M, 6M, 1Y, ALL 중 하나여야 합니다")
            String range,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String from,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String to,
            @RequestParam(defaultValue = "AUTO") 
            @Pattern(regexp = "^(AUTO|DAY|WEEK|MONTH)$", 
                    message = "interval은 AUTO, DAY, WEEK, MONTH 중 하나여야 합니다")
            String interval,
            @RequestParam(defaultValue = "Asia/Seoul") String tz) {
        
        ArtistMainResponse response = artistDashboardService.getMainStats(
                authorization, range, from, to, interval, tz);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "작가 대시보드 메인 조회 성공",
                response
        ));
    }
    
    /**
     * 작가 상품 목록 조회
     * 작가가 등록한 상품들의 목록을 페이지 단위로 조회
     * 상품명/번호 검색, 판매 상태 필터링, 다양한 정렬 기준을 지원
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (1-100, 기본값: 10)
     * @param keyword 검색 키워드 (상품명/상품번호, 선택사항)
     * @param selling 판매 상태 필터 (null=전체, true=판매중만, false=중지만)
     * @param sort 정렬 기준 (productNumber, productName, price, sellingStatus, registrationDate 중 선택)
     * @param order 정렬 방향 (ASC, DESC 중 선택)
     * @return 작가 상품 목록 (페이징 정보 포함)
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/products")
    @Operation(summary = "작가 상품 목록 조회", description = "작가가 등록한 상품들의 목록을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistProductResponse.List>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") 
            @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
            int page,
            @RequestParam(defaultValue = "10") 
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean selling,
            @RequestParam(defaultValue = "registrationDate") 
            @Pattern(regexp = "^(productNumber|productName|price|sellingStatus|registrationDate)$", 
                    message = "sort는 productNumber, productName, price, sellingStatus, registrationDate 중 하나여야 합니다")
            String sort,
            @RequestParam(defaultValue = "DESC") 
            @Pattern(regexp = "^(ASC|DESC)$", message = "order는 ASC 또는 DESC여야 합니다")
            String order) {
        
        ArtistProductResponse.List response = artistDashboardService.getProducts(
                authorization, page, size, keyword, selling, sort, order);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "내 상품 목록 조회 성공",
                response
        ));
    }
    
    /**
     * 작가 지갑 잔액 조회
     * 작가의 현재 지갑 잔액과 관련 정보를 조회
     * 현재 잔액, 정산 대기 금액, 환전 처리 중인 금액, 환전 가능 금액을 포함
     * 
     * @param authorization Bearer 토큰 (필수)
     * @return 작가 지갑 잔액 정보
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     */
    @GetMapping("/cash")
    @Operation(summary = "작가 지갑 잔액 조회", description = "작가의 현재 지갑 잔액과 관련 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistCashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {
        
        ArtistCashResponse.Balance response = artistDashboardService.getCashBalance(authorization);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "작가지갑 요약 조회 성공",
                response
        ));
    }
}
