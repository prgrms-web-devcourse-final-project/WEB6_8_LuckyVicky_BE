package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistOrderResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistExchangeResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistFundingResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettlementResponse;

/**
 * 작가용 대시보드 서비스 인터페이스
 * 2025.09.25 Request DTO 패턴 적용
 */
public interface ArtistDashboardService {

    /**
     * 작가 대시보드 메인 현황 조회
     */
    ArtistMainResponse getMainStats(String authorization, String range, String from, String to,
                                    String interval, String tz);

    /**
     * 작가 상품 목록 조회
     */
    ArtistProductResponse.List getProducts(String authorization, int page, int size, String keyword,
                                           Boolean selling, String sort, String order);

    /**
     * 작가 지갑 잔액 조회
     */
    ArtistCashResponse.Balance getCashBalance(String authorization);

    /**
     * 작가 캐시 입금/환전 내역 조회
     */
    ArtistCashHistoryResponse.List getCashHistory(String authorization, int page, int size,
                                                  String type, String status, String dateFrom,
                                                  String dateTo, String sort, String order);

    /**
     * 작가 주문 내역 조회
     */
    ArtistOrderResponse.List getOrders(String authorization, int page, int size,
                                       String status, String keyword, String startDate,
                                       String endDate, String sort, String order);

    /**
     * 작가 취소 요청 목록 조회
     */
    ArtistCancellationResponse.List getCancellationRequests(String authorization, int page, int size,
                                                            String status, String keyword, String startDate,
                                                            String endDate, Long productId, String sort, String order);

    /**
     * 작가 교환 요청 목록 조회
     */
    ArtistExchangeResponse.List getExchangeRequests(String authorization, int page, int size,
                                                    String status, String keyword, String startDate,
                                                    String endDate, Long productId, String sort, String order);

    /**
     * 작가 설정 정보 조회
     */
    ArtistSettingsResponse getSettings(String authorization);

    /**
     * 작가 펀딩 목록 조회
     */
    ArtistFundingResponse.List getFundings(String authorization, int page, int size, String keyword,
                                           String status, Long categoryId, Integer minAchievement, Integer maxAchievement,
                                           String startDate, String endDate, String sort, String order);

    /**
     * 작가 정산내역 조회
     */
    ArtistSettlementResponse getSettlements(String authorization, Integer year, Integer month, String granularity,
                                            String status, Long productId, int page, int size, String sort, String order);
}
