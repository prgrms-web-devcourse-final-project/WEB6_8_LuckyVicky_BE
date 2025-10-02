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
 * <p>
 * 작가가 자신의 상품, 주문, 정산, 펀딩 등을 관리할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * <p>
 * 제공 기능:
 * <ul>
 *   <li>대시보드 메인 현황 조회 (통계, 트렌드, 알림, 유입 경로)</li>
 *   <li>상품 목록 조회</li>
 *   <li>지갑 잔액 및 거래 내역 조회</li>
 *   <li>주문 내역 조회</li>
 *   <li>취소/교환 요청 목록 조회</li>
 *   <li>펀딩 목록 조회</li>
 *   <li>정산 내역 조회</li>
 *   <li>작가 설정 정보 조회</li>
 * </ul>
 * <p>
 * 2025.10.01 GA4 유입 경로 통합 - 메인 현황에 포함
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
     */
    @GetMapping("/main")
    @Operation(summary = "작가 대시보드 메인 현황 조회", 
               description = "작가의 프로필, 통계, 트렌드, 알림, 유입 경로(GA4) 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistMainResponse>> getMainStats(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistMainStatsRequest request) {

        log.info("작가 대시보드 메인 현황 조회 - range: {}, interval: {}", request.range(), request.interval());

        ArtistMainResponse response = artistDashboardService.getMainStats(
                authorization, request.range(), request.from(), request.to(),
                request.interval(), request.tz());

        return ResponseEntity.ok(RsData.ok("작가 대시보드 메인 조회 성공", response));
    }

    /**
     * 작가 상품 목록 조회
     */
    @GetMapping("/products")
    @Operation(summary = "작가 상품 목록 조회", description = "작가가 등록한 상품들을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistProductResponse.List>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistProductSearchRequest request) {

        log.info("작가 상품 목록 조회 - page: {}, size: {}, keyword: {}",
                request.page(), request.size(), request.keyword());

        ArtistProductResponse.List response = artistDashboardService.getProducts(
                authorization, request.page(), request.size(), request.keyword(),
                request.selling(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("내 상품 목록 조회 성공", response));
    }

    /**
     * 작가 지갑 잔액 조회
     */
    @GetMapping("/cash/balance")
    @Operation(summary = "작가 지갑 잔액 조회", description = "작가의 현재 지갑 잔액 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistCashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 지갑 잔액 조회 요청");

        ArtistCashResponse.Balance response = artistDashboardService.getCashBalance(authorization);
        return ResponseEntity.ok(RsData.ok("작가지갑 요약 조회 성공", response));
    }

    /**
     * 작가 캐시 입금/환전 내역 조회
     */
    @GetMapping("/cash/history")
    @Operation(summary = "작가 캐시 입금/환전 내역 조회", description = "작가의 캐시 거래 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistCashHistoryResponse.List>> getCashHistory(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistCashHistorySearchRequest request) {

        log.info("작가 캐시 내역 조회 - page: {}, size: {}, type: {}",
                request.page(), request.size(), request.type());

        ArtistCashHistoryResponse.List response = artistDashboardService.getCashHistory(
                authorization, request.page(), request.size(), request.type(), request.status(),
                request.dateFrom(), request.dateTo(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("입금/환전 내역 조회 성공", response));
    }

    /**
     * 작가 주문 내역 조회
     */
    @GetMapping("/orders")
    @Operation(summary = "작가 주문 내역 조회", description = "작가의 상품 주문 내역을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistOrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistOrderSearchRequest request) {

        log.info("작가 주문 내역 조회 - page: {}, size: {}, status: {}",
                request.page(), request.size(), request.status());

        ArtistOrderResponse.List response = artistDashboardService.getOrders(
                authorization, request.page(), request.size(), request.status(), request.keyword(),
                request.startDate(), request.endDate(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("주문 목록 조회 성공", response));
    }

    /**
     * 작가 취소 요청 목록 조회
     */
    @GetMapping("/requests/cancellations")
    @Operation(summary = "작가 취소 요청 목록 조회", description = "작가 상품에 대한 고객의 취소 요청을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistCancellationResponse.List>> getCancellationRequests(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistCancellationSearchRequest request) {

        log.info("작가 취소 요청 목록 조회 - page: {}, size: {}, status: {}",
                request.page(), request.size(), request.status());

        ArtistCancellationResponse.List response = artistDashboardService.getCancellationRequests(
                authorization, request.page(), request.size(), request.status(), request.keyword(),
                request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("취소 요청 목록 조회 성공", response));
    }

    /**
     * 작가 교환 요청 목록 조회
     */
    @GetMapping("/requests/exchanges")
    @Operation(summary = "작가 교환 요청 목록 조회", description = "작가 상품에 대한 고객의 교환 요청을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistExchangeResponse.List>> getExchangeRequests(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistExchangeSearchRequest request) {

        log.info("작가 교환 요청 목록 조회 - page: {}, size: {}, status: {}",
                request.page(), request.size(), request.status());

        ArtistExchangeResponse.List response = artistDashboardService.getExchangeRequests(
                authorization, request.page(), request.size(), request.status(), request.keyword(),
                request.startDate(), request.endDate(), request.productId(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("교환 요청 목록 조회 성공", response));
    }

    /**
     * 작가 설정 정보 조회
     */
    @GetMapping("/settings")
    @Operation(summary = "작가 설정 정보 조회", description = "작가의 프로필, 사업자, 정산 계좌 등 설정 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistSettingsResponse>> getSettings(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 설정 정보 조회 요청");

        ArtistSettingsResponse response = artistDashboardService.getSettings(authorization);
        return ResponseEntity.ok(RsData.ok("판매자 설정 조회 성공", response));
    }

    /**
     * 작가 펀딩 목록 조회
     */
    @GetMapping("/funding")
    @Operation(summary = "작가 펀딩 목록 조회", description = "작가가 진행한 펀딩들을 페이지 단위로 조회합니다")
    public ResponseEntity<RsData<ArtistFundingResponse.List>> getFundings(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistFundingSearchRequest request) {

        log.info("작가 펀딩 목록 조회 - page: {}, size: {}, status: {}",
                request.page(), request.size(), request.status());

        ArtistFundingResponse.List response = artistDashboardService.getFundings(
                authorization, request.page(), request.size(), request.keyword(), request.status(),
                request.categoryId(), request.minAchievement(), request.maxAchievement(),
                request.startDate(), request.endDate(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("내 펀딩 모니터링 조회 성공", response));
    }

    /**
     * 작가 정산내역 조회
     */
    @GetMapping("/settlements")
    @Operation(summary = "작가 정산내역 조회", description = "작가의 정산 내역을 조회하며, 월별/일별 차트와 상세 테이블을 제공합니다")
    public ResponseEntity<RsData<ArtistSettlementResponse>> getSettlements(
            @RequestHeader("Authorization") String authorization,
            @Valid @ModelAttribute ArtistSettlementSearchRequest request) {

        log.info("작가 정산내역 조회 - year: {}, month: {}, granularity: {}, page: {}, size: {}",
                request.year(), request.month(), request.granularity(), request.page(), request.size());

        ArtistSettlementResponse response = artistDashboardService.getSettlements(
                authorization, request.year(), request.month(), request.granularity(), request.status(),
                request.productId(), request.page(), request.size(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("정산 내역 조회 성공", response));
    }
}
