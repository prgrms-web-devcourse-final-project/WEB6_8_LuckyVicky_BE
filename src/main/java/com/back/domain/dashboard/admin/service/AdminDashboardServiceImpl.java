package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자용 대시보드 서비스 구현체
 * 2025.09.26 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Override
    public AdminOverviewResponse getOverview(String authorization, String adminRole, String range,
                                             String granularity, String period, String timezone) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 통계 데이터 조회

        log.info("관리자 현황 조회 - range: {}, granularity: {}, adminRole: {}", range, granularity, adminRole);

        // 전체 현황 통계
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(12450L, "가입자 수", "명", 234L, 0.019),
                new AdminOverviewResponse.StatInfo(8945L, "주문", "건", 156L, 0.018),
                new AdminOverviewResponse.StatInfo(145780000L, "매출", "원", 19560000L, 0.155),
                new AdminOverviewResponse.StatInfo(2340L, "상품수", "개", 45L, 0.02),
                new AdminOverviewResponse.StatInfo(45L, "펀딩수", "개", 8L, 0.216),
                new AdminOverviewResponse.StatInfo(156L, "작가수", "명", 12L, 0.083)
        );

        // 매출 트렌드 (간소화)
        AdminOverviewResponse.SalesTrend salesTrend = new AdminOverviewResponse.SalesTrend(
                new AdminOverviewResponse.SalesSeries(
                        List.of(
                                new AdminOverviewResponse.DataPoint("2025-12-18", 1250000L),
                                new AdminOverviewResponse.DataPoint("2025-12-24", 2340000L)
                        ),
                        List.of(
                                new AdminOverviewResponse.DataPoint("2025-12-18", 125L),
                                new AdminOverviewResponse.DataPoint("2025-12-24", 234L)
                        )
                ),
                new AdminOverviewResponse.SalesDelta(
                        new AdminOverviewResponse.DeltaInfo(480000L, 0.23),
                        new AdminOverviewResponse.DeltaInfo(18L, 0.084)
                )
        );

        // 사용자 증가 현황 (간소화)
        AdminOverviewResponse.UserGrowth userGrowth = new AdminOverviewResponse.UserGrowth(
                new AdminOverviewResponse.UserSeries(
                        List.of(
                                new AdminOverviewResponse.DataPoint("2025-07-01", 10200L),
                                new AdminOverviewResponse.DataPoint("2025-12-01", 12450L)
                        ),
                        List.of(
                                new AdminOverviewResponse.DataPoint("2025-07-01", 120L),
                                new AdminOverviewResponse.DataPoint("2025-12-01", 156L)
                        )
                ),
                new AdminOverviewResponse.UserDelta(
                        new AdminOverviewResponse.DeltaInfo(450L, 0.037),
                        new AdminOverviewResponse.DeltaInfo(36L, 0.30)
                )
        );

        // 카테고리 분포 (간소화)
        AdminOverviewResponse.CategoryDistribution categoryDistribution = 
                new AdminOverviewResponse.CategoryDistribution(
                        "2025-12-24", 
                        2340, 
                        List.of(
                                new AdminOverviewResponse.CategoryBucket(1L, "스티커", 820, 0.35),
                                new AdminOverviewResponse.CategoryBucket(2L, "다이어리", 420, 0.179),
                                new AdminOverviewResponse.CategoryBucket(3L, "포스터", 360, 0.154)
                        )
                );

        // 차트 데이터 통합
        AdminOverviewResponse.Charts charts = new AdminOverviewResponse.Charts(
                new AdminOverviewResponse.ChartMeta(range, granularity, timezone),
                salesTrend, 
                userGrowth, 
                categoryDistribution
        );

        // 승인 대기 알림 (간소화)
        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                List.of(
                        new AdminOverviewResponse.ArtistApproval(1001L, "작가A", 
                                LocalDateTime.of(2025, 12, 23, 9, 10))
                ),
                List.of(
                        new AdminOverviewResponse.FundingApproval(456789L, "한정 제품", 
                                LocalDateTime.of(2025, 12, 23, 10, 15))
                )
        );

        return new AdminOverviewResponse(overview, charts, alerts, LocalDateTime.now(), timezone);
    }

    @Override
    public AdminProductResponse getProducts(String authorization, String adminRole, int page, int size,
                                            String keyword, String sellingStatus, Long categoryId, Long artistId,
                                            String startDate, String endDate, String sort, String order, boolean metrics) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 상품 데이터 조회 및 필터링
        // TODO: 동적 정렬 및 페이징 처리

        log.info("관리자 상품 목록 조회 - page: {}, size: {}, keyword: {}, sellingStatus: {}, categoryId: {}, artistId: {}, metrics: {}, adminRole: {}",
                page, size, keyword, sellingStatus, categoryId, artistId, metrics, adminRole);

        // 요약 정보 (더미 데이터)
        AdminProductResponse.Summary summary = new AdminProductResponse.Summary(
                2340, 2105, 235
        );

        // 상품 목록 (더미 데이터)
        List<AdminProductResponse.Product> products = List.of(
                new AdminProductResponse.Product(
                        123357L,
                        "0123357",
                        "상품명입니다 상품명입니다",
                        new AdminProductResponse.Artist(9001L, "작가이름입니다"),
                        "SELLING",
                        new AdminProductResponse.Category(1L, "스티커"),
                        LocalDate.of(2025, 9, 18),
                        new AdminProductResponse.Permissions(true, true, true),
                        metrics ? 4.5 : null,
                        metrics ? 12 : null,
                        metrics ? 2250000L : null,
                        new AdminProductResponse.Moderation(false, null)
                ),
                new AdminProductResponse.Product(
                        123356L,
                        "0123356",
                        "상품명입니다 상품명입니다",
                        new AdminProductResponse.Artist(9002L, "작가가나다"),
                        "STOPPED",
                        new AdminProductResponse.Category(3L, "포스터"),
                        LocalDate.of(2025, 9, 18),
                        new AdminProductResponse.Permissions(true, true, true),
                        metrics ? 4.2 : null,
                        metrics ? 8 : null,
                        metrics ? 1800000L : null,
                        new AdminProductResponse.Moderation(false, null)
                )
        );

        // 페이지 정보
        int totalElements = 2340;
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new AdminProductResponse(
                summary, products, page, size, totalElements, totalPages, hasNext, hasPrevious
        );
    }

    @Override
    public AdminUserResponse getUsers(String authorization, String adminRole, int page, int size,
                                      String keyword, String role, String accountStatus, String grade,
                                      String joinedStartDate, String joinedEndDate, Long artistId,
                                      String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 사용자 데이터 조회 및 필터링
        // TODO: 동적 정렬 및 페이징 처리

        log.info("관리자 사용자 목록 조회 - page: {}, size: {}, keyword: {}, role: {}, accountStatus: {}, grade: {}, artistId: {}, adminRole: {}",
                page, size, keyword, role, accountStatus, grade, artistId, adminRole);

        // 요약 정보 (더미 데이터)
        AdminUserResponse.Summary summary = new AdminUserResponse.Summary(
                13240, 12810, 280, 150, 1000
        );

        // 사용자 목록 (더미 데이터)
        List<AdminUserResponse.User> users = List.of(
                new AdminUserResponse.User(
                        100136L,
                        "abc136",
                        "닉네임입니다",
                        "USER",
                        new AdminUserResponse.Artist(null, null),
                        new AdminUserResponse.Grade("SEED", "새싹"),
                        "ACTIVE",
                        LocalDate.of(2025, 9, 18),
                        LocalDateTime.of(2025, 9, 18, 10, 20, 0),
                        new AdminUserResponse.Permissions(true, false)
                ),
                new AdminUserResponse.User(
                        100131L,
                        "abc131",
                        "작가명입니다",
                        "ARTIST",
                        new AdminUserResponse.Artist(90031L, "작가명입니다"),
                        new AdminUserResponse.Grade("SEED", "새싹"),
                        "ACTIVE",
                        LocalDate.of(2025, 9, 18),
                        LocalDateTime.of(2025, 9, 18, 8, 5, 0),
                        new AdminUserResponse.Permissions(true, false)
                ),
                new AdminUserResponse.User(
                        100129L,
                        "abc129",
                        "작가명입니다",
                        "ARTIST",
                        new AdminUserResponse.Artist(90029L, "작가명입니다"),
                        new AdminUserResponse.Grade("SEED", "새싹"),
                        "SUSPENDED",
                        LocalDate.of(2025, 9, 18),
                        LocalDateTime.of(2025, 9, 17, 22, 10, 0),
                        new AdminUserResponse.Permissions(true, false)
                ),
                new AdminUserResponse.User(
                        100123L,
                        "abc123",
                        "작가명입니다",
                        "ARTIST",
                        new AdminUserResponse.Artist(90023L, "작가명입니다"),
                        new AdminUserResponse.Grade("SEED", "새싹"),
                        "BLACKLISTED",
                        LocalDate.of(2025, 9, 18),
                        LocalDateTime.of(2025, 9, 16, 13, 30, 0),
                        new AdminUserResponse.Permissions(false, true)
                )
        );

        // 페이지 정보
        int totalElements = 13240;
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new AdminUserResponse(
                summary, users, page, size, totalElements, totalPages, hasNext, hasPrevious
        );
    }

    @Override
    public AdminSettlementResponse getSettlements(String authorization, String adminRole, Integer year,
                                                  Integer month, String granularity, String timezone) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 매출/정산 데이터 조회

        // 연도 기본값 설정 (현재 연도)
        if (year == null) {
            year = Year.now().getValue();
        }

        log.info("관리자 매출/정산 조회 - year: {}, month: {}, granularity: {}, timezone: {}, adminRole: {}",
                year, month, granularity, timezone, adminRole);

        // 조회 범위
        AdminSettlementResponse.Scope scope = new AdminSettlementResponse.Scope(year, month);

        // 요약 정보
        AdminSettlementResponse.Summary summary = new AdminSettlementResponse.Summary(
                108000000L,  // 총 매출액
                12000000L,   // 총 작가 정산금
                96000000L    // 총 순수익
        );

        // 차트 데이터 생성
        List<AdminSettlementResponse.DataPoint> grossSalesData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> artistPayoutData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> netIncomeData = new ArrayList<>();
        List<AdminSettlementResponse.TableRow> tableData = new ArrayList<>();

        if (month != null) {
            // 일별 집계 (해당 월의 모든 일자)
            LocalDate startDate = LocalDate.of(year, month, 1);
            int daysInMonth = startDate.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                String bucketStart = String.format("%d-%02d-%02d", year, month, day);
                long dailyGrossSales = 3000000L + (day * 50000L);
                long dailyArtistPayout = dailyGrossSales / 10;
                long dailyNetIncome = dailyGrossSales - dailyArtistPayout;

                grossSalesData.add(new AdminSettlementResponse.DataPoint(bucketStart, dailyGrossSales));
                artistPayoutData.add(new AdminSettlementResponse.DataPoint(bucketStart, dailyArtistPayout));
                netIncomeData.add(new AdminSettlementResponse.DataPoint(bucketStart, dailyNetIncome));

                tableData.add(new AdminSettlementResponse.TableRow(
                        bucketStart, dailyGrossSales, dailyArtistPayout, dailyNetIncome
                ));
            }
        } else {
            // 월별 집계 (해당 연도의 모든 월)
            for (int m = 1; m <= 12; m++) {
                String bucketStart = String.format("%d-%02d-01", year, m);
                long monthlyGrossSales = 10000000L + (m * 500000L);
                long monthlyArtistPayout = monthlyGrossSales / 10;
                long monthlyNetIncome = monthlyGrossSales - monthlyArtistPayout;

                grossSalesData.add(new AdminSettlementResponse.DataPoint(bucketStart, monthlyGrossSales));
                artistPayoutData.add(new AdminSettlementResponse.DataPoint(bucketStart, monthlyArtistPayout));
                netIncomeData.add(new AdminSettlementResponse.DataPoint(bucketStart, monthlyNetIncome));

                tableData.add(new AdminSettlementResponse.TableRow(
                        bucketStart, monthlyGrossSales, monthlyArtistPayout, monthlyNetIncome
                ));
            }
        }

        // 차트 데이터 통합
        AdminSettlementResponse.Chart chart = new AdminSettlementResponse.Chart(
                new AdminSettlementResponse.Series(grossSalesData, artistPayoutData, netIncomeData)
        );

        return new AdminSettlementResponse(
                scope, granularity, timezone, summary, chart, tableData, LocalDateTime.now()
        );
    }

    @Override
    public AdminFundingResponse getFundings(String authorization, String adminRole, int page, int size,
                                            String keyword, String status, Long categoryId, Long artistId,
                                            Integer minAchievement, Integer maxAchievement,
                                            String registeredFrom, String registeredTo,
                                            String dueFrom, String dueTo, String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 펀딩 데이터 조회 및 필터링
        // TODO: 동적 정렬 및 페이징 처리

        log.info("관리자 펀딩 목록 조회 - page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, artistId: {}, adminRole: {}",
                page, size, keyword, status, categoryId, artistId, adminRole);

        // 요약 정보 (더미 데이터)
        AdminFundingResponse.Summary summary = new AdminFundingResponse.Summary(
                120, 86, 5, 23, 6
        );

        // 펀딩 목록 (더미 데이터)
        List<AdminFundingResponse.Funding> fundings = List.of(
                new AdminFundingResponse.Funding(
                        456789L,
                        "펀딩 제목입니다 펀딩 제목입니다",
                        new AdminFundingResponse.Artist(90036L, "abc136", "작가명입니다"),
                        new AdminFundingResponse.Category(1L, "스티커"),
                        "ACTIVE",
                        1000000L,
                        1000000L,
                        100,
                        45,
                        "2025-09-18",
                        "2025-09-01",
                        15,
                        "https://example.com/image.jpg",
                        new AdminFundingResponse.Permissions(true, true),
                        new AdminFundingResponse.Flags(true, false)
                ),
                new AdminFundingResponse.Funding(
                        456788L,
                        "펀딩 제목입니다",
                        new AdminFundingResponse.Artist(90035L, "abc135", "작가명입니다"),
                        new AdminFundingResponse.Category(2L, "다이어리"),
                        "ACTIVE",
                        600000L,
                        9000000L,
                        1500,
                        220,
                        "2025-09-18",
                        "2025-08-20",
                        12,
                        "https://example.com/image2.jpg",
                        new AdminFundingResponse.Permissions(true, true),
                        new AdminFundingResponse.Flags(true, true)
                )
        );

        // 페이지 정보
        int totalElements = 120;
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new AdminFundingResponse(
                summary, fundings, page, size, totalElements, totalPages, hasNext, hasPrevious
        );
    }
}
