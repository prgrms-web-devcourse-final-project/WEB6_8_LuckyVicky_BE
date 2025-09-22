package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 고객용 대시보드 서비스 구현체
 *2025.09.22 수정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    
    @Override
    public AccountResponse.Settings getAccountSettings(String authorization, String include) {
        // TODO: JWT 토큰에서 사용자 정보 추출
        // TODO: 실제 데이터베이스에서 사용자 정보 조회
        
        AccountResponse.Settings response = new AccountResponse.Settings();
        
        // include 파라미터에 따라 필요한 정보만 포함
        String[] includeFields = include.split(",");
        
        for (String field : includeFields) {
            switch (field.trim()) {
                case "profile":
                    response.setProfile(new AccountResponse.Profile(
                            10025L, "닉네임입니다", "https://cdn.example.com/u/10025/profile.jpg"));
                    break;
                case "contact":
                    response.setContact(new AccountResponse.Contact(
                            "user@example.com", true, "+821012345678", "서울특별시 강남구 테헤란로 123 2층"));
                    break;
                case "security":
                    response.setSecurity(new AccountResponse.Security(
                            LocalDateTime.of(2025, 8, 10, 11, 0)));
                    break;
            }
        }
        
        return response;
    }
    
    @Override
    public ArtistApplicationResponse.List getArtistApplications(String authorization, int page, int size,
                                                                String status, String startDate, String endDate,
                                                                String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        // 샘플 데이터
        ArtistApplicationResponse.SummaryDto summary = 
                new ArtistApplicationResponse.SummaryDto(2, 0, 1, 1);
        
        List<ArtistApplicationResponse.Summary> content = Arrays.asList(
                new ArtistApplicationResponse.Summary(
                        1L, "모리모리", "2025-09-19", "REJECTED", "거절",
                        new ArtistApplicationResponse.Permission(false, false, true),
                        LocalDateTime.of(2025, 9, 20, 10, 30)),
                new ArtistApplicationResponse.Summary(
                        2L, "모리모리모리", "2025-09-25", "APPROVED", "승인",
                        new ArtistApplicationResponse.Permission(false, false, false),
                        LocalDateTime.of(2025, 9, 25, 14, 10))
        );
        
        return new ArtistApplicationResponse.List(summary, content, page, 10, 2, 1, false, false);
    }
    
    @Override
    public ArtistApplicationResponse.Detail getArtistApplicationDetail(String authorization, Long applicationId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        return new ArtistApplicationResponse.Detail(
                new ArtistApplicationResponse.Application(
                        2L, "REJECTED", "입점 거절", 
                        LocalDateTime.of(2025, 9, 25, 9, 30),
                        LocalDateTime.of(2025, 9, 25, 14, 10), 
                        "브랜드 컨셉 불일치",
                        new ArtistApplicationResponse.Reviewer("admin_001", "관리자A")),
                new ArtistApplicationResponse.Applicant(
                        "abc123", "모리모리모리", "https://cdn.example.com/u/10025/profile.jpg"),
                new ArtistApplicationResponse.Contact(
                        "abc123@abc.com", "010-1234-5678"),
                new ArtistApplicationResponse.Business(
                        "123-45-67890",
                        new ArtistApplicationResponse.FileDto(
                                "biz-123", "사업자등록증.pdf", 
                                "https://files.example.com/signed/biz-123",
                                LocalDateTime.of(2025, 9, 25, 15, 10)),
                        "2025-서울강남-1234",
                        new ArtistApplicationResponse.FileDto(
                                "rep-456", "통신판매업신고증.pdf",
                                "https://files.example.com/signed/rep-456",
                                LocalDateTime.of(2025, 9, 25, 15, 10)),
                        "서울특별시 강남구 테헤란로 123 2층"),
                new ArtistApplicationResponse.Profile(
                        "작가 소개입니다.",
                        Arrays.asList("스티커", "메모지"),
                        List.of(new ArtistApplicationResponse.Sns("Instagram", "@morimori_official")),
                        List.of(new ArtistApplicationResponse.FileDto(
                                "pf-1", "포트폴리오.pdf",
                                "https://files.example.com/signed/pf-1",
                                LocalDateTime.of(2025, 9, 25, 15, 10)))),
                new ArtistApplicationResponse.Permission(false, false, true)
        );
    }
    
    @Override
    public OrderResponse.List getOrders(String authorization, int page, int size, String status,
                                        String period, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        OrderResponse.SummaryDto summary = 
                new OrderResponse.SummaryDto(42, 3, 5, 12, 20, 2);
        
        List<OrderResponse.Summary> content = List.of(
                new OrderResponse.Summary(
                        "550e8400-e29b-41d4-a716-446655440000", "0123157", "2025-09-18",
                        "PENDING", "발주 전", 47500, 2,
                        new OrderResponse.Product(101L, "상품명입니다 상품명입니다", 1, 12500, "https://example.com/product101.jpg"),
                        new OrderResponse.Shipping("서울시 강남구 …", "홍길동"),
                        new OrderResponse.Permission(true, false, false),
                        new OrderResponse.Link("/orders/0123157"),
                        Arrays.asList(
                                new OrderResponse.OrderItem(1L, 101L, "상품명입니다 상품명입니다", 1, 12500, "https://example.com/product101.jpg"),
                                new OrderResponse.OrderItem(2L, 102L, "다른 상품명", 1, 35000, "https://example.com/product102.jpg")
                        ))
        );
        
        return new OrderResponse.List(summary, content, page, 10, 42, 5, true, false);
    }
    
    @Override
    public FollowingResponse.List getFollowingArtists(String userId, String authorization, int page, int size, 
                                                     String keyword, String status, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        // 조회 대상 사용자 프로필 정보
        FollowingResponse.Profile profile = new FollowingResponse.Profile(
                "abc123", "사용자닉네임", "https://cdn.example.com/u/abc123/profile.jpg");
        
        // 팔로우 현황 요약 정보
        FollowingResponse.SummaryDto summary = 
                new FollowingResponse.SummaryDto(8);
        
        // 팔로우한 작가 목록
        List<FollowingResponse.Artist> content = Arrays.asList(
                new FollowingResponse.Artist(
                        "artist_001", "작가명입니다",
                        "https://cdn.example.com/artists/artist_001/profile.jpg",
                        500, "/artists/artist_001",
                        new FollowingResponse.FollowRelation("FOLLOWING", 
                                LocalDateTime.of(2025, 9, 18, 10, 0)),
                        new FollowingResponse.Badge(true)
                ),
                new FollowingResponse.Artist(
                        "artist_002", "다른작가",
                        "https://cdn.example.com/artists/artist_002/profile.jpg",
                        123, "/artists/artist_002",
                        new FollowingResponse.FollowRelation("FOLLOWING", 
                                LocalDateTime.of(2025, 9, 17, 14, 20)),
                        new FollowingResponse.Badge(false)
                )
        );
        
        return new FollowingResponse.List(profile, summary, content, page, 8, 8, 1, false, false);
    }
    
    @Override
    public WishlistResponse.List getWishlist(String authorization, int page, int size, String keyword,
                                             String artistId, Long categoryId, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        WishlistResponse.SummaryDto summary = new WishlistResponse.SummaryDto(57);
        
        List<WishlistResponse.BulkAction> bulkActions = List.of(
                new WishlistResponse.BulkAction("BULK_UNWISH", "선택 항목 해제", true)
        );
        
        List<WishlistResponse.Item> content = List.of(
                new WishlistResponse.Item(
                        "w-20250918-0001", 123157L, "0123157", "상품명입니다 상품명입니다", 90000,
                        new WishlistResponse.Artist("asdfd331", "작가명입니다 작가명입니다"),
                        "https://cdn.example.com/p/123157/main_256.jpg", "SELLING", "2025-09-18",
                        LocalDateTime.of(2025, 9, 19, 11, 45), "/products/0123157",
                        new WishlistResponse.Permission(true)
                )
        );
        
        return new WishlistResponse.List(summary, bulkActions, content, page, 10, 57, 6, true, false);
    }
    
    @Override
    public FundingResponse.List getFundingParticipations(String authorization, int page, int size,
                                                         String status, String keyword, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        FundingResponse.SummaryDto summary = 
                new FundingResponse.SummaryDto(12, 4, 5, 1, 2);
        
        List<FundingResponse.Participation> content = Arrays.asList(
                new FundingResponse.Participation(
                        "00010", "FP-20250918-0001",
                        "https://cdn.example.com/f/456789/thumb_256.jpg",
                        "펀딩 제목입니다 펀딩 제목입니다",
                        new FundingResponse.Artist("artist_abc", "작가명입니다"),
                        1, 1000, "ACTIVE", "진행중", "2025-09-18",
                        new FundingResponse.Link("/fundings/456789"),
                        new FundingResponse.Meta(
                                456789L, "F0456789", 100, 900000, 900000, 800,
                                new FundingResponse.UserPledge("리워드 A", 
                                        LocalDateTime.of(2025, 9, 10, 11, 20), "ACTIVE"),
                                new FundingResponse.Permission(true, false)
                        )
                ),
                new FundingResponse.Participation(
                        "00027", "FP-20250918-0002",
                        "https://cdn.example.com/f/456790/thumb_256.jpg",
                        "다른 펀딩 제목",
                        new FundingResponse.Artist("artist_xyz", "작가명입니다"),
                        2, 8000, "ENDED", "종료", "2025-09-18",
                        new FundingResponse.Link("/fundings/456790"),
                        new FundingResponse.Meta(
                                456790L, "F0456790", 1500, 13500000, 900000, 800,
                                new FundingResponse.UserPledge("리워드 B", 
                                        LocalDateTime.of(2025, 9, 8, 9, 0), "ACTIVE"),
                                new FundingResponse.Permission(false, true)
                        )
                ),
                new FundingResponse.Participation(
                        "00100", "FP-20250918-0003",
                        "https://cdn.example.com/f/456791/thumb_256.jpg",
                        "펀딩 제목입니다",
                        new FundingResponse.Artist("artist_qwe", "작가명입니다"),
                        1, 8000, "FULFILLING", "발송준비중", "2025-09-18",
                        new FundingResponse.Link("/fundings/456791"),
                        null  // meta 선택적
                ),
                new FundingResponse.Participation(
                        "01230", "FP-20250918-0004",
                        "https://cdn.example.com/f/456792/thumb_256.jpg",
                        "펀딩 제목입니다",
                        new FundingResponse.Artist("artist_asd", "작가명입니다"),
                        1, 1000, "FULFILLED", "발송완료", "2025-09-18",
                        new FundingResponse.Link("/fundings/456792"),
                        null  // meta 선택적
                )
        );
        
        return new FundingResponse.List(summary, content, page, 10, 12, 2, false, false);
    }
    
    @Override
    public ReturnResponse.FormData getReturnFormData(String authorization, Long returnId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        ReturnResponse.Summary summary = new ReturnResponse.Summary(
                "0123157", "브랜드명", "상품명입니다", 1000, 1,
                "https://cdn.example.com/i/98765/256.jpg");
        
        ReturnResponse.Form form = new ReturnResponse.Form(
                "EXCHANGE", "PICKUP", "DEFECT", "스티커 구김 현상 발견",
                List.of(new ReturnResponse.Image("img-1", "photo_1.jpg")),
                new ReturnResponse.Pickup("06245", "서울 강남구 테헤란로 123", "3층", "홍길동", "010-1234-5678"));
        
        ReturnResponse.Permission permissions = new ReturnResponse.Permission(true, true);
        
        return new ReturnResponse.FormData(summary, form, permissions);
    }
    
    @Override
    public CashResponse.Balance getCashBalance(String authorization) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        return new CashResponse.Balance(
                5900, "KRW", LocalDateTime.of(2025, 9, 22, 10, 15));
    }
    
    @Override
    public CashResponse.HistoryList getCashHistory(String authorization, int page, int size,
                                                  String method, String status, String dateFrom, String dateTo,
                                                  String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        CashResponse.SummaryDto summary = new CashResponse.SummaryDto(720, 18000, 480);
        
        List<CashResponse.Transaction> content = Arrays.asList(
                new CashResponse.Transaction(
                        "RC-20250131-2303-0001",
                        LocalDateTime.of(2025, 1, 31, 23, 3),
                        "캐시 충전", 2000, 60, "NAVERPAY", "네이버페이", "COMPLETED",
                        new CashResponse.Link(null)),
                new CashResponse.Transaction(
                        "RC-20250123-0020-0001",
                        LocalDateTime.of(2025, 1, 23, 0, 20),
                        "캐시 충전", 2000, 60, "NAVERPAY", "네이버페이", "COMPLETED",
                        new CashResponse.Link(null))
        );
        
        return new CashResponse.HistoryList(summary, content, page, 10, 9, 1, false, false);
    }
}
