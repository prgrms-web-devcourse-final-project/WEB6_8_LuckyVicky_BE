package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationResponse;
import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;

/**
 * 관리자용 대시보드 서비스 인터페이스
 * 2025.09.28 수정
 */
public interface AdminDashboardService {

    /**
     * 관리자 대시보드 전체 현황 조회
     */
    AdminOverviewResponse getOverview(String authorization, String adminRole, String range, 
                                      String granularity, String period, String timezone);

    /**
     * 관리자 상품 목록 조회
     */
    AdminProductResponse getProducts(String authorization, String adminRole, int page, int size,
                                     String keyword, String sellingStatus, Long categoryId, Long artistId,
                                     String startDate, String endDate, String sort, String order, boolean metrics);

    /**
     * 관리자 사용자 목록 조회
     */
    AdminUserResponse getUsers(String authorization, String adminRole, int page, int size,
                               String keyword, String role, String accountStatus, String grade,
                               String joinedStartDate, String joinedEndDate, Long artistId,
                               String sort, String order);

    /**
     * 관리자 매출/정산 집계 조회
     */
    AdminSettlementResponse getSettlements(String authorization, String adminRole, Integer year,
                                           Integer month, String granularity, String timezone);

    /**
     * 관리자 펀딩 모니터링 목록 조회
     */
    AdminFundingResponse getFundings(String authorization, String adminRole, int page, int size,
                                     String keyword, String status, Long categoryId, Long artistId,
                                     Integer minAchievement, Integer maxAchievement,
                                     String registeredFrom, String registeredTo,
                                     String dueFrom, String dueTo, String sort, String order);

    /**
     * 관리자 입점 신청 목록 조회
     */
    AdminArtistApplicationResponse getArtistApplications(String authorization, String adminRole, int page, int size,
                                                         String keyword, String status,
                                                         String submittedFrom, String submittedTo,
                                                         String sort, String order);
}
