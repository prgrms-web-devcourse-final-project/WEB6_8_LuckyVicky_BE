package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;

/**
 * 작가용 대시보드 서비스 인터페이스
 */
public interface ArtistDashboardService {
    
    /**
     * 작가 대시보드 메인 현황 조회
     */
    ArtistMainResponse getMainStats(String authorization, String range, String from, String to, 
                                   String interval, String tz);
}
