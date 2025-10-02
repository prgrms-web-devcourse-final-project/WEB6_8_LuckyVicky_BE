package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.request.*;
import com.back.domain.dashboard.customer.dto.response.*;

/**
 * 고객용 대시보드 서비스 인터페이스
 * 2025.10.02 수정 - JWT 표준 패턴 적용, Request DTO 활용
 */
public interface DashboardService {

    /**
     * 계정 설정 조회
     */
    AccountResponse.Settings getAccountSettings(Long userId, String include);

    /**
     * 작가 신청 내역 목록 조회
     */
    ArtistApplicationResponse.List getArtistApplications(Long userId, ArtistApplicationSearchRequest request);

    /**
     * 입점 신청 상세 조회
     */
    ArtistApplicationResponse.Detail getArtistApplicationDetail(Long userId, Long applicationId);

    /**
     * 주문 목록 조회
     */
    OrderResponse.List getOrders(Long userId, OrderSearchRequest request);

    /**
     * 내가 팔로우한 작가 목록 조회
     */
    FollowingResponse.List getFollowingArtists(Long userId, FollowingSearchRequest request);

    /**
     * 찜한 상품 목록 조회
     */
    WishlistResponse.List getWishlist(Long userId, WishlistSearchRequest request);

    /**
     * 참여한 펀딩 목록 조회
     */
    FundingResponse.List getFundingParticipations(Long userId, FundingSearchRequest request);

    /**
     * 교환/반품 폼 데이터 조회
     */
    ReturnResponse.FormData getReturnFormData(Long userId, Long returnId);

    /**
     * 캐시 정보 조회
     */
    CashResponse.Balance getCashBalance(Long userId);

    /**
     * 캐시 충전 내역 조회
     */
    CashResponse.HistoryList getCashHistory(Long userId, CashHistorySearchRequest request);
}
