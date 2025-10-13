package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.request.*;
import com.back.domain.dashboard.admin.dto.response.*;

/**
 * 관리자용 대시보드 서비스 인터페이스
 * 2025.10.04 수정 - GA4 유입 경로 제거 (작가 대시보드 전용)
 */
public interface AdminDashboardService {

    /**
     * 관리자 대시보드 전체 현황 조회 (카테고리별 분포 포함)
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
    AdminArtistApplicationDetailResponse getArtistApplicationDetail(Long applicationId);

    /**
     * 관리자 펀딩 승인 대기 목록 조회 (PENDING 상태)
     */
    AdminFundingApprovalResponse getFundingApprovals(AdminFundingApprovalSearchRequest request);

    /**
     * 관리자 펀딩 승인 대기 상세 조회 (PENDING 상태)
     */
    AdminFundingApprovalDetailResponse getFundingApprovalDetail(Long fundingId);
}
