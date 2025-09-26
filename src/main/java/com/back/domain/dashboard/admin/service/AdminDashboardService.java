package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;

/**
 * 관리자용 대시보드 서비스 인터페이스
 * 2025.09.26 생성
 */
public interface AdminDashboardService {

    /**
     * 관리자 대시보드 전체 현황 조회
     */
    AdminOverviewResponse getOverview(String authorization, String adminRole, String range, 
                                      String granularity, String period, String timezone);
}
