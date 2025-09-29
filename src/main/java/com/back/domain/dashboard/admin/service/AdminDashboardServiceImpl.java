package com.back.domain.dashboard.admin.service;

import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationDetailResponse;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationResponse;
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
 * 2025.09.28 수정
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

        // 전체 현황 통계 (최소한의 더미 데이터)
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(12450L, "가입자 수", "명", 234L, 0.019),
                new AdminOverviewResponse.StatInfo(8945L, "주문", "건", 156L, 0.018),
                new AdminOverviewResponse.StatInfo(145780000L, "매출", "원", 19560000L, 0.155),
                new AdminOverviewResponse.StatInfo(2340L, "상품수", "개", 45L, 0.02),
                new AdminOverviewResponse.StatInfo(45L, "펀딩수", "개", 8L, 0.216),
                new AdminOverviewResponse.StatInfo(156L, "작가수", "명", 12L, 0.083)
        );

        // 차트 데이터 (최소한)
        AdminOverviewResponse.Charts charts = new AdminOverviewResponse.Charts(
                new AdminOverviewResponse.ChartMeta(range, granularity, timezone),
                new AdminOverviewResponse.SalesTrend(
                        new AdminOverviewResponse.SalesSeries(
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-24", 2340000L)),
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-24", 234L))
                        ),
                        new AdminOverviewResponse.SalesDelta(
                                new AdminOverviewResponse.DeltaInfo(480000L, 0.23),
                                new AdminOverviewResponse.DeltaInfo(18L, 0.084)
                        )
                ),
                new AdminOverviewResponse.UserGrowth(
                        new AdminOverviewResponse.UserSeries(
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-01", 12450L)),
                                List.of(new AdminOverviewResponse.DataPoint("2025-12-01", 156L))
                        ),
                        new AdminOverviewResponse.UserDelta(
                                new AdminOverviewResponse.DeltaInfo(450L, 0.037),
                                new AdminOverviewResponse.DeltaInfo(36L, 0.30)
                        )
                ),
                new AdminOverviewResponse.CategoryDistribution("2025-12-24", 2340,
                        List.of(new AdminOverviewResponse.CategoryBucket(1L, "스티커", 820, 0.35)))
        );

        // 승인 대기 알림 (최소한)
        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                List.of(new AdminOverviewResponse.ArtistApproval(1001L, "작가A", LocalDateTime.of(2025, 12, 23, 9, 10))),
                List.of(new AdminOverviewResponse.FundingApproval(456789L, "한정 제품", LocalDateTime.of(2025, 12, 23, 10, 15)))
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

        // 상품 목록 (최소한의 더미 데이터)
        List<AdminProductResponse.Product> products = List.of(
                new AdminProductResponse.Product(
                        123357L, "0123357", "상품명입니다",
                        new AdminProductResponse.Artist(9001L, "작가이름입니다"),
                        "SELLING", new AdminProductResponse.Category(1L, "스티커"),
                        LocalDate.of(2025, 9, 18), new AdminProductResponse.Permissions(true, true, true),
                        metrics ? 4.5 : null, metrics ? 12 : null, metrics ? 2250000L : null,
                        new AdminProductResponse.Moderation(false, null)
                ),
                new AdminProductResponse.Product(
                        123356L, "0123356", "상품명입니다2",
                        new AdminProductResponse.Artist(9002L, "작가가나다"),
                        "STOPPED", new AdminProductResponse.Category(3L, "포스터"),
                        LocalDate.of(2025, 9, 18), new AdminProductResponse.Permissions(true, true, true),
                        metrics ? 4.2 : null, metrics ? 8 : null, metrics ? 1800000L : null,
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

        // 사용자 목록 (최소한의 더미 데이터)
        List<AdminUserResponse.User> users = List.of(
                new AdminUserResponse.User(
                        100136L, "abc136", "닉네임입니다", "USER",
                        new AdminUserResponse.Artist(null, null),
                        new AdminUserResponse.Grade("SEED", "새싹"), "ACTIVE",
                        LocalDate.of(2025, 9, 18), LocalDateTime.of(2025, 9, 18, 10, 20, 0),
                        new AdminUserResponse.Permissions(true, false)
                ),
                new AdminUserResponse.User(
                        100131L, "abc131", "작가명입니다", "ARTIST",
                        new AdminUserResponse.Artist(90031L, "작가명입니다"),
                        new AdminUserResponse.Grade("SEED", "새싹"), "ACTIVE",
                        LocalDate.of(2025, 9, 18), LocalDateTime.of(2025, 9, 18, 8, 5, 0),
                        new AdminUserResponse.Permissions(true, false)
                ),
                new AdminUserResponse.User(
                        100123L, "abc123", "블랙리스트사용자", "ARTIST",
                        new AdminUserResponse.Artist(90023L, "블랙리스트작가"),
                        new AdminUserResponse.Grade("SEED", "새싹"), "BLACKLISTED",
                        LocalDate.of(2025, 9, 18), LocalDateTime.of(2025, 9, 16, 13, 30, 0),
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

        // 차트 데이터 생성 (간소화)
        List<AdminSettlementResponse.DataPoint> grossSalesData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> artistPayoutData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> netIncomeData = new ArrayList<>();
        List<AdminSettlementResponse.TableRow> tableData = new ArrayList<>();

        if (month != null) {
            // 일별 집계 (해당 월의 모든 일자)
            LocalDate startDate = LocalDate.of(year, month, 1);
            int daysInMonth = startDate.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                addDataPoint(year, month, day, grossSalesData, artistPayoutData, netIncomeData, tableData);
            }
        } else {
            // 월별 집계 (해당 연도의 모든 월)
            for (int m = 1; m <= 12; m++) {
                addDataPoint(year, m, 1, grossSalesData, artistPayoutData, netIncomeData, tableData);
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

    /**
     * 정산 데이터 포인트 생성 헬퍼 메서드
     */
    private void addDataPoint(int year, int monthOrMonth, int dayOrOne,
                              List<AdminSettlementResponse.DataPoint> grossSalesData,
                              List<AdminSettlementResponse.DataPoint> artistPayoutData,
                              List<AdminSettlementResponse.DataPoint> netIncomeData,
                              List<AdminSettlementResponse.TableRow> tableData) {
        String bucketStart = String.format("%d-%02d-%02d", year, monthOrMonth, dayOrOne);
        long grossSales = 3000000L + (monthOrMonth * 50000L) + (dayOrOne * 10000L);
        long artistPayout = grossSales / 10;
        long netIncome = grossSales - artistPayout;

        grossSalesData.add(new AdminSettlementResponse.DataPoint(bucketStart, grossSales));
        artistPayoutData.add(new AdminSettlementResponse.DataPoint(bucketStart, artistPayout));
        netIncomeData.add(new AdminSettlementResponse.DataPoint(bucketStart, netIncome));
        tableData.add(new AdminSettlementResponse.TableRow(bucketStart, grossSales, artistPayout, netIncome));
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

        // 펀딩 목록 (최소한의 더미 데이터)
        List<AdminFundingResponse.Funding> fundings = List.of(
                new AdminFundingResponse.Funding(
                        456789L, "펀딩 제목입니다",
                        new AdminFundingResponse.Artist(90036L, "abc136", "작가명입니다"),
                        new AdminFundingResponse.Category(1L, "스티커"), "ACTIVE",
                        1000000L, 1000000L, 100, 45, "2025-09-18", "2025-09-01", 15,
                        "https://example.com/image.jpg",
                        new AdminFundingResponse.Permissions(true, true),
                        new AdminFundingResponse.Flags(true, false)
                ),
                new AdminFundingResponse.Funding(
                        456788L, "펀딩 제목2",
                        new AdminFundingResponse.Artist(90035L, "abc135", "작가명2"),
                        new AdminFundingResponse.Category(2L, "다이어리"), "ACTIVE",
                        600000L, 900000L, 150, 220, "2025-09-18", "2025-08-20", 12,
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

    @Override
    public AdminArtistApplicationResponse getArtistApplications(String authorization, String adminRole, int page, int size,
                                                                String keyword, String status,
                                                                String submittedFrom, String submittedTo,
                                                                String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 입점 신청 데이터 조회 및 필터링
        // TODO: 동적 정렬 및 페이징 처리

        log.info("관리자 입점 신청 목록 조회 - page: {}, size: {}, keyword: {}, status: {}, adminRole: {}",
                page, size, keyword, status, adminRole);

        // 요약 정보 (더미 데이터)
        AdminArtistApplicationResponse.Summary summary = new AdminArtistApplicationResponse.Summary(
                120, 86, 23, 11
        );

        // 입점 신청 목록 (최소한의 더미 데이터)
        List<AdminArtistApplicationResponse.Application> applications = List.of(
                new AdminArtistApplicationResponse.Application(
                        80136L, new AdminArtistApplicationResponse.Artist("abc136", "작가명입니다"),
                        "PENDING", "2025-09-18", new AdminArtistApplicationResponse.Permissions(true, true)
                ),
                new AdminArtistApplicationResponse.Application(
                        80135L, new AdminArtistApplicationResponse.Artist("abc135", "작가명2"),
                        "PENDING", "2025-09-18", new AdminArtistApplicationResponse.Permissions(true, true)
                ),
                new AdminArtistApplicationResponse.Application(
                        80134L, new AdminArtistApplicationResponse.Artist("abc134", "승인된작가"),
                        "APPROVED", "2025-09-17", new AdminArtistApplicationResponse.Permissions(false, false)
                ),
                new AdminArtistApplicationResponse.Application(
                        80133L, new AdminArtistApplicationResponse.Artist("abc133", "거절된작가"),
                        "REJECTED", "2025-09-16", new AdminArtistApplicationResponse.Permissions(false, false)
                )
        );

        // 페이지 정보
        int totalElements = 120;
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new AdminArtistApplicationResponse(
                summary, applications, page, size, totalElements, totalPages, hasNext, hasPrevious
        );
    }

    @Override
    public AdminArtistApplicationDetailResponse getArtistApplicationDetail(String authorization, String adminRole, Long applicationId) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: 실제 데이터베이스에서 입점 신청 상세 데이터 조회
        // TODO: 신청 ID 존재 여부 검증

        log.info("관리자 입점 신청 상세 조회 - applicationId: {}, adminRole: {}", applicationId, adminRole);

        // 작가 정보 (최소한의 더미 데이터)
        AdminArtistApplicationDetailResponse.Artist artist = new AdminArtistApplicationDetailResponse.Artist(
                100123L, "abc123", "작가명입니다", null);

        // 연락처 정보
        AdminArtistApplicationDetailResponse.Contact contact = new AdminArtistApplicationDetailResponse.Contact(
                "abc123@abc.com", "010-1234-5678");

        // 사업자 정보
        AdminArtistApplicationDetailResponse.Business business = new AdminArtistApplicationDetailResponse.Business(
                "123-45-67890", "2025-서울강남-1234", "서울특별시 강남구 테헤란로 123 2층");

        // 프로필 정보 (최소한)
        AdminArtistApplicationDetailResponse.Profile profile = new AdminArtistApplicationDetailResponse.Profile(
                List.of("스티커", "메모지"),
                List.of(new AdminArtistApplicationDetailResponse.SnsInfo("Instagram", "@moriomori_official")),
                List.of(new AdminArtistApplicationDetailResponse.PortfolioFile(
                        "pf-1", "포트폴리오.pdf", "https://example.com/signed/pf-1"))
        );

        // 검토 정보
        AdminArtistApplicationDetailResponse.Review review = new AdminArtistApplicationDetailResponse.Review(
                null, new AdminArtistApplicationDetailResponse.Verifications(true, true));

        // 결정 정보 (PENDING 상태이므로 null)
        AdminArtistApplicationDetailResponse.Decision decision = new AdminArtistApplicationDetailResponse.Decision(
                null, null, null, null);

        // 권한 정보 (PENDING 상태이므로 승인/거절 가능)
        AdminArtistApplicationDetailResponse.Permissions permissions = new AdminArtistApplicationDetailResponse.Permissions(
                true, true);

        return new AdminArtistApplicationDetailResponse(
                80123L, "PENDING", LocalDateTime.of(2025, 9, 18, 10, 20, 0),
                artist, contact, business, profile, review, decision, permissions
        );
    }
}
