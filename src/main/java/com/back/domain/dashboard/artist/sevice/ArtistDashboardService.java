package com.back.domain.dashboard.artist.sevice;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;

/**
 * 작가용 대시보드 서비스 인터페이스
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
}