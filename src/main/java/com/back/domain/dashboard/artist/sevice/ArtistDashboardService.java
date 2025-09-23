package com.back.domain.dashboard.artist.sevice;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;

/**
 * 작가용 대시보드 서비스 인터페이스
 * 2025.09.23 생성
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
}