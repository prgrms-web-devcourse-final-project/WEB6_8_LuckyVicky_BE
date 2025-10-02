package com.back.domain.dashboard.admin.service;

<<<<<<< HEAD
import com.back.domain.dashboard.admin.dto.request.*;
=======
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
import com.back.domain.dashboard.admin.dto.response.*;

/**
 * 관리자용 대시보드 서비스 인터페이스
 * 2025.10.01 GA4 유입 경로 통합 - 메인 현황에 포함
<<<<<<< HEAD
 * 2025.10.02 JWT 표준 패턴 적용 - Request DTO 사용, SecurityContext 활용
=======
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
 */
public interface AdminDashboardService {

    /**
     * 관리자 대시보드 전체 현황 조회 (유입 경로 포함)
     */
    AdminOverviewResponse getOverview(AdminOverviewRequest request);

    /**
     * 관리자 상품 목록 조회
     */
    AdminProductResponse getProducts(AdminProductSearchRequest request);

    /**
     * 관리자 사용자 목록 조회
     */
    AdminUserResponse getUsers(AdminUserSearchRequest request);

    /**
     * 관리자 매출/정산 집계 조회
     */
    AdminSettlementResponse getSettlements(AdminSettlementRequest request);

    /**
     * 관리자 펀딩 모니터링 목록 조회
     */
    AdminFundingResponse getFundings(AdminFundingSearchRequest request);

    /**
     * 관리자 입점 신청 목록 조회
     */
    AdminArtistApplicationResponse getArtistApplications(AdminArtistApplicationSearchRequest request);

    /**
     * 관리자 입점 신청 상세 조회
     */
<<<<<<< HEAD
    AdminArtistApplicationDetailResponse getArtistApplicationDetail(Long applicationId);
=======
    AdminArtistApplicationDetailResponse getArtistApplicationDetail(String authorization, String adminRole, Long applicationId);
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98

    /**
     * 관리자 유입 경로 분석 조회 (GA4) - 내부 사용 전용
     * getOverview()에서 내부적으로 호출됨
     */
<<<<<<< HEAD
    AdminTrafficSourceResponse getTrafficSources(int days, String timezone);
=======
    AdminTrafficSourceResponse getTrafficSources(String authorization, String adminRole, int days, String timezone);
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
}
