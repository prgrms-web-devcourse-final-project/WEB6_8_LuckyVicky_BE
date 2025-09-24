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
 *2025.09.23 수정
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
                            10025L, "사용자닉네임", "https://cdn.example.com/u/10025/profile.jpg"));
                    break;
                case "contact":
                    response.setContact(new AccountResponse.Contact(
                            "user@example.com", true, "+821012345678", "서울특별시 강남구"));
                    break;
                case "security":
                    response.setSecurity(new AccountResponse.Security(LocalDateTime.now()));
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
        
        ArtistApplicationResponse.SummaryDto summary = 
                new ArtistApplicationResponse.SummaryDto(2, 0, 1, 1);
        
        List<ArtistApplicationResponse.Summary> content = Arrays.asList(
                new ArtistApplicationResponse.Summary(
                        1L, "작가지원자", "2025-09-19", "PENDING", "대기중",
                        new ArtistApplicationResponse.Permission(true, false, true),
                        LocalDateTime.now()),
                new ArtistApplicationResponse.Summary(
                        2L, "승인작가", "2025-09-18", "APPROVED", "승인",
                        new ArtistApplicationResponse.Permission(false, false, false),
                        LocalDateTime.now().minusDays(1))
        );
        
        return new ArtistApplicationResponse.List(summary, content, page, size, 2, 1, false, false);
    }
    
    @Override
    public ArtistApplicationResponse.Detail getArtistApplicationDetail(String authorization, Long applicationId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        return new ArtistApplicationResponse.Detail(
                new ArtistApplicationResponse.Application(
                        applicationId, "PENDING", "검토중", 
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now(), 
                        null,
                        new ArtistApplicationResponse.Reviewer("admin_001", "관리자")),
                new ArtistApplicationResponse.Applicant(
                        "user123", "지원작가", "https://cdn.example.com/u/user123/profile.jpg"),
                new ArtistApplicationResponse.Contact(
                        "user123@example.com", "010-1234-5678"),
                new ArtistApplicationResponse.Business(
                        "123-45-67890",
                        new ArtistApplicationResponse.FileDto(
                                "biz-123", "사업자등록증.pdf", 
                                "https://files.example.com/biz-123",
                                LocalDateTime.now()),
                        "2025-서울-1234",
                        new ArtistApplicationResponse.FileDto(
                                "rep-456", "통신판매업신고증.pdf",
                                "https://files.example.com/rep-456",
                                LocalDateTime.now()),
                        "서울특별시 강남구"),
                new ArtistApplicationResponse.Profile(
                        "작가 소개입니다.",
                        Arrays.asList("스티커", "메모지"),
                        List.of(new ArtistApplicationResponse.Sns("Instagram", "@artist")),
                        List.of(new ArtistApplicationResponse.FileDto(
                                "pf-1", "포트폴리오.pdf",
                                "https://files.example.com/pf-1",
                                LocalDateTime.now()))),
                new ArtistApplicationResponse.Permission(true, true, true)
        );
    }
    
    @Override
    public OrderResponse.List getOrders(String authorization, int page, int size, String status,
                                        String aftersalesStatus, String from, String to, String period, String sort, String order) {
        // TODO: 실제 주문 데이터 조회 로직 구현
        
        OrderResponse.SummaryDto summary = OrderResponse.SummaryDto.builder()
                .totalOrders(25)
                .pending(3)
                .confirmed(2)  
                .preparing(5)
                .shipped(10)
                .delivered(5)
                .canceled(2)
                .cancelRequested(1)
                .cancelProcessing(0) 
                .cancelCompleted(1)
                .exchangeRequested(1)
                .exchangeProcessing(0)
                .exchangeCompleted(1)
                .build();
        
        List<OrderResponse.Summary> content = List.of(
                OrderResponse.Summary.builder()
                        .orderId("ORDER-001")
                        .orderNumber("0123157")
                        .orderDate("2025-09-18T11:20:00+09:00")
                        .status("PENDING")
                        .statusText("결제완료")
                        .totalAmount(47500)
                        .itemCount(2)
                        .representativeItem(OrderResponse.Product.builder()
                                .productId(101L)
                                .productName("감성 포스터")
                                .quantity(1)
                                .price(25000)
                                .imageUrl("https://example.com/product101.jpg")
                                .build())
                        .shipping(OrderResponse.Shipping.builder()
                                .addressShort("서울시 강남구")
                                .recipient("홍길동")
                                .build())
                        .aftersales(null)
                        .permissions(OrderResponse.Permission.builder()
                                .canCancel(true)
                                .canReturn(false)
                                .canExchange(false)
                                .build())
                        .links(OrderResponse.Link.builder()
                                .detail("/orders/0123157")
                                .build())
                        .items(Arrays.asList(
                                OrderResponse.OrderItem.builder()
                                        .orderItemId(1L)
                                        .productId(101L)
                                        .productName("감성 포스터")
                                        .quantity(1)
                                        .price(25000)
                                        .imageUrl("https://example.com/product101.jpg")
                                        .build(),
                                OrderResponse.OrderItem.builder()
                                        .orderItemId(2L)
                                        .productId(102L)
                                        .productName("아트 스티커")
                                        .quantity(1)
                                        .price(22500)
                                        .imageUrl("https://example.com/product102.jpg")
                                        .build()
                        ))
                        .build()
        );
        
        OrderResponse.PeriodInfo periodInfo = OrderResponse.PeriodInfo.builder()
                .type("MONTH")
                .from("2025-09-01")
                .to("2025-09-30")
                .build();
        
        return new OrderResponse.List(
                summary, content, page, size, 25, 3, true, false, 
                "Asia/Seoul", periodInfo);
    }
    
    @Override
    public FollowingResponse.List getFollowingArtists(String userId, String authorization, int page, int size, 
                                                     String keyword, String status, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        FollowingResponse.Profile profile = new FollowingResponse.Profile(
                userId, "사용자닉네임", "https://cdn.example.com/u/" + userId + "/profile.jpg");
        
        FollowingResponse.SummaryDto summary = 
                new FollowingResponse.SummaryDto(5);
        
        List<FollowingResponse.Artist> content = Arrays.asList(
                new FollowingResponse.Artist(
                        "artist_001", "감성작가",
                        "https://cdn.example.com/artists/artist_001/profile.jpg",
                        500, "/artists/artist_001",
                        new FollowingResponse.FollowRelation("FOLLOWING", LocalDateTime.now()),
                        new FollowingResponse.Badge(true)
                ),
                new FollowingResponse.Artist(
                        "artist_002", "캐릭터작가",
                        "https://cdn.example.com/artists/artist_002/profile.jpg",
                        123, "/artists/artist_002",
                        new FollowingResponse.FollowRelation("FOLLOWING", LocalDateTime.now().minusDays(1)),
                        new FollowingResponse.Badge(false)
                )
        );
        
        return new FollowingResponse.List(profile, summary, content, page, size, 5, 1, false, false);
    }
    
    @Override
    public WishlistResponse.List getWishlist(String authorization, int page, int size, String keyword,
                                             String artistId, Long categoryId, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        WishlistResponse.SummaryDto summary = new WishlistResponse.SummaryDto(15);
        
        List<WishlistResponse.BulkAction> bulkActions = List.of(
                new WishlistResponse.BulkAction("BULK_UNWISH", "선택 항목 해제", true)
        );
        
        List<WishlistResponse.Item> content = List.of(
                new WishlistResponse.Item(
                        "w-001", 123157L, "0123157", "감성 일러스트 포스터", 25000,
                        new WishlistResponse.Artist("artist001", "감성작가"),
                        "https://cdn.example.com/p/123157/main.jpg", "SELLING", "2025-09-18",
                        LocalDateTime.now(), "/products/0123157",
                        new WishlistResponse.Permission(true)
                ),
                new WishlistResponse.Item(
                        "w-002", 123158L, "0123158", "귀여운 스티커 세트", 15000,
                        new WishlistResponse.Artist("artist002", "캐릭터작가"),
                        "https://cdn.example.com/p/123158/main.jpg", "SELLING", "2025-09-17",
                        LocalDateTime.now().minusDays(1), "/products/0123158",
                        new WishlistResponse.Permission(true)
                )
        );
        
        return new WishlistResponse.List(summary, bulkActions, content, page, size, 15, 2, true, false);
    }
    
    @Override
    public FundingResponse.List getFundingParticipations(String authorization, int page, int size,
                                                         String status, String keyword, String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        FundingResponse.SummaryDto summary = 
                new FundingResponse.SummaryDto(8, 3, 3, 1, 1);
        
        List<FundingResponse.Participation> content = Arrays.asList(
                new FundingResponse.Participation(
                        "00010", "FP-001",
                        "https://cdn.example.com/f/456789/thumb.jpg",
                        "아티스트 굿즈 펀딩",
                        new FundingResponse.Artist("artist_abc", "펀딩작가"),
                        1, 25000, "ACTIVE", "진행중", "2025-09-18",
                        new FundingResponse.Link("/fundings/456789"),
                        new FundingResponse.Meta(
                                456789L, "F0456789", 100, 900000, 900000, 800,
                                new FundingResponse.UserPledge("리워드 A", LocalDateTime.now(), "ACTIVE"),
                                new FundingResponse.Permission(true, false)
                        )
                ),
                new FundingResponse.Participation(
                        "00027", "FP-002",
                        "https://cdn.example.com/f/456790/thumb.jpg",
                        "캐릭터 굿즈 펀딩",
                        new FundingResponse.Artist("artist_xyz", "캐릭터작가"),
                        2, 35000, "ENDED", "종료", "2025-09-10",
                        new FundingResponse.Link("/fundings/456790"),
                        new FundingResponse.Meta(
                                456790L, "F0456790", 150, 1350000, 900000, 800,
                                new FundingResponse.UserPledge("리워드 B", LocalDateTime.now().minusDays(8), "COMPLETED"),
                                new FundingResponse.Permission(false, true)
                        )
                )
        );
        
        return new FundingResponse.List(summary, content, page, size, 8, 1, false, false);
    }
    
    @Override
    public ReturnResponse.FormData getReturnFormData(String authorization, Long returnId) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        ReturnResponse.Summary summary = new ReturnResponse.Summary(
                "0123157", "아티스트브랜드", "감성 포스터", 25000, 1,
                "https://cdn.example.com/product.jpg");
        
        ReturnResponse.Form form = new ReturnResponse.Form(
                "EXCHANGE", "PICKUP", "DEFECT", "상품 불량",
                List.of(new ReturnResponse.Image("img-1", "photo.jpg")),
                new ReturnResponse.Pickup("12345", "서울 강남구", "3층", "홍길동", "010-1234-5678"));
        
        ReturnResponse.Permission permissions = new ReturnResponse.Permission(true, true);
        
        return new ReturnResponse.FormData(summary, form, permissions);
    }
    
    @Override
    public CashResponse.Balance getCashBalance(String authorization) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        return new CashResponse.Balance(5900, "KRW", LocalDateTime.now());
    }
    
    @Override
    public CashResponse.HistoryList getCashHistory(String authorization, int page, int size,
                                                  String method, String status, String dateFrom, String dateTo,
                                                  String sort, String order) {
        // TODO: 실제 데이터베이스 조회 로직 구현
        
        CashResponse.SummaryDto summary = new CashResponse.SummaryDto(5, 15000, 2);
        
        List<CashResponse.Transaction> content = Arrays.asList(
                new CashResponse.Transaction(
                        "RC-001", LocalDateTime.now(),
                        "캐시 충전", 10000, 50, "NAVERPAY", "네이버페이", "COMPLETED",
                        new CashResponse.Link(null)),
                new CashResponse.Transaction(
                        "RC-002", LocalDateTime.now().minusDays(1),
                        "캐시 사용", -5000, 45, "PURCHASE", "상품구매", "COMPLETED",
                        new CashResponse.Link("/orders/0123157"))
        );
        
        return new CashResponse.HistoryList(summary, content, page, size, 5, 1, false, false);
    }
}