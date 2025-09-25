package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.request.*;
import com.back.domain.dashboard.artist.dto.response.*;
import com.back.domain.dashboard.artist.service.ArtistDashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 작가 대시보드 컨트롤러
 * 
 * 작가가 자신의 상품, 주문, 정산, 펀딩 등을 관리할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * 
 * 제공 기능:
 * <ul>
 *   <li>대시보드 메인 현황 조회 (통계, 트렌드, 알림)</li>
 *   <li>상품 목록 조회</li>
 *   <li>지갑 잔액 및 거래 내역 조회</li>
 *   <li>주문 내역 조회</li>
 *   <li>취소/교환 요청 목록 조회</li>
 *   <li>펀딩 목록 조회</li>
 *   <li>작가 설정 정보 조회</li>
 * </ul>
 * 
 * 2025.09.25 Request DTO 패턴 적용
 */
@RestController
@RequestMapping("/api/dashboard/artist")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ArtistDashboardController", description = "작가 대시보드 컨트롤러")
public class ArtistDashboardController {

    private final ArtistDashboardService artistDashboardService;

    /**
     * 작가 대시보드 메인 현황 조회
     * 작가의 프로필, 통계, 트렌드, 알림 정보를 조회합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 통계 조회 조건 (기간, 간격, 타임존)
     * @return 작가 대시보드 메인 현황 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 조회 조건이 유효하지 않은 경우
     */
    @GetMapping("/main")
    @Operation(summary = "작가 대시보드 메인 현황 조회", description = "작가의 프로필, 통계, 트렌드, 알림 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistMainResponse>> getMainStats(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistMainStatsRequest request) {

        log.info("작가 대시보드 메인 현황 조회 요청 - range: {}, from: {}, to: {}, interval: {}, tz: {}",
                request.range(), request.from(), request.to(), request.interval(), request.tz());

        try {
            ArtistMainResponse response = artistDashboardService.getMainStats(
                    authorization, request.range(), request.from(), request.to(), 
                    request.interval(), request.tz());

            return ResponseEntity.ok(
                    RsData.ok("작가 대시보드 메인 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 대시보드 메인 현황 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 상품 목록 조회
     * 작가가 등록한 상품들을 페이지 단위로 조회합니다.
     * 키워드 검색, 판매 상태 필터링, 다양한 정렬이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 상품 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 상품 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/products")
    @Operation(summary = "작가 상품 목록 조회", description = "작가가 등록한 상품들을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistProductResponse.List>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistProductSearchRequest request) {

        log.info("작가 상품 목록 조회 요청 - page: {}, size: {}, keyword: {}, selling: {}, sort: {}, order: {}",
                request.page(), request.size(), request.keyword(), request.selling(), 
                request.sort(), request.order());

        try {
            ArtistProductResponse.List response = artistDashboardService.getProducts(
                    authorization, request.page(), request.size(), request.keyword(), 
                    request.selling(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("내 상품 목록 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 상품 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 지갑 잔액 조회
     * 작가의 현재 지갑 잔액 정보를 조회합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @return 작가 지갑 잔액 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     */
    @GetMapping("/cash/balance")
    @Operation(summary = "작가 지갑 잔액 조회", description = "작가의 현재 지갑 잔액 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistCashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 지갑 잔액 조회 요청");

        try {
            ArtistCashResponse.Balance response = artistDashboardService.getCashBalance(authorization);

            return ResponseEntity.ok(
                    RsData.ok("작가지갑 요약 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 지갑 잔액 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 캐시 입금/환전 내역 조회
     * 작가의 캐시 거래 내역을 페이지 단위로 조회합니다.
     * 거래 유형, 상태, 기간별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 캐시 내역 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 캐시 거래 내역 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/cash/history")
    @Operation(summary = "작가 캐시 입금/환전 내역 조회", description = "작가의 캐시 거래 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistCashHistoryResponse.List>> getCashHistory(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistCashHistorySearchRequest request) {

        log.info("작가 캐시 내역 조회 요청 - page: {}, size: {}, type: {}, status: {}, dateFrom: {}, dateTo: {}, sort: {}, order: {}",
                request.page(), request.size(), request.type(), request.status(), 
                request.dateFrom(), request.dateTo(), request.sort(), request.order());

        try {
            ArtistCashHistoryResponse.List response = artistDashboardService.getCashHistory(
                    authorization, request.page(), request.size(), request.type(), request.status(), 
                    request.dateFrom(), request.dateTo(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("입금/환전 내역 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 캐시 내역 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 주문 내역 조회
     * 작가의 상품 주문 내역을 페이지 단위로 조회합니다.
     * 주문 상태, 키워드, 기간별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 주문 내역 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 주문 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/orders")
    @Operation(summary = "작가 주문 내역 조회", description = "작가의 상품 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistOrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistOrderSearchRequest request) {

        log.info("작가 주문 내역 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, sort: {}, order: {}",
                request.page(), request.size(), request.status(), request.keyword(), 
                request.startDate(), request.endDate(), request.sort(), request.order());

        try {
            ArtistOrderResponse.List response = artistDashboardService.getOrders(
                    authorization, request.page(), request.size(), request.status(), request.keyword(), 
                    request.startDate(), request.endDate(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("주문 목록 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 주문 내역 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 취소 요청 목록 조회
     * 작가 상품에 대한 고객의 취소 요청을 페이지 단위로 조회합니다.
     * 상태, 키워드, 기간, 상품별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 취소 요청 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 취소 요청 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/requests/cancellations")
    @Operation(summary = "작가 취소 요청 목록 조회", description = "작가 상품에 대한 고객의 취소 요청을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistCancellationResponse.List>> getCancellationRequests(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistCancellationSearchRequest request) {

        log.info("작가 취소 요청 목록 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, productId: {}, sort: {}, order: {}",
                request.page(), request.size(), request.status(), request.keyword(), 
                request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

        try {
            ArtistCancellationResponse.List response = artistDashboardService.getCancellationRequests(
                    authorization, request.page(), request.size(), request.status(), request.keyword(), 
                    request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("취소 요청 목록 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 취소 요청 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 교환 요청 목록 조회
     * 작가 상품에 대한 고객의 교환 요청을 페이지 단위로 조회합니다.
     * 상태, 키워드, 기간, 상품별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 교환 요청 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 교환 요청 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/requests/exchanges")
    @Operation(summary = "작가 교환 요청 목록 조회", description = "작가 상품에 대한 고객의 교환 요청을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistExchangeResponse.List>> getExchangeRequests(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistExchangeSearchRequest request) {

        log.info("작가 교환 요청 목록 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, productId: {}, sort: {}, order: {}",
                request.page(), request.size(), request.status(), request.keyword(), 
                request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

        try {
            ArtistExchangeResponse.List response = artistDashboardService.getExchangeRequests(
                    authorization, request.page(), request.size(), request.status(), request.keyword(), 
                    request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("교환 요청 목록 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 교환 요청 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 설정 정보 조회
     * 작가의 프로필, 사업자, 정산 계좌 등 설정 정보를 조회합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @return 작가 설정 정보
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     */
    @GetMapping("/settings")
    @Operation(summary = "작가 설정 정보 조회", description = "작가의 프로필, 사업자, 정산 계좌 등 설정 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistSettingsResponse>> getSettings(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 설정 정보 조회 요청");

        try {
            ArtistSettingsResponse response = artistDashboardService.getSettings(authorization);

            return ResponseEntity.ok(
                    RsData.ok("판매자 설정 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 설정 정보 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 작가 펀딩 목록 조회
     * 작가가 진행한 펀딩들을 페이지 단위로 조회합니다.
     * 키워드, 상태, 카테고리, 달성률, 기간별 필터링이 가능합니다.
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param request 펀딩 목록 검색 조건 (페이징, 필터링, 정렬)
     * @return 작가 펀딩 목록 (페이징 정보 포함)
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 검색 조건이 유효하지 않은 경우
     */
    @GetMapping("/funding")
    @Operation(summary = "작가 펀딩 목록 조회", description = "작가가 진행한 펀딩들을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistFundingResponse.List>> getFundings(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistFundingSearchRequest request) {

        log.info("작가 펀딩 목록 조회 요청 - page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, " +
                        "minAchievement: {}, maxAchievement: {}, startDate: {}, endDate: {}, sort: {}, order: {}",
                request.page(), request.size(), request.keyword(), request.status(), request.categoryId(), 
                request.minAchievement(), request.maxAchievement(), request.startDate(), request.endDate(), 
                request.sort(), request.order());

        try {
            ArtistFundingResponse.List response = artistDashboardService.getFundings(
                    authorization, request.page(), request.size(), request.keyword(), request.status(), 
                    request.categoryId(), request.minAchievement(), request.maxAchievement(),
                    request.startDate(), request.endDate(), request.sort(), request.order());

            return ResponseEntity.ok(
                    RsData.ok("내 펀딩 모니터링 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 펀딩 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }
}
