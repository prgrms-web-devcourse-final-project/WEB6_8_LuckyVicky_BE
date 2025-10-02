package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.request.*;
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
import com.back.domain.dashboard.artist.dto.response.ArtistTrafficSourceResponse;

/**
 * 작가용 대시보드 서비스 인터페이스
 * 2025.10.01 GA4 유입 경로 통합 - 메인 현황에 포함
 * 2025.10.02 JWT 표준 패턴 적용 - Request DTO 사용
 */
public interface ArtistDashboardService {

    /**
     * 작가 대시보드 메인 현황 조회 (유입 경로 포함)
     */
    ArtistMainResponse getMainStats(Long artistId, ArtistMainStatsRequest request);

    /**
     * 작가 상품 목록 조회
     */
    ArtistProductResponse.List getProducts(Long artistId, ArtistProductSearchRequest request);

    /**
     * 작가 지갑 잔액 조회
     */
    ArtistCashResponse.Balance getCashBalance(Long artistId);

    /**
     * 작가 캐시 입금/환전 내역 조회
     */
    ArtistCashHistoryResponse.List getCashHistory(Long artistId, ArtistCashHistorySearchRequest request);

    /**
     * 작가 주문 내역 조회
     */
    ArtistOrderResponse.List getOrders(Long artistId, ArtistOrderSearchRequest request);

    /**
     * 작가 취소 요청 목록 조회
     */
    ArtistCancellationResponse.List getCancellationRequests(Long artistId, ArtistCancellationSearchRequest request);

    /**
     * 작가 교환 요청 목록 조회
     */
    ArtistExchangeResponse.List getExchangeRequests(Long artistId, ArtistExchangeSearchRequest request);

    /**
     * 작가 설정 정보 조회
     */
    ArtistSettingsResponse getSettings(Long artistId);

    /**
     * 작가 펀딩 목록 조회
     */
    ArtistFundingResponse.List getFundings(Long artistId, ArtistFundingSearchRequest request);

    /**
     * 작가 정산내역 조회
     */
    ArtistSettlementResponse getSettlements(Long artistId, ArtistSettlementSearchRequest request);

    /**
     * 작가 유입 경로 분석 조회 (GA4) - 내부 사용 전용
     * getMainStats()에서 내부적으로 호출됨
     */
    ArtistTrafficSourceResponse getTrafficSources(Long artistId, int days, String timezone);
}
