package com.back.domain.dashboard.customer.service;

import com.back.domain.dashboard.customer.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingContribution;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고객용 대시보드 서비스 구현체
 * 2025.09.30 수정 - Funding 실제 구현 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FundingContributionRepository fundingContributionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AccountResponse.Settings getAccountSettings(String authorization, String include) {
        // TODO: JWT 토큰에서 사용자 정보 추출
        // TODO: 실제 데이터베이스에서 사용자 정보 조회

        AccountResponse.Profile profile = null;
        AccountResponse.Contact contact = null;
        AccountResponse.Security security = null;

        // include 파라미터에 따라 필요한 정보만 포함
        String[] includeFields = include.split(",");

        for (String field : includeFields) {
            switch (field.trim()) {
                case "profile":
                    profile = new AccountResponse.Profile(
                            10025L, "사용자닉네임", "https://cdn.example.com/u/10025/profile.jpg");
                    break;
                case "contact":
                    contact = new AccountResponse.Contact(
                            "user@example.com", true, "+821012345678", "서울특별시 강남구");
                    break;
                case "security":
                    security = new AccountResponse.Security(LocalDateTime.now());
                    break;
            }
        }

        return new AccountResponse.Settings(profile, contact, security);
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

        OrderResponse.SummaryDto summary = new OrderResponse.SummaryDto(
                25, 3, 2, 5, 10, 5, 2, 1, 0, 1, 1, 0, 1
        );

        List<OrderResponse.Summary> content = List.of(
                new OrderResponse.Summary(
                        "ORDER-001",
                        "0123157",
                        "2025-09-18T11:20:00+09:00",
                        "PENDING",
                        "결제완료",
                        47500,
                        2,
                        new OrderResponse.Product(
                                101L,
                                "감성 포스터",
                                1,
                                25000,
                                "https://example.com/product101.jpg"
                        ),
                        new OrderResponse.Shipping(
                                "서울시 강남구",
                                "홍길동"
                        ),
                        null, // aftersales
                        new OrderResponse.Permission(
                                true,
                                false,
                                false
                        ),
                        new OrderResponse.Link(
                                "/orders/0123157"
                        ),
                        Arrays.asList(
                                new OrderResponse.OrderItem(
                                        1L,
                                        101L,
                                        "감성 포스터",
                                        1,
                                        25000,
                                        "https://example.com/product101.jpg"
                                ),
                                new OrderResponse.OrderItem(
                                        2L,
                                        102L,
                                        "아트 스티커",
                                        1,
                                        22500,
                                        "https://example.com/product102.jpg"
                                )
                        )
                )
        );

        OrderResponse.PeriodInfo periodInfo = new OrderResponse.PeriodInfo(
                "MONTH",
                "2025-09-01",
                "2025-09-30"
        );

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
        // 1. JWT 토큰에서 사용자 ID 추출
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        log.debug("펀딩 목록 조회 - userId: {}, page: {}, size: {}, status: {}, keyword: {}",
                userId, page, size, status, keyword);

        // 2. Pageable 생성 (정렬 포함)
        Pageable pageable = createPageable(page, size, sort, order);

        // 3. Repository 조회
        Page<FundingContribution> contributionPage = fundingContributionRepository
                .findContributionsByBuyerWithFilters(userId, keyword, status, pageable);

        // 4. 엔티티 → DTO 변환
        List<FundingResponse.Participation> content = contributionPage.getContent().stream()
                .map(this::toParticipationDto)
                .collect(Collectors.toList());

        // 5. 통계 계산 (배송 상태 제외)
        FundingResponse.SummaryDto summary = calculateSummary(userId);

        // 6. 응답 생성
        return new FundingResponse.List(
                summary,
                content,
                contributionPage.getNumber(),
                contributionPage.getSize(),
                contributionPage.getTotalElements(),
                contributionPage.getTotalPages(),
                contributionPage.hasNext(),
                contributionPage.hasPrevious()
        );
    }

    /**
     * Pageable 생성 (정렬 포함)
     */
    private Pageable createPageable(int page, int size, String sort, String order) {
        if (sort == null || sort.isEmpty()) {
            sort = "paidAt";
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";
        }

        org.springframework.data.domain.Sort.Direction direction = 
            "ASC".equalsIgnoreCase(order) ? 
            org.springframework.data.domain.Sort.Direction.ASC : 
            org.springframework.data.domain.Sort.Direction.DESC;

        // 정렬 필드 매핑 (DTO 필드명 → 엔티티 필드명)
        String sortField = switch (sort) {
            case "paidAt" -> "paidAt";
            case "pledgedAmount" -> "totalAmount";
            case "title" -> "funding.title";
            case "artistName" -> "funding.user.name";
            case "status" -> "funding.status";
            default -> "paidAt";
        };

        return PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortField));
    }

    /**
     * FundingContribution 엔티티를 Participation DTO로 변환
     */
    private FundingResponse.Participation toParticipationDto(FundingContribution contribution) {
        Funding funding = contribution.getFunding();

        return new FundingResponse.Participation(
                generateParticipationNumber(contribution.getId()),
                contribution.getId(),
                funding.getImageUrl(),
                funding.getTitle(),
                new FundingResponse.Artist(
                        funding.getUser().getId(),
                        funding.getUser().getName()
                ),
                contribution.getQuantity(),
                contribution.getTotalAmount(),
                mapFundingStatus(funding.getStatus()),
                mapFundingStatusText(funding.getStatus()),
                contribution.getPaidAt().toLocalDate().toString(),
                new FundingResponse.Link("/fundings/" + funding.getId()),
                null  // Meta는 목록 조회에서 제외
        );
    }

    /**
     * 참여번호 생성 (5자리 포맷)
     */
    private String generateParticipationNumber(Long id) {
        return String.format("%05d", id);
    }

    /**
     * Funding 상태를 프론트엔드용 상태로 매핑
     */
    private String mapFundingStatus(FundingStatus status) {
        return switch (status) {
            case OPEN -> "ACTIVE";
            case CLOSED, SUCCESS, FAILED, CANCELED -> "ENDED";
        };
    }

    /**
     * Funding 상태를 한글 텍스트로 매핑
     */
    private String mapFundingStatusText(FundingStatus status) {
        return switch (status) {
            case OPEN -> "진행중";
            case CLOSED -> "종료";
            case SUCCESS -> "성공";
            case FAILED -> "실패";
            case CANCELED -> "취소";
        };
    }

    /**
     * 펀딩 참여 통계 계산 (배송 상태 제외)
     */
    private FundingResponse.SummaryDto calculateSummary(Long userId) {
        // 상태별 카운트 (간단한 방법 - 성능이 중요하면 별도 쿼리 메서드 추가)
        List<FundingContribution> allContributions = fundingContributionRepository
                .findContributionsByBuyerWithFilters(userId, null, null, Pageable.unpaged())
                .getContent();

        int total = allContributions.size();

        int activeCount = (int) allContributions.stream()
                .filter(fc -> fc.getFunding().getStatus() == FundingStatus.OPEN)
                .count();

        int endedCount = (int) allContributions.stream()
                .filter(fc -> fc.getFunding().getStatus() == FundingStatus.CLOSED ||
                        fc.getFunding().getStatus() == FundingStatus.SUCCESS ||
                        fc.getFunding().getStatus() == FundingStatus.FAILED ||
                        fc.getFunding().getStatus() == FundingStatus.CANCELED)
                .count();

        return new FundingResponse.SummaryDto(
                total,
                activeCount,
                endedCount
        );
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
