package com.back.domain.dashboard.admin.service;

import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.dashboard.admin.dto.response.*;
import com.google.analytics.data.v1beta.*;
import org.springframework.beans.factory.annotation.Value;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.Status;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자용 대시보드 서비스 구현체
 * 2025.10.01 DB 연동 작업 완료
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FundingRepository fundingRepository;
    private final ArtistApplicationRepository artistApplicationRepository;
    private final BetaAnalyticsDataClient analyticsDataClient;

    @Value("${google.analytics.property-id}")
    private String propertyId;

    @Override
    public AdminOverviewResponse getOverview(String authorization, String adminRole, String range,
                                             String granularity, String period, String timezone) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 현황 조회 - range: {}, granularity: {}, adminRole: {}", range, granularity, adminRole);

        // 전체 현황 통계 계산
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalFundings = fundingRepository.count();
        
        // 작가 수는 직접 조회 (countByRole 제거됨)
        long artistCount = userRepository.findAll().stream()
                .filter(User::isArtist)
                .count();
        
        // TODO: Order 테이블 연동 후 실제 주문 수, 매출 계산
        long orderCount = 0L; // orderRepository.count();
        long totalRevenue = 0L; // 실제 매출 집계 필요
        
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(totalUsers, "가입자 수", "명", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(orderCount, "주문", "건", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(totalRevenue, "매출", "원", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(totalProducts, "상품수", "개", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(totalFundings, "펀딩수", "개", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(artistCount, "작가수", "명", 0L, 0.0)
        );

        // 차트 데이터 (최소한) - 실제 집계는 추후 구현
        AdminOverviewResponse.Charts charts = new AdminOverviewResponse.Charts(
                new AdminOverviewResponse.ChartMeta(range, granularity, timezone),
                new AdminOverviewResponse.SalesTrend(
                        new AdminOverviewResponse.SalesSeries(
                                List.of(new AdminOverviewResponse.DataPoint(LocalDate.now().toString(), 0L)),
                                List.of(new AdminOverviewResponse.DataPoint(LocalDate.now().toString(), 0L))
                        ),
                        new AdminOverviewResponse.SalesDelta(
                                new AdminOverviewResponse.DeltaInfo(0L, 0.0),
                                new AdminOverviewResponse.DeltaInfo(0L, 0.0)
                        )
                ),
                new AdminOverviewResponse.UserGrowth(
                        new AdminOverviewResponse.UserSeries(
                                List.of(new AdminOverviewResponse.DataPoint(LocalDate.now().toString(), totalUsers)),
                                List.of(new AdminOverviewResponse.DataPoint(LocalDate.now().toString(), artistCount))
                        ),
                        new AdminOverviewResponse.UserDelta(
                                new AdminOverviewResponse.DeltaInfo(0L, 0.0),
                                new AdminOverviewResponse.DeltaInfo(0L, 0.0)
                        )
                ),
                new AdminOverviewResponse.CategoryDistribution(LocalDate.now().toString(), (int) totalProducts,
                        List.of()) // TODO: 카테고리별 집계
        );

        // 승인 대기 알림 - 실제 데이터
        List<ArtistApplication> pendingApplications = artistApplicationRepository
                .findByStatusOrderByCreateDateDesc(ApplicationStatus.PENDING, PageRequest.of(0, 5))
                .getContent();

        List<AdminOverviewResponse.ArtistApproval> artistApprovals = pendingApplications.stream()
                .map(app -> new AdminOverviewResponse.ArtistApproval(
                        app.getUser().getId(),
                        app.getArtistName(),
                        app.getCreateDate()
                ))
                .toList();

        // TODO: 펀딩 승인 대기는 현재 없음 (펀딩 승인 프로세스 추가 시 구현)
        List<AdminOverviewResponse.FundingApproval> fundingApprovals = List.of();

        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                artistApprovals,
                fundingApprovals
        );

        // 유입 경로 정보 (GA4) - 7일 기준
        AdminOverviewResponse.TrafficSources trafficSources = getTrafficSourcesForOverview(authorization, timezone);

        return new AdminOverviewResponse(overview, charts, alerts, trafficSources, LocalDateTime.now(), timezone);
    }

    /**
     * 메인 대시보드용 유입 경로 데이터 조회 (간소화 버전)
     */
    private AdminOverviewResponse.TrafficSources getTrafficSourcesForOverview(String authorization, String timezone) {
        try {
            // 기존 getTrafficSources 메서드 호출 (7일 기준)
            AdminTrafficSourceResponse fullResponse = getTrafficSources(authorization, null, 7, timezone);

            // 메인 대시보드용으로 간소화
            AdminOverviewResponse.Summary summary = new AdminOverviewResponse.Summary(
                    fullResponse.summary().totalSessions(),
                    fullResponse.summary().totalUsers(),
                    fullResponse.summary().avgSessionDuration(),
                    fullResponse.summary().bounceRate()
            );

            // 상위 5개 유입 경로만
            List<AdminOverviewResponse.Source> sources = fullResponse.sources().stream()
                    .limit(5)
                    .map(source -> new AdminOverviewResponse.Source(
                            source.name(),
                            source.sessions(),
                            source.users(),
                            source.share()
                    ))
                    .toList();

            // 차트 데이터
            List<AdminOverviewResponse.ChartData> chartData = fullResponse.chart().data().stream()
                    .limit(5)
                    .map(data -> new AdminOverviewResponse.ChartData(
                            data.name(),
                            data.value(),
                            data.percentage(),
                            data.color()
                    ))
                    .toList();

            AdminOverviewResponse.Chart chart = new AdminOverviewResponse.Chart(chartData);

            return new AdminOverviewResponse.TrafficSources(summary, sources, chart);

        } catch (Exception e) {
            log.error("메인 대시보드 유입 경로 조회 중 오류 발생", e);
            
            // 오류 발생 시 빈 데이터 반환
            return new AdminOverviewResponse.TrafficSources(
                    new AdminOverviewResponse.Summary(0, 0, 0.0, 0.0),
                    List.of(),
                    new AdminOverviewResponse.Chart(List.of())
            );
        }
    }

    @Override
    public AdminProductResponse getProducts(String authorization, String adminRole, int page, int size,
                                            String keyword, String sellingStatus, Long categoryId, Long artistId,
                                            String startDate, String endDate, String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 상품 목록 조회 - page: {}, size: {}, keyword: {}, sellingStatus: {}, categoryId: {}, artistId: {}, adminRole: {}",
                page, size, keyword, sellingStatus, categoryId, artistId, adminRole);

        // 페이징 및 정렬 설정
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = mapProductSortField(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 실제 DB에서 상품 조회 (논리 삭제된 상품 제외)
        Page<Product> productPage = productRepository.findAll(
                buildProductSpecification(keyword, sellingStatus, categoryId, artistId, startDate, endDate),
                pageable
        );

        // Entity → DTO 변환
        List<AdminProductResponse.Product> products = productPage.getContent().stream()
                .map(this::convertToProductDto)
                .toList();

        return new AdminProductResponse(
                products,
                page,
                size,
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    /**
     * Product Entity → DTO 변환
     */
    private AdminProductResponse.Product convertToProductDto(Product product) {
        // UUID를 상품번호로 사용
        String productNumber = product.getProductUuid() != null 
                ? product.getProductUuid().toString() 
                : String.valueOf(product.getId());

        return new AdminProductResponse.Product(
                product.getId(),
                productNumber,
                product.getName(),
                new AdminProductResponse.Artist(
                        product.getUser().getId(),
                        product.getUser().getName() // User에서 작가명 추출
                ),
                product.getSellingStatus().name(),
                new AdminProductResponse.Category(
                        product.getCategory().getId(),
                        product.getCategory().getCategoryName() // categoryName 사용
                ),
                product.getCreateDate().toLocalDate()
        );
    }

    /**
     * 상품 검색 조건 빌더
     */
    private org.springframework.data.jpa.domain.Specification<Product> buildProductSpecification(
            String keyword, String sellingStatus, Long categoryId, Long artistId, String startDate, String endDate) {
        
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 논리 삭제된 상품 제외
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            // 키워드 검색 (상품명, 브랜드명, 작가명, UUID)
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword + "%";
                
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("name"), likePattern)); // 상품명
                keywordPredicates.add(criteriaBuilder.like(root.get("brandName"), likePattern)); // 브랜드명
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern)); // 작가명
                
                // UUID로 검색 (상품번호는 UUID)
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(keyword);
                    keywordPredicates.add(criteriaBuilder.equal(root.get("productUuid"), uuid));
                } catch (IllegalArgumentException e) {
                    // UUID 형식이 아니면 UUID 검색 제외
                }
                
                // 숫자인 경우 ID로도 검색 (레거시 지원)
                try {
                    Long id = Long.parseLong(keyword);
                    keywordPredicates.add(criteriaBuilder.equal(root.get("id"), id));
                } catch (NumberFormatException e) {
                    // 숫자가 아니면 ID 검색 제외
                }
                
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 판매 상태 필터
            if (sellingStatus != null && !sellingStatus.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sellingStatus"), SellingStatus.valueOf(sellingStatus)));
            }

            // 카테고리 필터
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // 작가 필터
            if (artistId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), artistId));
            }

            // 등록일 기간 필터
            if (startDate != null && !startDate.isBlank()) {
                LocalDate start = LocalDate.parse(startDate);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (endDate != null && !endDate.isBlank()) {
                LocalDate end = LocalDate.parse(endDate);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"), end.atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 상품 정렬 필드 매핑
     */
    private String mapProductSortField(String sort) {
        return switch (sort) {
            case "productNumber" -> "id"; // productNumber는 ID 기반
            case "name" -> "name";
            case "artistName" -> "user.name";
            case "sellingStatus" -> "sellingStatus";
            case "registeredAt" -> "createDate";
            default -> "createDate";
        };
    }

    @Override
    public AdminUserResponse getUsers(String authorization, String adminRole, int page, int size,
                                      String keyword, String role, String accountStatus, String grade,
                                      String joinedStartDate, String joinedEndDate, Long artistId,
                                      String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 사용자 목록 조회 - page: {}, size: {}, keyword: {}, role: {}, accountStatus: {}, grade: {}, artistId: {}, adminRole: {}",
                page, size, keyword, role, accountStatus, grade, artistId, adminRole);

        // 페이징 및 정렬 설정
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = mapUserSortField(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 실제 DB에서 사용자 조회
        Page<User> userPage = userRepository.findAll(
                buildUserSpecification(keyword, role, accountStatus, grade, joinedStartDate, joinedEndDate, artistId),
                pageable
        );

        // Entity → DTO 변환
        List<AdminUserResponse.User> users = userPage.getContent().stream()
                .map(this::convertToUserDto)
                .toList();

        return new AdminUserResponse(
                users,
                page,
                size,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    /**
     * User Entity → DTO 변환
     */
    private AdminUserResponse.User convertToUserDto(User user) {
        // 작가명 (작가인 경우만)
        String artistName = user.isArtist() ? user.getName() : null;
        
        // 수수료율 (작가인 경우만, 현재는 기본값 0% - TODO: User 엔티티에 필드 추가 시 수정)
        Integer commissionRate = user.isArtist() ? 0 : null;

        return new AdminUserResponse.User(
                user.getId(),
                user.getEmail() != null ? user.getEmail() : "N/A", // OAuth 사용자는 email이 null일 수 있음
                user.getName(),
                artistName,
                commissionRate,
                user.getGrade().name(),
                user.getStatus().name(),
                user.getCreateDate().toLocalDate()
        );
    }

    /**
     * 사용자 검색 조건 빌더
     */
    private org.springframework.data.jpa.domain.Specification<User> buildUserSpecification(
            String keyword, String role, String accountStatus, String grade,
            String joinedStartDate, String joinedEndDate, Long artistId) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (회원ID=email, 작가명만)
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword + "%";
                
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("email"), likePattern)); // 회원ID (email)
                
                // 작가인 경우 작가명으로 검색
                jakarta.persistence.criteria.Predicate isArtist = criteriaBuilder.equal(root.get("role"), Role.ARTIST);
                jakarta.persistence.criteria.Predicate artistNameMatch = criteriaBuilder.like(root.get("name"), likePattern);
                keywordPredicates.add(criteriaBuilder.and(isArtist, artistNameMatch));
                
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 역할 필터
            if (role != null && !role.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.valueOf(role)));
            }

            // 계정 상태 필터
            if (accountStatus != null && !accountStatus.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.valueOf(accountStatus)));
            }

            // 등급 필터
            if (grade != null && !grade.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("grade"), com.back.domain.user.entity.Grade.valueOf(grade)));
            }

            // 작가 ID 필터
            if (artistId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), artistId));
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.ARTIST));
            }

            // 가입일 기간 필터
            if (joinedStartDate != null && !joinedStartDate.isBlank()) {
                LocalDate start = LocalDate.parse(joinedStartDate);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (joinedEndDate != null && !joinedEndDate.isBlank()) {
                LocalDate end = LocalDate.parse(joinedEndDate);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"), end.atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 사용자 정렬 필드 매핑
     */
    private String mapUserSortField(String sort) {
        return switch (sort) {
            case "memberId" -> "email";
            case "nickname" -> "name";
            case "artistName" -> "name";
            case "commissionRate" -> "id"; // TODO: User 엔티티에 commissionRate 필드 추가 시 수정
            case "grade" -> "grade";
            case "accountStatus" -> "status";
            case "joinedAt" -> "createDate";
            default -> "createDate";
        };
    }

    @Override
    public AdminSettlementResponse getSettlements(String authorization, String adminRole, Integer year,
                                                  Integer month, String granularity, String timezone) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증
        // TODO: Order 테이블에서 실제 매출/정산 데이터 조회 및 집계

        // 연도 기본값 설정 (현재 연도)
        if (year == null) {
            year = Year.now().getValue();
        }

        log.info("관리자 매출/정산 조회 - year: {}, month: {}, granularity: {}, timezone: {}, adminRole: {}",
                year, month, granularity, timezone, adminRole);

        // 조회 범위
        AdminSettlementResponse.Scope scope = new AdminSettlementResponse.Scope(year, month);

        // 요약 정보 (더미 데이터 - Order 테이블 연동 필요)
        AdminSettlementResponse.Summary summary = new AdminSettlementResponse.Summary(
                0L,  // 총 매출액
                0L,  // 총 작가 정산금
                0L   // 총 순수익
        );

        // 차트 데이터 생성 (더미 데이터)
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
     * 정산 데이터 포인트 생성 헬퍼 메서드 (더미 데이터)
     * TODO: Order 테이블에서 실제 집계로 교체 필요
     */
    private void addDataPoint(int year, int monthOrMonth, int dayOrOne,
                              List<AdminSettlementResponse.DataPoint> grossSalesData,
                              List<AdminSettlementResponse.DataPoint> artistPayoutData,
                              List<AdminSettlementResponse.DataPoint> netIncomeData,
                              List<AdminSettlementResponse.TableRow> tableData) {
        String bucketStart = String.format("%d-%02d-%02d", year, monthOrMonth, dayOrOne);
        
        // 더미 데이터 - 실제로는 Order 테이블에서 집계
        long grossSales = 0L;
        long artistPayout = 0L;
        long netIncome = 0L;

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

        log.info("관리자 펀딩 목록 조회 - page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, artistId: {}, adminRole: {}",
                page, size, keyword, status, categoryId, artistId, adminRole);

        // 페이징 및 정렬 설정
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = mapFundingSortField(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 실제 DB에서 펀딩 조회
        Page<Funding> fundingPage = fundingRepository.findAll(
                buildFundingSpecification(keyword, status, categoryId, artistId, minAchievement, maxAchievement,
                        registeredFrom, registeredTo, dueFrom, dueTo),
                pageable
        );

        // Entity → DTO 변환
        List<AdminFundingResponse.Funding> fundings = fundingPage.getContent().stream()
                .map(this::convertToFundingDto)
                .toList();

        return new AdminFundingResponse(
                fundings,
                page,
                size,
                (int) fundingPage.getTotalElements(),
                fundingPage.getTotalPages(),
                fundingPage.hasNext(),
                fundingPage.hasPrevious()
        );
    }

    /**
     * Funding Entity → DTO 변환
     */
    private AdminFundingResponse.Funding convertToFundingDto(Funding funding) {
        // 달성률 계산
        int achievementRate = funding.getTargetAmount() > 0
                ? (int) ((funding.getCollectedAmount() * 100) / funding.getTargetAmount())
                : 0;

        // 남은 일수 계산
        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                funding.getEndDate()
        );

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new AdminFundingResponse.Funding(
                funding.getId(),
                funding.getTitle(),
                new AdminFundingResponse.Artist(
                        funding.getUser().getId(),
                        funding.getUser().getEmail() != null ? funding.getUser().getEmail() : "N/A",
                        funding.getUser().getName()
                ),
                // TODO: Funding에 Category 연동 후 수정 (현재 주석 처리됨)
                new AdminFundingResponse.Category(1L, "미분류"),
                funding.getStatus().name(),
                funding.getTargetAmount(),
                funding.getCollectedAmount(),
                achievementRate,
                funding.getParticipantCount(),
                funding.getEndDate().format(dateFormatter),
                funding.getCreateDate().format(dateFormatter),
                (int) Math.max(0, remainingDays),
                funding.getImageUrl(),
                new AdminFundingResponse.Permissions(true, true),
                new AdminFundingResponse.Flags(
                        achievementRate >= 100,
                        remainingDays <= 7 && remainingDays > 0
                )
        );
    }

    /**
     * 펀딩 검색 조건 빌더
     */
    private org.springframework.data.jpa.domain.Specification<Funding> buildFundingSpecification(
            String keyword, String status, Long categoryId, Long artistId,
            Integer minAchievement, Integer maxAchievement,
            String registeredFrom, String registeredTo, String dueFrom, String dueTo) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (제목, 작가명, 작가ID)
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword + "%";
                
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("title"), likePattern)); // 제목
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern)); // 작가명
                
                // 숫자인 경우 작가ID로도 검색
                try {
                    Long id = Long.parseLong(keyword);
                    keywordPredicates.add(criteriaBuilder.equal(root.get("user").get("id"), id));
                } catch (NumberFormatException e) {
                    // 숫자가 아니면 ID 검색 제외
                }
                
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 펀딩 상태 필터
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), FundingStatus.valueOf(status)));
            }

            // 작가 필터
            if (artistId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), artistId));
            }

            // 달성률 필터 - 애플리케이션 레벨에서 처리하는 것이 더 안전
            // JPA Criteria에서 복잡한 계산식은 타입 문제가 발생할 수 있음
            // TODO: 필요시 네이티브 쿼리나 JPQL로 대체

            // 등록일 기간 필터
            if (registeredFrom != null && !registeredFrom.isBlank()) {
                LocalDate start = LocalDate.parse(registeredFrom);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (registeredTo != null && !registeredTo.isBlank()) {
                LocalDate end = LocalDate.parse(registeredTo);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"), end.atTime(23, 59, 59)));
            }

            // 마감일 기간 필터
            if (dueFrom != null && !dueFrom.isBlank()) {
                LocalDate start = LocalDate.parse(dueFrom);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), start.atStartOfDay()));
            }
            if (dueTo != null && !dueTo.isBlank()) {
                LocalDate end = LocalDate.parse(dueTo);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), end.atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 펀딩 정렬 필드 매핑
     */
    private String mapFundingSortField(String sort) {
        return switch (sort) {
            case "title" -> "title";
            case "artistId" -> "user.id";
            case "artistName" -> "user.name";
            case "achievementRate" -> "collectedAmount"; // 달성률은 수집금액 기준 정렬
            case "status" -> "status";
            case "remainingDays", "endDate" -> "endDate";
            case "currentAmount" -> "collectedAmount";
            case "supporterCount" -> "participantCount";
            case "registeredAt" -> "createDate";
            default -> "endDate";
        };
    }

    @Override
    public AdminArtistApplicationResponse getArtistApplications(String authorization, String adminRole, int page, int size,
                                                                String keyword, String status,
                                                                String submittedFrom, String submittedTo,
                                                                String sort, String order) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 입점 신청 목록 조회 - page: {}, size: {}, keyword: {}, status: {}, adminRole: {}",
                page, size, keyword, status, adminRole);

        // 페이징 및 정렬 설정
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = "createDate"; // 기본 정렬은 신청일
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 실제 DB에서 입점 신청 조회
        Page<ArtistApplication> applicationPage;
        
        if (status != null && !status.isBlank()) {
            ApplicationStatus appStatus = ApplicationStatus.valueOf(status);
            if (keyword != null && !keyword.isBlank()) {
                applicationPage = artistApplicationRepository.findByArtistNameContainingOrderByCreateDateDesc(keyword, pageable);
            } else {
                applicationPage = artistApplicationRepository.findByStatusOrderByCreateDateDesc(appStatus, pageable);
            }
        } else {
            if (keyword != null && !keyword.isBlank()) {
                applicationPage = artistApplicationRepository.findByArtistNameContainingOrderByCreateDateDesc(keyword, pageable);
            } else {
                applicationPage = artistApplicationRepository.findAllByOrderByCreateDateDesc(pageable);
            }
        }

        // 요약 정보 계산
        long totalApplications = artistApplicationRepository.count();
        long pending = artistApplicationRepository.countByStatus(ApplicationStatus.PENDING);
        long approved = artistApplicationRepository.countByStatus(ApplicationStatus.APPROVED);
        long rejected = artistApplicationRepository.countByStatus(ApplicationStatus.REJECTED);

        AdminArtistApplicationResponse.Summary summary = new AdminArtistApplicationResponse.Summary(
                (int) totalApplications,
                (int) pending,
                (int) approved,
                (int) rejected
        );

        // Entity → DTO 변환
        List<AdminArtistApplicationResponse.Application> applications = applicationPage.getContent().stream()
                .map(this::convertToApplicationDto)
                .toList();

        return new AdminArtistApplicationResponse(
                summary,
                applications,
                page,
                size,
                (int) applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                applicationPage.hasNext(),
                applicationPage.hasPrevious()
        );
    }

    /**
     * ArtistApplication Entity → DTO 변환
     */
    private AdminArtistApplicationResponse.Application convertToApplicationDto(ArtistApplication application) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new AdminArtistApplicationResponse.Application(
                application.getId(),
                new AdminArtistApplicationResponse.Artist(
                        application.getUser().getEmail() != null ? application.getUser().getEmail() : "N/A",
                        application.getArtistName()
                ),
                application.getStatus().name(),
                application.getCreateDate().format(dateFormatter),
                new AdminArtistApplicationResponse.Permissions(
                        application.isPending(), // 대기중일 때만 승인 가능
                        application.isPending()  // 대기중일 때만 거절 가능
                )
        );
    }

    @Override
    public AdminArtistApplicationDetailResponse getArtistApplicationDetail(String authorization, String adminRole, Long applicationId) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 입점 신청 상세 조회 - applicationId: {}, adminRole: {}", applicationId, adminRole);

        // 실제 DB에서 입점 신청 상세 조회
        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("입점 신청을 찾을 수 없습니다. ID: " + applicationId));

        // 작가 정보
        AdminArtistApplicationDetailResponse.Artist artist = new AdminArtistApplicationDetailResponse.Artist(
                application.getUser().getId(),
                application.getUser().getEmail() != null ? application.getUser().getEmail() : "N/A",
                application.getArtistName(),
                application.getUser().getProfileImageUrl()
        );

        // 연락처 정보
        AdminArtistApplicationDetailResponse.Contact contact = new AdminArtistApplicationDetailResponse.Contact(
                application.getEmail(),
                application.getPhone()
        );

        // 사업자 정보
        String fullAddress = application.getBusinessAddress();
        if (application.getBusinessAddressDetail() != null) {
            fullAddress += " " + application.getBusinessAddressDetail();
        }
        
        AdminArtistApplicationDetailResponse.Business business = new AdminArtistApplicationDetailResponse.Business(
                application.getBusinessNumber(),
                application.getTelecomSalesNumber(),
                fullAddress
        );

        // 프로필 정보
        List<String> mainCategories = application.getMainProducts() != null
                ? List.of(application.getMainProducts().split(","))
                : List.of();

        List<AdminArtistApplicationDetailResponse.SnsInfo> snsList = new ArrayList<>();
        if (application.getSnsAccount() != null && !application.getSnsAccount().isBlank()) {
            snsList.add(new AdminArtistApplicationDetailResponse.SnsInfo("SNS", application.getSnsAccount()));
        }

        // TODO: 포트폴리오 파일은 ArtistDocument 연동 후 구현
        List<AdminArtistApplicationDetailResponse.PortfolioFile> portfolioFiles = List.of();

        AdminArtistApplicationDetailResponse.Profile profile = new AdminArtistApplicationDetailResponse.Profile(
                mainCategories,
                snsList,
                portfolioFiles
        );

        // 검토 정보
        AdminArtistApplicationDetailResponse.Review review = new AdminArtistApplicationDetailResponse.Review(
                null, // 신청자 메모는 현재 없음
                new AdminArtistApplicationDetailResponse.Verifications(
                        application.getBusinessNumber() != null, // 사업자 정보 있으면 검증됨으로 간주
                        application.getPhone() != null // 연락처 있으면 검증됨으로 간주
                )
        );

        // 결정 정보
        AdminArtistApplicationDetailResponse.Decision decision = new AdminArtistApplicationDetailResponse.Decision(
                application.getStatus().name(),
                application.getRejectionReason(),
                application.getReviewedAt(),
                application.getReviewedByName()
        );

        // 권한 정보
        AdminArtistApplicationDetailResponse.Permissions permissions = new AdminArtistApplicationDetailResponse.Permissions(
                application.isPending(), // 대기중일 때만 승인 가능
                application.isPending()  // 대기중일 때만 거절 가능
        );

        return new AdminArtistApplicationDetailResponse(
                application.getId(),
                application.getStatus().name(),
                application.getCreateDate(),
                artist,
                contact,
                business,
                profile,
                review,
                decision,
                permissions
        );
    }

    @Override
    public AdminTrafficSourceResponse getTrafficSources(String authorization, String adminRole, int days, String timezone) {
        // TODO: JWT 토큰에서 관리자 정보 추출 및 권한 검증

        log.info("관리자 유입 경로 조회 - days: {}, timezone: {}, adminRole: {}", days, timezone, adminRole);

        try {
            // GA4 API 요청 생성
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(propertyId)
                    // 측정 기준: 세션 소스 (Instagram, YouTube, Google 등)
                    .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                    // 지표: 세션 수, 사용자 수, 이탈률, 평균 세션 시간, 전환 수
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("totalUsers"))
                    .addMetrics(Metric.newBuilder().setName("bounceRate"))
                    .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                    .addMetrics(Metric.newBuilder().setName("conversions"))
                    .addMetrics(Metric.newBuilder().setName("newUsers"))
                    // 조회 기간 설정
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(days + "daysAgo")
                            .setEndDate("today"))
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
            double totalBounceRate = 0;
            double totalAvgDuration = 0;
            long totalConversions = 0;

            List<AdminTrafficSourceResponse.Source> sources = new ArrayList<>();
            List<AdminTrafficSourceResponse.ChartData> chartData = new ArrayList<>();

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
                double bounceRate = Double.parseDouble(row.getMetricValues(2).getValue());
                double avgDuration = Double.parseDouble(row.getMetricValues(3).getValue());
                long conversions = Long.parseLong(row.getMetricValues(4).getValue());
                long newUsers = Long.parseLong(row.getMetricValues(5).getValue());

                totalSessions += sessions;
                totalUsers += users;
                totalBounceRate += bounceRate;
                totalAvgDuration += avgDuration;
                totalConversions += conversions;

                // 색상 선택 (소문자로 매칭)
                String color = colorMap.getOrDefault(sourceName.toLowerCase(), "#999999");

                sources.add(new AdminTrafficSourceResponse.Source(
                        sourceName,
                        sessions,
                        users,
                        0.0, // 점유율은 나중에 계산
                        users > 0 ? (double) newUsers / users * 100 : 0.0,
                        bounceRate,
                        avgDuration,
                        conversions,
                        sessions > 0 ? (double) conversions / sessions * 100 : 0.0
                ));

                chartData.add(new AdminTrafficSourceResponse.ChartData(
                        sourceName,
                        sessions,
                        0.0, // 퍼센트는 나중에 계산
                        color
                ));
            }

            // 점유율 계산 (총 세션 대비)
            final long finalTotalSessions = totalSessions;
            sources = sources.stream()
                    .map(source -> new AdminTrafficSourceResponse.Source(
                            source.name(),
                            source.sessions(),
                            source.users(),
                            finalTotalSessions > 0 ? (double) source.sessions() / finalTotalSessions * 100 : 0.0,
                            source.newUserRate(),
                            source.bounceRate(),
                            source.avgSessionDuration(),
                            source.conversions(),
                            source.conversionRate()
                    ))
                    .toList();

            chartData = chartData.stream()
                    .map(data -> new AdminTrafficSourceResponse.ChartData(
                            data.name(),
                            data.value(),
                            finalTotalSessions > 0 ? (double) data.value() / finalTotalSessions * 100 : 0.0,
                            data.color()
                    ))
                    .toList();

            // 요약 정보
            int sourceCount = sources.size();
            AdminTrafficSourceResponse.Summary summary = new AdminTrafficSourceResponse.Summary(
                    totalSessions,
                    totalUsers,
                    sourceCount > 0 ? totalAvgDuration / sourceCount : 0.0,
                    sourceCount > 0 ? totalBounceRate / sourceCount : 0.0
            );

            // 차트 데이터
            AdminTrafficSourceResponse.Chart chart = new AdminTrafficSourceResponse.Chart(
                    chartData,
                    5.0 // 5% 미만은 "기타"로 그룹화
            );

            // 조회 기간
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            AdminTrafficSourceResponse.Period period = new AdminTrafficSourceResponse.Period(
                    startDate.toString(),
                    endDate.toString(),
                    days
            );

            log.info("GA4 유입 경로 조회 완료 - 총 {}개 소스, 총 세션 {}", sources.size(), totalSessions);

            return new AdminTrafficSourceResponse(
                    summary,
                    sources,
                    chart,
                    period,
                    LocalDateTime.now(),
                    timezone
            );

        } catch (Exception e) {
            log.error("GA4 유입 경로 조회 중 오류 발생", e);
            
            // 오류 발생 시 빈 데이터 반환
            return new AdminTrafficSourceResponse(
                    new AdminTrafficSourceResponse.Summary(0, 0, 0.0, 0.0),
                    List.of(),
                    new AdminTrafficSourceResponse.Chart(List.of(), 5.0),
                    new AdminTrafficSourceResponse.Period(
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
