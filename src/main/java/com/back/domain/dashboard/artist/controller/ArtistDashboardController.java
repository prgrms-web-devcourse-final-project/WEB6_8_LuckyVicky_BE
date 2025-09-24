package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistOrderResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistExchangeResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistFundingResponse;
import com.back.domain.dashboard.artist.service.ArtistDashboardService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 작가 대시보드 컨트롤러
 * 2025.09.23 생성
 */
@RestController
@RequestMapping("/api/dashboard/artist")
@RequiredArgsConstructor
@Slf4j
public class ArtistDashboardController {

    private final ArtistDashboardService artistDashboardService;

    /**
     * 작가 대시보드 메인 현황 조회
     */
    @GetMapping("/main")
    public ResponseEntity<RsData<ArtistMainResponse>> getMainStats(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "6M") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "AUTO") String interval,
            @RequestParam(defaultValue = "Asia/Seoul") String tz) {

        log.info("작가 대시보드 메인 현황 조회 요청 - range: {}, from: {}, to: {}, interval: {}, tz: {}",
                range, from, to, interval, tz);

        try {
            ArtistMainResponse response = artistDashboardService.getMainStats(
                    authorization, range, from, to, interval, tz);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "작가 대시보드 메인 조회 성공", response)
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
     */
    @GetMapping("/products")
    public ResponseEntity<RsData<ArtistProductResponse.List>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean selling,
            @RequestParam(defaultValue = "registrationDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {

        log.info("작가 상품 목록 조회 요청 - page: {}, size: {}, keyword: {}, selling: {}, sort: {}, order: {}",
                page, size, keyword, selling, sort, order);

        try {
            ArtistProductResponse.List response = artistDashboardService.getProducts(
                    authorization, page, size, keyword, selling, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "내 상품 목록 조회 성공", response)
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
     */
    @GetMapping("/cash/balance")
    public ResponseEntity<RsData<ArtistCashResponse.Balance>> getCashBalance(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 지갑 잔액 조회 요청");

        try {
            ArtistCashResponse.Balance response = artistDashboardService.getCashBalance(authorization);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "작가지갑 요약 조회 성공", response)
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
     */
    @GetMapping("/cash/history")
    public ResponseEntity<RsData<ArtistCashHistoryResponse.List>> getCashHistory(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "transactedAt") String sort,
            @RequestParam(defaultValue = "DESC") String order) {

        log.info("작가 캐시 내역 조회 요청 - page: {}, size: {}, type: {}, status: {}, dateFrom: {}, dateTo: {}, sort: {}, order: {}",
                page, size, type, status, dateFrom, dateTo, sort, order);

        try {
            ArtistCashHistoryResponse.List response = artistDashboardService.getCashHistory(
                    authorization, page, size, type, status, dateFrom, dateTo, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "입금/환전 내역 조회 성공", response)
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
     */
    @GetMapping("/orders")
    public ResponseEntity<RsData<ArtistOrderResponse.List>> getOrders(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "orderDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {

        log.info("작가 주문 내역 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, sort: {}, order: {}",
                page, size, status, keyword, startDate, endDate, sort, order);

        try {
            ArtistOrderResponse.List response = artistDashboardService.getOrders(
                    authorization, page, size, status, keyword, startDate, endDate, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "주문 목록 조회 성공", response)
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
     */
    @GetMapping("/requests/cancellations")
    public ResponseEntity<RsData<ArtistCancellationResponse.List>> getCancellationRequests(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "requestDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {

        log.info("작가 취소 요청 목록 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, productId: {}, sort: {}, order: {}",
                page, size, status, keyword, startDate, endDate, productId, sort, order);

        try {
            ArtistCancellationResponse.List response = artistDashboardService.getCancellationRequests(
                    authorization, page, size, status, keyword, startDate, endDate, productId, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "취소 요청 목록 조회 성공", response)
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
     */
    @GetMapping("/requests/exchanges")
    public ResponseEntity<RsData<ArtistExchangeResponse.List>> getExchangeRequests(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "requestDate") String sort,
            @RequestParam(defaultValue = "DESC") String order) {

        log.info("작가 교환 요청 목록 조회 요청 - page: {}, size: {}, status: {}, keyword: {}, startDate: {}, endDate: {}, productId: {}, sort: {}, order: {}",
                page, size, status, keyword, startDate, endDate, productId, sort, order);

        try {
            ArtistExchangeResponse.List response = artistDashboardService.getExchangeRequests(
                    authorization, page, size, status, keyword, startDate, endDate, productId, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "교환 요청 목록 조회 성공", response)
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
     */
    @GetMapping("/settings")
    public ResponseEntity<RsData<ArtistSettingsResponse>> getSettings(
            @RequestHeader("Authorization") String authorization) {

        log.info("작가 설정 정보 조회 요청");

        try {
            ArtistSettingsResponse response = artistDashboardService.getSettings(authorization);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "판매자 설정 조회 성공", response)
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
     */
    @GetMapping("/funding")
    public ResponseEntity<RsData<ArtistFundingResponse.List>> getFundings(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minAchievement,
            @RequestParam(required = false) Integer maxAchievement,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "endDate") String sort,
            @RequestParam(defaultValue = "ASC") String order) {

        log.info("작가 펀딩 목록 조회 요청 - page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, " +
                        "minAchievement: {}, maxAchievement: {}, startDate: {}, endDate: {}, sort: {}, order: {}",
                page, size, keyword, status, categoryId, minAchievement, maxAchievement,
                startDate, endDate, sort, order);

        try {
            ArtistFundingResponse.List response = artistDashboardService.getFundings(
                    authorization, page, size, keyword, status, categoryId, minAchievement, maxAchievement,
                    startDate, endDate, sort, order);

            return ResponseEntity.ok(
                    RsData.of("200-OK", "내 펀딩 모니터링 조회 성공", response)
            );
        } catch (Exception e) {
            log.error("작가 펀딩 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    RsData.of("500-ERROR", "서버 오류가 발생했습니다.")
            );
        }
    }
}