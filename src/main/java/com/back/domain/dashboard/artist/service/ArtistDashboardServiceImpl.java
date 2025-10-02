package com.back.domain.dashboard.artist.service;

import com.back.domain.dashboard.artist.dto.request.*;
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
import com.back.domain.dashboard.artist.dto.response.ArtistTrafficSourceResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 작가용 대시보드 서비스 구현체
 * 2025.09.29 수정 - getProducts() 실제 DB 연동
 * 2025.09.30 수정 - getFundings() 실제 DB 연동
 * 2025.10.01 추가 - getTrafficSources() GA4 유입 경로 분석
 * 2025.10.02 JWT 표준 패턴 적용 - JWT 파싱 제거, Request DTO 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArtistDashboardServiceImpl implements ArtistDashboardService {

    private final ProductRepository productRepository;
    private final FundingRepository fundingRepository;
    private final FundingContributionRepository fundingContributionRepository;
    private final BetaAnalyticsDataClient analyticsDataClient;

    @Value("${google.analytics.property-id}")
    private String propertyId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    @Override
    public ArtistMainResponse getMainStats(Long artistId, ArtistMainStatsRequest request) {
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

        // 유입 경로 정보 (GA4) - 7일 기준
        ArtistMainResponse.TrafficSources trafficSources = getTrafficSourcesForMain(artistId, request.tz());

        return new ArtistMainResponse(
                profile,
                stats,
                trends,
                notifications,
                trafficSources,
                LocalDateTime.now(),
                "Asia/Seoul"
        );
    }

    /**
     * 메인 대시보드용 유입 경로 데이터 조회 (간소화 버전)
     */
    private ArtistMainResponse.TrafficSources getTrafficSourcesForMain(Long artistId, String timezone) {
        try {
            // 기존 getTrafficSources 메서드 호출 (7일 기준)
            ArtistTrafficSourceResponse fullResponse = getTrafficSources(artistId, 7, timezone);

            // 메인 대시보드용으로 간소화
            ArtistMainResponse.Summary summary = new ArtistMainResponse.Summary(
                    fullResponse.summary().totalSessions(),
                    fullResponse.summary().totalUsers(),
                    fullResponse.summary().conversions(),
                    fullResponse.summary().conversionRate(),
                    fullResponse.summary().topSource()
            );

            // 상위 5개 유입 경로만
            List<ArtistMainResponse.Source> sources = fullResponse.sources().stream()
                    .limit(5)
                    .map(source -> new ArtistMainResponse.Source(
                            source.name(),
                            source.sessions(),
                            source.users(),
                            source.share()
                    ))
                    .toList();

            // 차트 데이터
            List<ArtistMainResponse.ChartData> chartData = fullResponse.chart().data().stream()
                    .limit(5)
                    .map(data -> new ArtistMainResponse.ChartData(
                            data.name(),
                            data.value(),
                            data.percentage(),
                            data.color()
                    ))
                    .toList();

            ArtistMainResponse.Chart chart = new ArtistMainResponse.Chart(chartData);

            return new ArtistMainResponse.TrafficSources(summary, sources, chart);

        } catch (Exception e) {
            log.error("메인 대시보드 유입 경로 조회 중 오류 발생", e);
            
            // 오류 발생 시 빈 데이터 반환
            return new ArtistMainResponse.TrafficSources(
                    new ArtistMainResponse.Summary(0, 0, 0, 0.0, "없음"),
                    List.of(),
                    new ArtistMainResponse.Chart(List.of())
            );
        }
    }

    @Override
    public ArtistProductResponse.List getProducts(Long artistId, ArtistProductSearchRequest request) {
        log.info("작가 상품 목록 조회 시작 - artistId: {}, page: {}, size: {}, keyword: {}, selling: {}, sort: {}, order: {}",
                artistId, request.page(), request.size(), request.keyword(), request.selling(), request.sort(), request.order());

        // Repository를 통한 실제 DB 조회
        Page<Product> productPage = productRepository.findProductsByArtist(
                artistId, request.keyword(), request.selling(), request.sort(), request.order(), 
                PageRequest.of(request.page(), request.size())
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

        return new ArtistProductResponse.List(content, request.page(), request.size(), totalElements, totalPages, hasNext, hasPrevious);
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
    public ArtistCashResponse.Balance getCashBalance(Long artistId) {
        // TODO: 실제 데이터베이스에서 지갑 잔액 정보 조회
        log.info("작가 지갑 잔액 조회 - artistId: {}", artistId);

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
    public ArtistCashHistoryResponse.List getCashHistory(Long artistId, ArtistCashHistorySearchRequest request) {
        // TODO: 실제 데이터베이스에서 캐시 거래 내역 조회
        log.info("작가 캐시 내역 조회 - artistId: {}, page: {}, size: {}, type: {}",
                artistId, request.page(), request.size(), request.type());

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
                request.page(),
                request.size(),
                content.size(),
                1,
                false,
                false
        );
    }

    @Override
    public ArtistOrderResponse.List getOrders(Long artistId, ArtistOrderSearchRequest request) {
        // TODO: 실제 데이터베이스에서 주문 목록 조회
        log.info("작가 주문 내역 조회 - artistId: {}, page: {}, size: {}, status: {}",
                artistId, request.page(), request.size(), request.status());

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
                request.page(),
                request.size(),
                156,
                8,
                request.page() < 7,
                request.page() > 0
        );
    }

    @Override
    public ArtistCancellationResponse.List getCancellationRequests(Long artistId, ArtistCancellationSearchRequest request) {
        // TODO: 실제 데이터베이스에서 취소 요청 목록 조회
        log.info("작가 취소 요청 목록 조회 - artistId: {}, page: {}, size: {}, status: {}",
                artistId, request.page(), request.size(), request.status());

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
                request.page(),
                request.size(),
                8,
                1,
                false,
                false
        );
    }

    @Override
    public ArtistExchangeResponse.List getExchangeRequests(Long artistId, ArtistExchangeSearchRequest request) {
        // TODO: 실제 데이터베이스에서 교환 요청 목록 조회
        log.info("작가 교환 요청 목록 조회 - artistId: {}, page: {}, size: {}, status: {}",
                artistId, request.page(), request.size(), request.status());

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
                request.page(),
                request.size(),
                5,
                1,
                false,
                false
        );
    }

    @Override
    public ArtistSettingsResponse getSettings(Long artistId) {
        // TODO: 실제 데이터베이스에서 작가 설정 정보 조회
        log.info("작가 설정 정보 조회 - artistId: {}", artistId);

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
    public ArtistFundingResponse.List getFundings(Long artistId, ArtistFundingSearchRequest request) {
        log.info("작가 펀딩 목록 조회 시작 - artistId: {}, page: {}, size: {}, keyword: {}, status: {}, sort: {}, order: {}",
                artistId, request.page(), request.size(), request.keyword(), request.status(), request.sort(), request.order());

        // status 문자열을 FundingStatus enum으로 변환
        FundingStatus fundingStatus = null;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                fundingStatus = FundingStatus.valueOf(request.status());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 펀딩 상태값: {}", request.status());
            }
        }

        // Repository를 통한 실제 DB 조회
        Page<Funding> fundingPage = fundingRepository.findFundingsByArtist(
                artistId, request.keyword(), fundingStatus, request.sort(), request.order(), 
                PageRequest.of(request.page(), request.size())
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
                request.page(), request.size(), totalElements, totalPages, hasNext, hasPrevious
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
    public ArtistSettlementResponse getSettlements(Long artistId, ArtistSettlementSearchRequest request) {
        // TODO: 실제 데이터베이스에서 정산 내역 조회
        // TODO: 연도가 null이면 서버 현재 연도 사용

        // 조회 범위 - month 파라미터를 그대로 전달
        ArtistSettlementResponse.Scope scope = new ArtistSettlementResponse.Scope(
                request.year() != null ? request.year() : 2025, request.month()
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
                request.page(),
                request.size(),
                124,
                7,
                true,
                false
        );

        return new ArtistSettlementResponse(
                scope,
                request.granularity(),
                "Asia/Seoul",
                summary,
                chart,
                table,
                LocalDateTime.now()
        );
    }

    @Override
    public ArtistTrafficSourceResponse getTrafficSources(Long artistId, int days, String timezone) {
        log.info("작가 유입 경로 조회 - artistId: {}, days: {}, timezone: {}", artistId, days, timezone);

        try {
            // GA4 API 요청 생성 (특정 작가 필터링)
            String campaignFilter = "artist_" + artistId;
            
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(propertyId)
                    // 측정 기준: 세션 소스 (Instagram, YouTube 등)
                    .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                    // 지표: 세션 수, 사용자 수, 신규 사용자, 전환 수
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("totalUsers"))
                    .addMetrics(Metric.newBuilder().setName("newUsers"))
                    .addMetrics(Metric.newBuilder().setName("conversions"))
                    // 조회 기간 설정
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
                    // 특정 작가의 캠페인만 필터링
                    .setDimensionFilter(
                        FilterExpression.newBuilder()
                            .setFilter(Filter.newBuilder()
                                .setFieldName("sessionCampaignName")
                                .setStringFilter(Filter.StringFilter.newBuilder()
                                    .setMatchType(Filter.StringFilter.MatchType.CONTAINS)
                                    .setValue(campaignFilter)
                                )
                            )
                    )
                    // 세션 수 기준 내림차순 정렬
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("sessions"))
                            .setDesc(true))
                    .build();

            // GA4 API 호출
            RunReportResponse response = analyticsDataClient.runReport(request);

            // 총합 계산
            long totalSessions = 0;
            long totalUsers = 0;
            long totalNewUsers = 0;
            long totalConversions = 0;

            List<ArtistTrafficSourceResponse.Source> sources = new ArrayList<>();
            List<ArtistTrafficSourceResponse.ChartData> chartData = new ArrayList<>();

            // 색상 팔레트 (소셜 미디어별)
            java.util.Map<String, String> colorMap = java.util.Map.of(
                    "instagram", "#E4405F",
                    "youtube", "#FF0000",
                    "naver", "#03C75A",
                    "google", "#4285F4",
                    "facebook", "#1877F2",
                    "twitter", "#1DA1F2",
                    "kakao", "#FEE500"
            );

            // 응답 데이터 처리
            for (Row row : response.getRowsList()) {
                String sourceName = row.getDimensionValues(0).getValue();
                long sessions = Long.parseLong(row.getMetricValues(0).getValue());
                long users = Long.parseLong(row.getMetricValues(1).getValue());
                long newUsers = Long.parseLong(row.getMetricValues(2).getValue());
                long conversions = Long.parseLong(row.getMetricValues(3).getValue());

                totalSessions += sessions;
                totalUsers += users;
                totalNewUsers += newUsers;
                totalConversions += conversions;

                // 색상 선택 (소문자로 매칭)
                String color = colorMap.getOrDefault(sourceName.toLowerCase(), "#999999");

                sources.add(new ArtistTrafficSourceResponse.Source(
                        sourceName,
                        sessions,
                        users,
                        0.0, // 점유율은 나중에 계산
                        newUsers,
                        users > 0 ? (double) newUsers / users * 100 : 0.0,
                        conversions,
                        sessions > 0 ? (double) conversions / sessions * 100 : 0.0
                ));

                chartData.add(new ArtistTrafficSourceResponse.ChartData(
                        sourceName,
                        sessions,
                        0.0, // 퍼센트는 나중에 계산
                        color
                ));
            }

            // 점유율 계산 (총 세션 대비)
            final long finalTotalSessions = totalSessions;
            sources = sources.stream()
                    .map(source -> new ArtistTrafficSourceResponse.Source(
                            source.name(),
                            source.sessions(),
                            source.users(),
                            finalTotalSessions > 0 ? (double) source.sessions() / finalTotalSessions * 100 : 0.0,
                            source.newUsers(),
                            source.newUserRate(),
                            source.conversions(),
                            source.conversionRate()
                    ))
                    .toList();

            chartData = chartData.stream()
                    .map(data -> new ArtistTrafficSourceResponse.ChartData(
                            data.name(),
                            data.value(),
                            finalTotalSessions > 0 ? (double) data.value() / finalTotalSessions * 100 : 0.0,
                            data.color()
                    ))
                    .toList();

            // 가장 많은 유입 경로
            String topSource = sources.isEmpty() ? "없음" : sources.get(0).name();

            // 전환율 계산
            double conversionRate = totalSessions > 0 
                    ? (double) totalConversions / totalSessions * 100 
                    : 0.0;

            // 요약 정보
            ArtistTrafficSourceResponse.Summary summary = new ArtistTrafficSourceResponse.Summary(
                    totalSessions,
                    totalUsers,
                    totalConversions,
                    conversionRate,
                    topSource
            );

            // 차트 데이터
            ArtistTrafficSourceResponse.Chart chart = new ArtistTrafficSourceResponse.Chart(
                    chartData,
                    5.0 // 5% 미만은 "기타"로 그룹화
            );

            // 조회 기간
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            ArtistTrafficSourceResponse.Period period = new ArtistTrafficSourceResponse.Period(
                    startDate.toString(),
                    endDate.toString(),
                    days
            );

            log.info("작가 유입 경로 조회 완료 - artistId: {}, 총 {}개 소스, 총 세션 {}", 
                     artistId, sources.size(), totalSessions);

            return new ArtistTrafficSourceResponse(
                    summary,
                    sources,
                    chart,
                    period,
                    LocalDateTime.now(),
                    timezone
            );

        } catch (Exception e) {
            log.error("작가 유입 경로 조회 중 오류 발생 - artistId: {}", artistId, e);
            
            // 오류 발생 시 빈 데이터 반환
            return new ArtistTrafficSourceResponse(
                    new ArtistTrafficSourceResponse.Summary(0, 0, 0, 0.0, "없음"),
                    List.of(),
                    new ArtistTrafficSourceResponse.Chart(List.of(), 5.0),
                    new ArtistTrafficSourceResponse.Period(
                            LocalDate.now().minusDays(days).toString(),
                            LocalDate.now().toString(),
                            days
                    ),
                    LocalDateTime.now(),
                    timezone
            );
        }
    }
}