package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;

/**
 * 고객용 대시보드 서비스 인터페이스
 *2025.09.22 수정
 */
public interface DashboardService {
    
    /**
     * 계정 설정 조회
     */
    AccountResponse.Settings getAccountSettings(String authorization, String include);
    
    /**
     * 작가 신청 내역 목록 조회
     */
    ArtistApplicationResponse.List getArtistApplications(String authorization, int page, int size,
                                                         String status, String startDate, String endDate,
                                                         String sort, String order);
    
    /**
     * 입점 신청 상세 조회
     */
    ArtistApplicationResponse.Detail getArtistApplicationDetail(String authorization, Long applicationId);
    
    /**
     * 주문 목록 조회
     */
    OrderResponse.List getOrders(String authorization, int page, int size, String status,
                                String aftersalesStatus, String from, String to, String period,
                                String sort, String order);
    
    /**
     * 내가 팔로우한 작가 목록 조회
     */
    FollowingResponse.List getFollowingArtists(String userId, String authorization, int page, int size, 
                                              String keyword, String status, String sort, String order);
    
    /**
     * 찜한 상품 목록 조회
     */
    WishlistResponse.List getWishlist(String authorization, int page, int size, String keyword,
                                      String artistId, Long categoryId, String sort, String order);
    
    /**
     * 참여한 펀딩 목록 조회
     */
    FundingResponse.List getFundingParticipations(String authorization, int page, int size,
                                                  String status, String keyword, String sort, String order);
    
    /**
     * 교환/반품 폼 데이터 조회
     */
    ReturnResponse.FormData getReturnFormData(String authorization, Long returnId);
    
    /**
     * 캐시 정보 조회
     */
    CashResponse.Balance getCashBalance(String authorization);
    
    /**
     * 캐시 충전 내역 조회
     */
    CashResponse.HistoryList getCashHistory(String authorization, int page, int size,
                                           String method, String status, String dateFrom, String dateTo,
                                           String sort, String order);
}
