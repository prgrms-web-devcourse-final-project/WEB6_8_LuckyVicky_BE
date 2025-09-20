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
                        new ArtistApplicationResponse.Permission(false, false),
                        LocalDateTime.of(2025, 9, 20, 10, 30)),
                new ArtistApplicationResponse.Summary(
                        2L, "모리모리모리", "2025-09-25", "APPROVED", "승인",
                        new ArtistApplicationResponse.Permission(false, false),
                        LocalDateTime.of(2025, 9, 25, 14, 10))
        );
        
        return new ArtistApplicationResponse.List(summary, content, page, 10, 2, 1, false, false);
    }
    
    @Override
    public ArtistApplicationResponse.Detail getArtistApplicationDetail(String authorization, Long applicationId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        return new ArtistApplicationResponse.Detail(
                new ArtistApplicationResponse.Application(
                        2L, "APPROVED", "승인", 
                        LocalDateTime.of(2025, 9, 25, 9, 30),
                        LocalDateTime.of(2025, 9, 25, 14, 10), null),
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
                new ArtistApplicationResponse.Permission(false, false)
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
    public FollowingResponse.List getFollowingArtists(String authorization, int page, int size, 
                                                     String keyword, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        FollowingResponse.SummaryDto summary = 
                new FollowingResponse.SummaryDto(2, 1);
        
        List<FollowingResponse.Artist> content = List.of(
                new FollowingResponse.Artist(
                        "asdfd331", "작가명입니다 작가명입니다",
                        "https://cdn.example.com/artists/asdfd331/profile.jpg",
                        500, "/artists/asdfd331",
                        LocalDateTime.of(2025, 9, 18, 10, 0),
                        LocalDateTime.of(2025, 9, 16, 9, 20),
                        Arrays.asList("스티커", "메모지"),
                        new FollowingResponse.Badge(true),
                        new FollowingResponse.Permission(true)
                )
        );
        
        return new FollowingResponse.List(summary, content, page, 10, 2, 1, false, false);
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
                new FundingResponse.SummaryDto(12, 4, 7, 1);
        
        List<FundingResponse.Participation> content = List.of(
                new FundingResponse.Participation(
                        "FP-20250918-0001",
                        new FundingResponse.Funding(
                                456789L, "F0456789", "펀딩 제목입니다 펀딩 제목입니다",
                                new FundingResponse.Artist("artist_abc", "작가명입니다"),
                                "https://cdn.example.com/f/456789/thumb_256.jpg", "2025-09-18",
                                100, 900000, 900000, 800
                        ),
                        new FundingResponse.UserPledge(
                                30000, "리워드 A", 1,
                                LocalDateTime.of(2025, 9, 10, 11, 20), "ACTIVE"
                        ),
                        "ACTIVE", "진행중",
                        new FundingResponse.Permission(true, false),
                        new FundingResponse.Link("/fundings/456789")
                )
        );
        
        return new FundingResponse.List(summary, content, page, 10, 12, 2, false, false);
    }
}
