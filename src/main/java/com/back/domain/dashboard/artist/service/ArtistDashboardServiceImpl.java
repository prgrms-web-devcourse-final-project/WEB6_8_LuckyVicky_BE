package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.response.ArtistCashResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistProductResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCashHistoryResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistOrderResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistCancellationResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistExchangeResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettingsResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistFundingResponse;
import com.back.domain.dashboard.artist.dto.response.ArtistSettlementResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * 작가용 대시보드 서비스 구현체
 * 2025.09.29 수정 - getProducts() 실제 DB 연동
 * 2025.09.30 수정 - getFundings() 실제 DB 연동
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArtistDashboardServiceImpl implements ArtistDashboardService {

    private final ProductRepository productRepository;
    private final FundingRepository fundingRepository;
    private final FundingContributionRepository fundingContributionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    @Override
    public ArtistMainResponse getMainStats(String authorization, String range, String from, String to,
                                           String interval, String tz) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 통계 데이터 조회

        // 프로필 정보
        ArtistMainResponse.Profile profile = new ArtistMainResponse.Profile(
                5L,
                "감성작가",
                "artist@example.com",
                "https://cdn.example.com/u/5/profile.jpg"
        );

        // 통계 정보
        ArtistMainResponse.Stats stats = new ArtistMainResponse.Stats(
                1250,
                28,
                125000,
                8,
                2450000,
                156,
                4.8,
                3
        );

        // 트렌드 메타 정보
        ArtistMainResponse.Meta meta = new ArtistMainResponse.Meta(
                "6M",
                "2025-03-01",
                "2025-09-01",
                "WEEK",
                "Asia/Seoul",
                400,
                new ArtistMainResponse.Compare("2024-09-01", "2025-03-01")
        );

        // 시계열 데이터 포인트 (간략화)
        List<ArtistMainResponse.DataPoint> salesPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 95000),
                new ArtistMainResponse.DataPoint("2025-03-10", 140000),
                new ArtistMainResponse.DataPoint("2025-03-17", 125000)
        );

        List<ArtistMainResponse.DataPoint> orderPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 5),
                new ArtistMainResponse.DataPoint("2025-03-10", 8),
                new ArtistMainResponse.DataPoint("2025-03-17", 6)
        );

        List<ArtistMainResponse.DataPoint> followerPoints = Arrays.asList(
                new ArtistMainResponse.DataPoint("2025-03-03", 1180),
                new ArtistMainResponse.DataPoint("2025-03-10", 1195),
                new ArtistMainResponse.DataPoint("2025-03-17", 1250)
        );

        // 시계열 데이터
        ArtistMainResponse.Series series = new ArtistMainResponse.Series(
                new ArtistMainResponse.SeriesData("KRW", salesPoints, 2450000),
                new ArtistMainResponse.SeriesData("COUNT", orderPoints, 156),
                new ArtistMainResponse.SeriesData("COUNT", followerPoints, 1250)
        );

        // 변화량 정보
        ArtistMainResponse.Changes changes = new ArtistMainResponse.Changes(
                new ArtistMainResponse.ChangeData(-40000, -0.242),
                new ArtistMainResponse.ChangeData(1, 0.143),
                new ArtistMainResponse.ChangeData(70, 0.059)
        );

        // 트렌드 정보
        ArtistMainResponse.Trends trends = new ArtistMainResponse.Trends(meta, series, changes);

        // 알림 정보 (간략화)
        List<ArtistMainResponse.Alert> orderAlerts = List.of(
                new ArtistMainResponse.Alert("NEW_ORDER", "새로운 주문 3건", 3, LocalDateTime.now())
        );

        List<ArtistMainResponse.Alert> fundingAlerts = List.of(
                new ArtistMainResponse.Alert("FUNDING_GOAL_ACHIEVED", "펀딩 목표 달성", 1, LocalDateTime.now())
        );

        ArtistMainResponse.Notifications notifications = new ArtistMainResponse.Notifications(orderAlerts, fundingAlerts);

        return new ArtistMainResponse(
                profile,
                stats,
                trends,
                notifications,
                LocalDateTime.now(),
                "Asia/Seoul"
        );
    }

    @Override
    public ArtistProductResponse.List getProducts(String authorization, int page, int size, String keyword,
                                                  Boolean selling, String sort, String order) {
        // JWT 토큰에서 작가 ID 추출
        String token = authorization.replace("Bearer ", "");
        Long artistId = jwtTokenProvider.getUserIdFromToken(token);

        log.info("작가 상품 목록 조회 시작 - artistId: {}, page: {}, size: {}, keyword: {}, selling: {}, sort: {}, order: {}",
                artistId, page, size, keyword, selling, sort, order);

        // Repository를 통한 실제 DB 조회
        Page<Product> productPage = productRepository.findProductsByArtist(
                artistId, keyword, selling, sort, order, PageRequest.of(page, size)
        );

        // Entity → DTO 변환
        List<ArtistProductResponse.Product> content = productPage.getContent().stream()
                .map(this::convertToProductDto)
                .toList();

        int totalPages = productPage.getTotalPages();
        long totalElements = productPage.getTotalElements();
        boolean hasNext = productPage.hasNext();
        boolean hasPrevious = productPage.hasPrevious();

        log.info("작가 상품 목록 조회 완료 - 조회된 상품 수: {}, 전체: {}", content.size(), totalElements);

        return new ArtistProductResponse.List(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }

    /**
     * Product 엔티티를 DTO로 변환
     */
    private ArtistProductResponse.Product convertToProductDto(Product product) {
        return new ArtistProductResponse.Product(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getSellingStatus().name(),
                convertStatusToKorean(product.getSellingStatus()),
                product.getCreateDate().format(DATE_FORMATTER)
        );
    }

    /**
     * SellingStatus Enum을 한글로 변환
     */
    private String convertStatusToKorean(SellingStatus status) {
        return switch (status) {
            case BEFORE_SELLING -> "판매 전";
            case SELLING -> "판매중";
            case SOLD_OUT -> "품절";
            case END_OF_SALE -> "판매종료";
        };
    }

    @Override
    public ArtistCashResponse.Balance getCashBalance(String authorization) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 지갑 잔액 정보 조회

        return new ArtistCashResponse.Balance(
                72000,
                15000,
                0,
                72000,
                "KRW",
                LocalDateTime.now()
        );
    }

    @Override
    public ArtistCashHistoryResponse.List getCashHistory(String authorization, int page, int size,
                                                         String type, String status, String dateFrom,
                                                         String dateTo, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 캐시 거래 내역 조회

        ArtistCashHistoryResponse.Summary summary = new ArtistCashHistoryResponse.Summary(
                74000,
                64000,
                10000
        );

        List<ArtistCashHistoryResponse.Transaction> content = Arrays.asList(
                new ArtistCashHistoryResponse.Transaction(
                        "TX-001",
                        "2025-09-24T09:12:00+09:00",
                        "DEPOSIT",
                        "정산금 입금",
                        10000,
                        0,
                        72000,
                        "WALLET",
                        "모리캐시",
                        "COMPLETED",
                        null
                ),
                new ArtistCashHistoryResponse.Transaction(
                        "TX-002",
                        "2025-09-23T16:20:00+09:00",
                        "WITHDRAWAL",
                        "환전",
                        0,
                        50000,
                        62000,
                        "BANK_TRANSFER",
                        "계좌이체",
                        "COMPLETED",
                        "우리은행"
                )
        );

        return new ArtistCashHistoryResponse.List(
                summary,
                content,
                page,
                size,
                content.size(),
                1,
                false,
                false
        );
    }

    @Override
    public ArtistOrderResponse.List getOrders(String authorization, int page, int size,
                                              String status, String keyword, String startDate,
                                              String endDate, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 주문 목록 조회

        ArtistOrderResponse.Summary summary = new ArtistOrderResponse.Summary(
                156, 8, 12, 100, 36, 5
        );

        List<ArtistOrderResponse.Order> content = List.of(
                new ArtistOrderResponse.Order(
                        "ORDER-001",
                        "0123157",
                        "2025-09-18",
                        "PENDING",
                        "발주 전",
                        47500,
                        "감성 포스터 외 1건",
                        2,
                        new ArtistOrderResponse.Buyer(201L, "아트러버", "김**"),
                        new ArtistOrderResponse.Shipment("READY", null, null),
                        new ArtistOrderResponse.Permissions(true, true)
                )
        );

        return new ArtistOrderResponse.List(
                summary,
                content,
                page,
                size,
                156,
                8,
                page < 7,
                page > 0
        );
    }

    @Override
    public ArtistCancellationResponse.List getCancellationRequests(String authorization, int page, int size,
                                                                   String status, String keyword, String startDate,
                                                                   String endDate, Long productId, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 취소 요청 목록 조회

        ArtistCancellationResponse.Summary summary = new ArtistCancellationResponse.Summary(8, 5, 2, 1);

        List<ArtistCancellationResponse.CancellationRequest> content = List.of(
                new ArtistCancellationResponse.CancellationRequest(
                        1L,
                        "ORDER-001",
                        "0123157",
                        "CANCEL",
                        "PENDING",
                        "처리대기",
                        "2024-12-26T09:00:00+09:00",
                        "단순 변심",
                        "취소 요청합니다.",
                        new ArtistCancellationResponse.Customer(201L, "고객A"),
                        new ArtistCancellationResponse.OrderItem(101L, "귀여운 스티커", 1, 15000),
                        15000,
                        new ArtistCancellationResponse.Permissions(true, true)
                )
        );

        return new ArtistCancellationResponse.List(
                summary,
                content,
                page,
                size,
                8,
                1,
                false,
                false
        );
    }

    @Override
    public ArtistExchangeResponse.List getExchangeRequests(String authorization, int page, int size,
                                                           String status, String keyword, String startDate,
                                                           String endDate, Long productId, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 교환 요청 목록 조회

        ArtistExchangeResponse.Summary summary = new ArtistExchangeResponse.Summary(5, 3, 1, 1);

        List<ArtistExchangeResponse.ExchangeRequest> content = List.of(
                new ArtistExchangeResponse.ExchangeRequest(
                        21L,
                        "ORDER-002",
                        "0123156",
                        "EXCHANGE",
                        "PENDING",
                        "처리대기",
                        "2024-12-26T11:10:00+09:00",
                        "사이즈 변경",
                        "L사이즈로 교환 요청",
                        new ArtistExchangeResponse.Customer(204L, "고객B"),
                        new ArtistExchangeResponse.OrderItem(103L, "아티스트 티셔츠", 1, 28500),
                        new ArtistExchangeResponse.ExchangeRequested("사이즈=L", 1),
                        new ArtistExchangeResponse.Permissions(true, true)
                )
        );

        return new ArtistExchangeResponse.List(
                summary,
                content,
                page,
                size,
                5,
                1,
                false,
                false
        );
    }

    @Override
    public ArtistSettingsResponse getSettings(String authorization) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 작가 설정 정보 조회

        // 프로필 정보
        ArtistSettingsResponse.Profile profile = new ArtistSettingsResponse.Profile(
                "작가명입니다",
                "자신을 소개하는 글을 입력해주세요.",
                List.of(new ArtistSettingsResponse.Sns("Instagram", "@mori_official")),
                "https://cdn.example.com/u/5/profile.jpg"
        );

        // 사업자 정보
        ArtistSettingsResponse.Business business = new ArtistSettingsResponse.Business(
                "서울특별시 강남구 테헤란로 123 2층",
                "123-45-67890",
                "2025-서울강남-1234",
                true
        );

        // 정산 계좌 정보
        ArtistSettingsResponse.Payout payout = new ArtistSettingsResponse.Payout(
                "088",
                "신한",
                "홍길동",
                "****-****-**3456",
                "VERIFIED"
        );

        // 권한 정보
        ArtistSettingsResponse.Permissions permissions = new ArtistSettingsResponse.Permissions(
                true,
                true,
                true
        );

        return new ArtistSettingsResponse(profile, business, payout, permissions);
    }

    @Override
    public ArtistFundingResponse.List getFundings(String authorization, int page, int size, String keyword,
                                                  String status, Long categoryId, Integer minAchievement, Integer maxAchievement,
                                                  String startDate, String endDate, String sort, String order) {
        // JWT 토큰에서 작가 ID 추출
        String token = authorization.replace("Bearer ", "");
        Long artistId = jwtTokenProvider.getUserIdFromToken(token);

        log.info("작가 펀딩 목록 조회 시작 - artistId: {}, page: {}, size: {}, keyword: {}, status: {}, sort: {}, order: {}",
                artistId, page, size, keyword, status, sort, order);

        // status 문자열을 FundingStatus enum으로 변환
        FundingStatus fundingStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                fundingStatus = FundingStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 펀딩 상태값: {}", status);
            }
        }

        // Repository를 통한 실제 DB 조회
        Page<Funding> fundingPage = fundingRepository.findFundingsByArtist(
                artistId, keyword, fundingStatus, sort, order, PageRequest.of(page, size)
        );

        // 전체 펀딩 통계 조회 (요약 정보용)
        Page<Funding> allFundings = fundingRepository.findFundingsByArtist(
                artistId, null, null, "createDate", "DESC", PageRequest.of(0, Integer.MAX_VALUE)
        );

        // 요약 정보 계산
        ArtistFundingResponse.Summary summary = calculateFundingSummary(allFundings.getContent());

        // Entity → DTO 변환
        List<ArtistFundingResponse.Funding> content = fundingPage.getContent().stream()
                .map(this::convertToFundingDto)
                .toList();

        int totalPages = fundingPage.getTotalPages();
        long totalElements = fundingPage.getTotalElements();
        boolean hasNext = fundingPage.hasNext();
        boolean hasPrevious = fundingPage.hasPrevious();

        log.info("작가 펀딩 목록 조회 완료 - 조회된 펀딩 수: {}, 전체: {}", content.size(), totalElements);

        return new ArtistFundingResponse.List(
                summary, content,
                page, size, totalElements, totalPages, hasNext, hasPrevious
        );
    }

    /**
     * 펀딩 요약 정보 계산
     */
    private ArtistFundingResponse.Summary calculateFundingSummary(List<Funding> fundings) {
        int total = fundings.size();
        int open = (int) fundings.stream().filter(f -> f.getStatus() == FundingStatus.OPEN).count();
        int success = (int) fundings.stream().filter(f -> f.getStatus() == FundingStatus.SUCCESS).count();
        int failed = (int) fundings.stream().filter(f -> 
            f.getStatus() == FundingStatus.FAILED || f.getStatus() == FundingStatus.CLOSED
        ).count();

        return new ArtistFundingResponse.Summary(total, open, success, failed);
    }

    /**
     * Funding 엔티티를 DTO로 변환
     */
    private ArtistFundingResponse.Funding convertToFundingDto(Funding funding) {
        // 누적 모금액 조회
        long currentAmount = nz(fundingContributionRepository.sumContributedAmountByFundingId(funding.getId()));
        
        // 참여자 수 조회 (Funding 엔티티에 저장된 값 사용)
        int participantCount = funding.getParticipantCount();

        // 달성률 계산
        double achievementRate = funding.getTargetAmount() == 0 
                ? 0.0 
                : Math.min(100.0, (currentAmount * 100.0) / funding.getTargetAmount());

        // 남은 일수 계산
        int remainingDays = 0;
        boolean dueSoon = false;
        boolean ended = false;

        if (funding.getEndDate() != null) {
            remainingDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), funding.getEndDate().toLocalDate());
            dueSoon = remainingDays > 0 && remainingDays <= 7;
            ended = remainingDays < 0;
        }

        // 목표 달성 여부
        boolean goalAchieved = currentAmount >= funding.getTargetAmount();

        // 권한 정보 (작가 본인이므로 모든 권한 true)
        ArtistFundingResponse.Permissions permissions = new ArtistFundingResponse.Permissions(
                true,  // canEdit
                funding.getStatus() == FundingStatus.OPEN,  // canCancel (진행중일 때만)
                true   // canCreateNews
        );

        // 플래그 정보
        ArtistFundingResponse.Flags flags = new ArtistFundingResponse.Flags(
                goalAchieved,
                dueSoon,
                ended
        );

        // 카테고리 정보 (현재 미사용)
        ArtistFundingResponse.Category category = null;

        return new ArtistFundingResponse.Funding(
                funding.getId(),
                funding.getTitle(),
                funding.getStatus().name(),
                convertFundingStatusToKorean(funding.getStatus()),
                funding.getTargetAmount(),
                currentAmount,
                achievementRate,
                participantCount,
                funding.getStartDate().format(DATE_FORMATTER),
                funding.getEndDate().format(DATE_FORMATTER),
                funding.getCreateDate().format(DATE_FORMATTER),
                funding.getImageUrl(),
                category,
                permissions,
                flags
        );
    }

    /**
     * FundingStatus Enum을 한글로 변환
     */
    private String convertFundingStatusToKorean(FundingStatus status) {
        return switch (status) {
            case OPEN -> "진행중";
            case CLOSED -> "마감";
            case SUCCESS -> "성공";
            case FAILED -> "실패";
            case CANCELED -> "취소됨";
        };
    }

    /**
     * null을 0으로 변환하는 헬퍼 메서드
     */
    private long nz(Long value) {
        return value == null ? 0L : value;
    }

    @Override
    public ArtistSettlementResponse getSettlements(String authorization, Integer year, Integer month, String granularity,
                                                   String status, Long productId, int page, int size, String sort, String order) {
        // TODO: JWT 토큰에서 작가 정보 추출
        // TODO: 실제 데이터베이스에서 정산 내역 조회
        // TODO: 연도가 null이면 서버 현재 연도 사용

        // 조회 범위 - month 파라미터를 그대로 전달
        ArtistSettlementResponse.Scope scope = new ArtistSettlementResponse.Scope(
                year != null ? year : 2025, month
        );

        // 요약 정보
        ArtistSettlementResponse.Summary summary = new ArtistSettlementResponse.Summary(
                new ArtistSettlementResponse.AmountInfo(128000, "총 매출"),
                new ArtistSettlementResponse.AmountInfo(51264, "수수료"),
                new ArtistSettlementResponse.AmountInfo(64000, "순수익")
        );

        // 차트 데이터 (월별)
        List<ArtistSettlementResponse.ChartDataPoint> salesData = Arrays.asList(
                new ArtistSettlementResponse.ChartDataPoint("2025-01-01", 500000),
                new ArtistSettlementResponse.ChartDataPoint("2025-02-01", 750000),
                new ArtistSettlementResponse.ChartDataPoint("2025-03-01", 650000),
                new ArtistSettlementResponse.ChartDataPoint("2025-04-01", 650000),
                new ArtistSettlementResponse.ChartDataPoint("2025-05-01", 550000),
                new ArtistSettlementResponse.ChartDataPoint("2025-06-01", 800000),
                new ArtistSettlementResponse.ChartDataPoint("2025-07-01", 850000),
                new ArtistSettlementResponse.ChartDataPoint("2025-08-01", 450000),
                new ArtistSettlementResponse.ChartDataPoint("2025-09-01", 800000),
                new ArtistSettlementResponse.ChartDataPoint("2025-10-01", 950000),
                new ArtistSettlementResponse.ChartDataPoint("2025-11-01", 1000000),
                new ArtistSettlementResponse.ChartDataPoint("2025-12-01", 1100000)
        );

        ArtistSettlementResponse.Chart chart = new ArtistSettlementResponse.Chart(
                new ArtistSettlementResponse.ChartSeries(salesData),
                new ArtistSettlementResponse.YDomain(0, 1100000)
        );

        // 테이블 데이터
        List<ArtistSettlementResponse.Settlement> settlements = Arrays.asList(
                new ArtistSettlementResponse.Settlement(
                        910004L,
                        "2025-09-18",
                        new ArtistSettlementResponse.Product(101L, "상품명입니다 상품명입니다"),
                        18000,
                        200,
                        17800,
                        "PENDING",
                        "미지급"
                ),
                new ArtistSettlementResponse.Settlement(
                        910003L,
                        "2025-09-18",
                        new ArtistSettlementResponse.Product(102L, "상품명입니다 상품명입니다"),
                        50000,
                        500,
                        49500,
                        "COMPLETED",
                        "정산 완료"
                ),
                new ArtistSettlementResponse.Settlement(
                        910002L,
                        "2025-09-18",
                        new ArtistSettlementResponse.Product(103L, "상품명입니다 상품명입니다"),
                        30000,
                        1000,
                        29000,
                        "COMPLETED",
                        "정산 완료"
                ),
                new ArtistSettlementResponse.Settlement(
                        910001L,
                        "2025-09-18",
                        new ArtistSettlementResponse.Product(104L, "상품명입니다 상품명입니다"),
                        5000,
                        100,
                        4900,
                        "PENDING",
                        "미지급"
                )
        );

        ArtistSettlementResponse.Table table = new ArtistSettlementResponse.Table(
                settlements,
                page,
                size,
                124,
                7,
                true,
                false
        );

        return new ArtistSettlementResponse(
                scope,
                granularity,
                "Asia/Seoul",
                summary,
                chart,
                table,
                LocalDateTime.now()
        );
    }
}