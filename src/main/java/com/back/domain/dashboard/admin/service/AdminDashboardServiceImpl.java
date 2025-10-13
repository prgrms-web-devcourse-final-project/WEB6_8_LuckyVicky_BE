package com.back.domain.dashboard.admin.service;

import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.dashboard.admin.dto.MonthlySettlementDto;
import com.back.domain.dashboard.admin.dto.request.*;
import com.back.domain.dashboard.admin.dto.response.*;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.SellingStatus;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.Status;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final OrderRepository orderRepository;
    private final com.back.domain.product.category.repository.CategoryRepository categoryRepository;

    /**
     * SecurityContext에서 인증된 관리자 정보를 추출하고 권한을 검증하는 헬퍼 메서드
     */
    private CustomUserDetails validateAdminAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceException("401", "인증이 필요합니다.");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new ServiceException("401", "유효하지 않은 인증 정보입니다.");
        }

        Role role = userDetails.getCurrentRole();
        if (role != Role.ADMIN && role != Role.ROOT) {
            throw new ServiceException("403", "관리자 권한이 필요합니다.");
        }

        return userDetails;
    }

    @Override
    public AdminOverviewResponse getOverview(AdminOverviewRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        // 1. 전체 현황 통계 계산
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalFundings = fundingRepository.count();
        long artistCount = userRepository.findAll().stream().filter(User::isArtist).count();

        // 2. 오늘 날짜 기준 통계
        LocalDate today = LocalDate.now();
        BigDecimal todayRevenueBd = orderRepository.findTodayTotalRevenue(today);
        long todayRevenue = todayRevenueBd != null ? todayRevenueBd.longValue() : 0L;
        long todayOrderCount = orderRepository.countTodayOrders(today);

        // 3. Overview 객체 생성
        AdminOverviewResponse.Overview overview = new AdminOverviewResponse.Overview(
                new AdminOverviewResponse.StatInfo(totalUsers, "가입자 수", "명", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(todayOrderCount, "오늘의 주문", "건", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(todayRevenue, "오늘의 매출", "원", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(totalProducts, "상품수", "개", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(totalFundings, "펀딩수", "개", 0L, 0.0),
                new AdminOverviewResponse.StatInfo(artistCount, "작가수", "명", 0L, 0.0)
        );

        // 4. 차트 데이터 생성
        AdminOverviewResponse.Charts charts = createChartsData(request);

        // 5. 승인 대기 알림
        List<ArtistApplication> pendingApplications = artistApplicationRepository
                .findByStatusOrderByCreateDateDesc(ApplicationStatus.PENDING, PageRequest.of(0, 2))
                .getContent();

        List<AdminOverviewResponse.ArtistApproval> artistApprovals = pendingApplications.stream()
                .map(app -> new AdminOverviewResponse.ArtistApproval(
                        app.getUser().getId(),
                        app.getArtistName(),
                        app.getCreateDate()
                ))
                .toList();

        AdminOverviewResponse.Alerts alerts = new AdminOverviewResponse.Alerts(
                artistApprovals,
                List.of()  // 펀딩 승인은 나중에 구현
        );

        return new AdminOverviewResponse(overview, charts, alerts, LocalDateTime.now(), request.timezone());
    }

    /**
     * 차트 데이터 생성 (매출 + 주문 + 사용자 트렌드)
     */
    private AdminOverviewResponse.Charts createChartsData(AdminOverviewRequest request) {
        // 1. 기간 계산
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime compareStartDate;
        LocalDateTime compareEndDate;

        String range = request.range() != null ? request.range() : "1M";

        switch (range) {
            case "1M" -> {
                startDate = endDate.minusMonths(1);
                compareStartDate = startDate.minusMonths(1);
                compareEndDate = startDate;
            }
            case "3M" -> {
                startDate = endDate.minusMonths(3);
                compareStartDate = startDate.minusMonths(3);
                compareEndDate = startDate;
            }
            case "6M" -> {
                startDate = endDate.minusMonths(6);
                compareStartDate = startDate.minusMonths(6);
                compareEndDate = startDate;
            }
            case "1Y" -> {
                startDate = endDate.minusYears(1);
                compareStartDate = startDate.minusYears(1);
                compareEndDate = startDate;
            }
            case "ALL" -> {
                startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
                compareStartDate = startDate;
                compareEndDate = startDate.plusYears(1);
            }
            default -> {
                startDate = endDate.minusMonths(1);
                compareStartDate = startDate.minusMonths(1);
                compareEndDate = startDate;
            }
        }

        // 2. 매출/주문 트렌드 데이터 조회
        List<com.back.domain.dashboard.artist.dto.DailyTrendDto> salesTrends =
                orderRepository.findDailyTrendsForAdmin(startDate, endDate);
        List<com.back.domain.dashboard.artist.dto.DailyTrendDto> compareSalesTrends =
                orderRepository.findDailyTrendsForAdmin(compareStartDate, compareEndDate);

        // 3. 사용자 증가 트렌드 데이터 조회
        List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> userGrowthTrends =
                userRepository.findDailyUserGrowth(startDate, endDate);
        List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> compareUserGrowthTrends =
                userRepository.findDailyUserGrowth(compareStartDate, compareEndDate);
        List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> artistGrowthTrends =
                userRepository.findDailyArtistGrowth(startDate, endDate);
        List<com.back.domain.dashboard.admin.dto.DailyUserGrowthDto> compareArtistGrowthTrends =
                userRepository.findDailyArtistGrowth(compareStartDate, compareEndDate);

        // 4. 매출 트렌드 차트 데이터 생성
        List<AdminOverviewResponse.DataPoint> salesData = salesTrends.stream()
                .map(d -> new AdminOverviewResponse.DataPoint(d.date().toString(), d.salesAmount()))
                .toList();
        List<AdminOverviewResponse.DataPoint> orderData = salesTrends.stream()
                .map(d -> new AdminOverviewResponse.DataPoint(d.date().toString(), d.orderCount()))
                .toList();

        long totalSales = salesTrends.stream().mapToLong(com.back.domain.dashboard.artist.dto.DailyTrendDto::salesAmount).sum();
        long totalOrders = salesTrends.stream().mapToLong(com.back.domain.dashboard.artist.dto.DailyTrendDto::orderCount).sum();
        long compareTotalSales = compareSalesTrends.stream().mapToLong(com.back.domain.dashboard.artist.dto.DailyTrendDto::salesAmount).sum();
        long compareTotalOrders = compareSalesTrends.stream().mapToLong(com.back.domain.dashboard.artist.dto.DailyTrendDto::orderCount).sum();

        AdminOverviewResponse.DeltaInfo salesDelta = calculateDelta(totalSales, compareTotalSales);
        AdminOverviewResponse.DeltaInfo orderDelta = calculateDelta(totalOrders, compareTotalOrders);

        AdminOverviewResponse.SalesTrend salesTrend = new AdminOverviewResponse.SalesTrend(
                new AdminOverviewResponse.SalesSeries(salesData, orderData),
                new AdminOverviewResponse.SalesDelta(salesDelta, orderDelta)
        );

        // 5. 사용자 증가 차트 데이터 생성
        List<AdminOverviewResponse.DataPoint> userData = userGrowthTrends.stream()
                .map(d -> new AdminOverviewResponse.DataPoint(d.date().toString(), d.userCount()))
                .toList();
        List<AdminOverviewResponse.DataPoint> artistData = artistGrowthTrends.stream()
                .map(d -> new AdminOverviewResponse.DataPoint(d.date().toString(), d.userCount()))
                .toList();

        long totalNewUsers = userGrowthTrends.stream().mapToLong(com.back.domain.dashboard.admin.dto.DailyUserGrowthDto::userCount).sum();
        long totalNewArtists = artistGrowthTrends.stream().mapToLong(com.back.domain.dashboard.admin.dto.DailyUserGrowthDto::userCount).sum();
        long compareTotalNewUsers = compareUserGrowthTrends.stream().mapToLong(com.back.domain.dashboard.admin.dto.DailyUserGrowthDto::userCount).sum();
        long compareTotalNewArtists = compareArtistGrowthTrends.stream().mapToLong(com.back.domain.dashboard.admin.dto.DailyUserGrowthDto::userCount).sum();

        AdminOverviewResponse.DeltaInfo userDelta = calculateDelta(totalNewUsers, compareTotalNewUsers);
        AdminOverviewResponse.DeltaInfo artistDelta = calculateDelta(totalNewArtists, compareTotalNewArtists);

        AdminOverviewResponse.UserGrowth userGrowth = new AdminOverviewResponse.UserGrowth(
                new AdminOverviewResponse.UserSeries(userData, artistData),
                new AdminOverviewResponse.UserDelta(userDelta, artistDelta)
        );

        // 6. 카테고리 분포
        long totalProducts = productRepository.count();
        AdminOverviewResponse.CategoryDistribution categoryDist = calculateCategoryDistribution((int) totalProducts);

        // 7. 메타 정보
        AdminOverviewResponse.ChartMeta meta = new AdminOverviewResponse.ChartMeta(
                range,
                request.granularity(),
                request.timezone()
        );

        return new AdminOverviewResponse.Charts(meta, salesTrend, userGrowth, categoryDist);
    }

    /**
     * 변화량 계산 (delta, rate)
     */
    private AdminOverviewResponse.DeltaInfo calculateDelta(long current, long previous) {
        long delta = current - previous;
        double rate = 0.0;

        if (previous > 0) {
            rate = ((double) delta / previous) * 100;
        } else if (current > 0) {
            rate = 100.0;
        }

        return new AdminOverviewResponse.DeltaInfo(delta, rate);
    }

    @Override
    public AdminProductResponse getProducts(AdminProductSearchRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapProductSortField);

        Page<Product> productPage = productRepository.findAll(
                buildProductSpecification(request),
                pageable
        );

        List<AdminProductResponse.Product> products = productPage.getContent().stream()
                .map(this::convertToProductDto)
                .toList();

        return new AdminProductResponse(
                products,
                request.page(),
                request.size(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    /**
     * 공통 Pageable 생성 헬퍼 메서드
     */
    private Pageable buildPageable(int page, int size, String sort, String order,
                                   java.util.function.Function<String, String> sortFieldMapper) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortFieldMapper.apply(sort);
        return PageRequest.of(page, size, Sort.by(direction, sortField));
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
            AdminProductSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("name"), likePattern));
                keywordPredicates.add(criteriaBuilder.like(root.get("brandName"), likePattern));
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern));

                try {
                    java.util.UUID uuid = java.util.UUID.fromString(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("productUuid"), uuid));
                } catch (IllegalArgumentException ignored) {}

                try {
                    Long id = Long.parseLong(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("id"), id));
                } catch (NumberFormatException ignored) {}

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            if (request.sellingStatus() != null && !request.sellingStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sellingStatus"), SellingStatus.valueOf(request.sellingStatus())));
            }

            if (request.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.categoryId()));
            }

            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.artistId()));
            }

            if (request.startDate() != null && !request.startDate().isBlank()) {
                LocalDate start = LocalDate.parse(request.startDate());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (request.endDate() != null && !request.endDate().isBlank()) {
                LocalDate end = LocalDate.parse(request.endDate());
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
    public AdminUserResponse getUsers(AdminUserSearchRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapUserSortField);

        Page<User> userPage = userRepository.findAll(
                buildUserSpecification(request),
                pageable
        );

        List<AdminUserResponse.User> users = userPage.getContent().stream()
                .map(this::convertToUserDto)
                .toList();

        return new AdminUserResponse(
                users,
                request.page(),
                request.size(),
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
        String artistName = user.isArtist() ? user.getName() : null;
        Integer commissionRate = user.isArtist() ? 0 : null;

        return new AdminUserResponse.User(
                user.getId(),
                user.getEmail() != null ? user.getEmail() : "N/A",
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
            AdminUserSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("email"), likePattern));

                jakarta.persistence.criteria.Predicate isArtist = criteriaBuilder.equal(root.get("role"), Role.ARTIST);
                jakarta.persistence.criteria.Predicate artistNameMatch = criteriaBuilder.like(root.get("name"), likePattern);
                keywordPredicates.add(criteriaBuilder.and(isArtist, artistNameMatch));

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            if (request.role() != null && !request.role().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.valueOf(request.role())));
            }

            if (request.accountStatus() != null && !request.accountStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.valueOf(request.accountStatus())));
            }

            if (request.grade() != null && !request.grade().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("grade"), com.back.domain.user.entity.Grade.valueOf(request.grade())));
            }

            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.artistId()));
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.ARTIST));
            }

            if (request.joinedStartDate() != null && !request.joinedStartDate().isBlank()) {
                LocalDate start = LocalDate.parse(request.joinedStartDate());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (request.joinedEndDate() != null && !request.joinedEndDate().isBlank()) {
                LocalDate end = LocalDate.parse(request.joinedEndDate());
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
    public AdminSettlementResponse getSettlements(AdminSettlementRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        Integer year = request.year() != null ? request.year() : Year.now().getValue();
        Integer month = request.month();

        AdminSettlementResponse.Scope scope = new AdminSettlementResponse.Scope(year, month);

        // 1. DB에서 실제 매출/정산 데이터 조회
        List<MonthlySettlementDto> settlements;
        if (month != null) {
            settlements = orderRepository.findDailySettlements(year, month);
        } else {
            settlements = orderRepository.findMonthlySettlements(year);
        }

        // 2. 요약 정보 계산
        BigDecimal totalAmountBd = orderRepository.findTotalSettlementAmount(year, month);
        long totalAmount = totalAmountBd != null ? totalAmountBd.longValue() : 0L;
        long artistPayout = (long) (totalAmount * 0.9);
        long netIncome = (long) (totalAmount * 0.1);

        AdminSettlementResponse.Summary summary = new AdminSettlementResponse.Summary(
                totalAmount,
                artistPayout,
                netIncome
        );

        // 3. 차트 데이터 및 테이블 데이터 생성
        List<AdminSettlementResponse.DataPoint> grossSalesData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> artistPayoutData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> netIncomeData = new ArrayList<>();
        List<AdminSettlementResponse.TableRow> tableData = new ArrayList<>();

        Map<LocalDate, MonthlySettlementDto> settlementMap = new HashMap<>();
        for (MonthlySettlementDto dto : settlements) {
            settlementMap.put(dto.date(), dto);
        }

        if (month != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            int daysInMonth = startDate.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = LocalDate.of(year, month, day);
                MonthlySettlementDto settlement = settlementMap.getOrDefault(currentDate,
                        new MonthlySettlementDto(currentDate, 0L));

                addSettlementDataPoint(settlement, grossSalesData, artistPayoutData, netIncomeData, tableData);
            }
        } else {
            for (int m = 1; m <= 12; m++) {
                LocalDate currentDate = LocalDate.of(year, m, 1);
                MonthlySettlementDto settlement = settlementMap.getOrDefault(currentDate,
                        new MonthlySettlementDto(currentDate, 0L));

                addSettlementDataPoint(settlement, grossSalesData, artistPayoutData, netIncomeData, tableData);
            }
        }

        AdminSettlementResponse.Chart chart = new AdminSettlementResponse.Chart(
                new AdminSettlementResponse.Series(grossSalesData, artistPayoutData, netIncomeData)
        );

        return new AdminSettlementResponse(
                scope, request.granularity(), request.timezone(), summary, chart, tableData, LocalDateTime.now()
        );
    }

    /**
     * 정산 데이터 포인트 생성
     */
    private void addSettlementDataPoint(MonthlySettlementDto settlement,
                                        List<AdminSettlementResponse.DataPoint> grossSalesData,
                                        List<AdminSettlementResponse.DataPoint> artistPayoutData,
                                        List<AdminSettlementResponse.DataPoint> netIncomeData,
                                        List<AdminSettlementResponse.TableRow> tableData) {

        String bucketStart = settlement.date().toString();
        long grossSales = settlement.totalAmount();
        long artistPayout = settlement.getArtistPayout();
        long netIncome = settlement.getNetIncome();

        grossSalesData.add(new AdminSettlementResponse.DataPoint(bucketStart, grossSales));
        artistPayoutData.add(new AdminSettlementResponse.DataPoint(bucketStart, artistPayout));
        netIncomeData.add(new AdminSettlementResponse.DataPoint(bucketStart, netIncome));
        tableData.add(new AdminSettlementResponse.TableRow(bucketStart, grossSales, artistPayout, netIncome));
    }

    @Override
    public AdminFundingResponse getFundings(AdminFundingSearchRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapFundingSortField);

        Page<Funding> fundingPage = fundingRepository.findAll(
                buildFundingSpecification(request),
                pageable
        );

        List<AdminFundingResponse.Funding> fundings = fundingPage.getContent().stream()
                .map(this::convertToFundingDto)
                .toList();

        return new AdminFundingResponse(
                fundings,
                request.page(),
                request.size(),
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
        int achievementRate = funding.getTargetAmount() > 0
                ? (int) ((funding.getCollectedAmount() * 100) / funding.getTargetAmount())
                : 0;

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
            AdminFundingSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("title"), likePattern));
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern));

                try {
                    Long id = Long.parseLong(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("user").get("id"), id));
                } catch (NumberFormatException ignored) {}

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            if (request.status() != null && !request.status().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), FundingStatus.valueOf(request.status())));
            }

            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.artistId()));
            }

            if (request.registeredFrom() != null && !request.registeredFrom().isBlank()) {
                LocalDate start = LocalDate.parse(request.registeredFrom());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (request.registeredTo() != null && !request.registeredTo().isBlank()) {
                LocalDate end = LocalDate.parse(request.registeredTo());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"), end.atTime(23, 59, 59)));
            }

            if (request.dueFrom() != null && !request.dueFrom().isBlank()) {
                LocalDate start = LocalDate.parse(request.dueFrom());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), start.atStartOfDay()));
            }
            if (request.dueTo() != null && !request.dueTo().isBlank()) {
                LocalDate end = LocalDate.parse(request.dueTo());
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
    public AdminArtistApplicationResponse getArtistApplications(AdminArtistApplicationSearchRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(),
                sort -> "createDate");

        Page<ArtistApplication> applicationPage;

        if (request.status() != null && !request.status().isBlank()) {
            ApplicationStatus appStatus = ApplicationStatus.valueOf(request.status());
            if (request.keyword() != null && !request.keyword().isBlank()) {
                applicationPage = artistApplicationRepository.findByArtistNameContainingOrderByCreateDateDesc(request.keyword(), pageable);
            } else {
                applicationPage = artistApplicationRepository.findByStatusOrderByCreateDateDesc(appStatus, pageable);
            }
        } else {
            if (request.keyword() != null && !request.keyword().isBlank()) {
                applicationPage = artistApplicationRepository.findByArtistNameContainingOrderByCreateDateDesc(request.keyword(), pageable);
            } else {
                applicationPage = artistApplicationRepository.findAllByOrderByCreateDateDesc(pageable);
            }
        }

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

        List<AdminArtistApplicationResponse.Application> applications = applicationPage.getContent().stream()
                .map(this::convertToApplicationDto)
                .toList();

        return new AdminArtistApplicationResponse(
                summary,
                applications,
                request.page(),
                request.size(),
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

    /**
     * 카테고리별 상품 분포 계산 (실제 DB 연동)
     */
    private AdminOverviewResponse.CategoryDistribution calculateCategoryDistribution(int totalProducts) {
        try {
            // 모든 상위 카테고리 조회
            List<com.back.domain.product.category.entity.Category> topCategories =
                    categoryRepository.findAllByParentIdIsNull();

            if (topCategories.isEmpty()) {
                log.warn("카테고리가 존재하지 않습니다.");
                return new AdminOverviewResponse.CategoryDistribution(
                        LocalDate.now().toString(),
                        totalProducts,
                        List.of()
                );
            }

            // 카테고리별 상품 수 집계
            List<AdminOverviewResponse.CategoryBucket> buckets = new ArrayList<>();

            for (com.back.domain.product.category.entity.Category category : topCategories) {
                // 해당 카테고리의 삭제되지 않은 상품 수 조회
                long count = productRepository.countByCategoryAndIsDeletedFalse(category);
                // 하위 카테고리의 상품도 합산
                for (com.back.domain.product.category.entity.Category subCategory : category.getSubCategories()) {
                    count += productRepository.countByCategoryAndIsDeletedFalse(subCategory);
                }

                if (count > 0) {
                    // 점유율 계산
                    double share = totalProducts > 0 ? (count * 100.0 / totalProducts) : 0.0;

                    buckets.add(new AdminOverviewResponse.CategoryBucket(
                            category.getId(),
                            category.getCategoryName(),
                            (int) count,
                            share
                    ));
                }
            }

            // 점유율 높은 순으로 정렬
            buckets.sort((a, b) -> Double.compare(b.share(), a.share()));

            log.info("카테고리별 분포 계산 완료 - 총 {}개 카테고리, 전체 상품 {}개",
                    buckets.size(), totalProducts);

            return new AdminOverviewResponse.CategoryDistribution(
                    LocalDate.now().toString(),
                    totalProducts,
                    buckets
            );

        } catch (Exception e) {
            log.error("카테고리별 분포 계산 중 오류 발생", e);
            return new AdminOverviewResponse.CategoryDistribution(
                    LocalDate.now().toString(),
                    totalProducts,
                    List.of()
            );
        }
    }

    @Override
    public AdminArtistApplicationDetailResponse getArtistApplicationDetail(Long applicationId) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        ArtistApplication application = artistApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("입점 신청을 찾을 수 없습니다. ID: " + applicationId));

        AdminArtistApplicationDetailResponse.Artist artist = new AdminArtistApplicationDetailResponse.Artist(
                application.getUser().getId(),
                application.getUser().getEmail() != null ? application.getUser().getEmail() : "N/A",
                application.getArtistName(),
                application.getUser().getProfileImageUrl()
        );

        AdminArtistApplicationDetailResponse.Contact contact = new AdminArtistApplicationDetailResponse.Contact(
                application.getEmail(),
                application.getPhone()
        );

        String fullAddress = application.getBusinessAddress();
        if (application.getBusinessAddressDetail() != null) {
            fullAddress += " " + application.getBusinessAddressDetail();
        }

        AdminArtistApplicationDetailResponse.Business business = new AdminArtistApplicationDetailResponse.Business(
                application.getBusinessNumber(),
                application.getTelecomSalesNumber(),
                fullAddress
        );

        List<String> mainCategories = application.getMainProducts() != null
                ? List.of(application.getMainProducts().split(","))
                : List.of();

        List<AdminArtistApplicationDetailResponse.SnsInfo> snsList = new ArrayList<>();
        if (application.getSnsAccount() != null && !application.getSnsAccount().isBlank()) {
            snsList.add(new AdminArtistApplicationDetailResponse.SnsInfo("SNS", application.getSnsAccount()));
        }

        List<AdminArtistApplicationDetailResponse.PortfolioFile> portfolioFiles = List.of();

        AdminArtistApplicationDetailResponse.Profile profile = new AdminArtistApplicationDetailResponse.Profile(
                mainCategories,
                snsList,
                portfolioFiles
        );

        AdminArtistApplicationDetailResponse.Review review = new AdminArtistApplicationDetailResponse.Review(
                null,
                new AdminArtistApplicationDetailResponse.Verifications(
                        application.getBusinessNumber() != null,
                        application.getPhone() != null
                )
        );

        AdminArtistApplicationDetailResponse.Decision decision = new AdminArtistApplicationDetailResponse.Decision(
                application.getStatus().name(),
                application.getRejectionReason(),
                application.getReviewedAt(),
                application.getReviewedByName()
        );

        AdminArtistApplicationDetailResponse.Permissions permissions = new AdminArtistApplicationDetailResponse.Permissions(
                application.isPending(),
                application.isPending()
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
    public AdminFundingApprovalResponse getFundingApprovals(AdminFundingApprovalSearchRequest request) {
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 펀딩 승인 대기 목록 조회 - adminId: {}, page: {}, size: {}, keyword: {}",
                adminUser.getUserId(), request.page(), request.size(), request.keyword());

        // Pageable 생성
        Pageable pageable = buildPageable(
                request.page(),
                request.size(),
                request.sort(),
                request.order(),
                this::mapFundingApprovalSortField
        );

        // PENDING 상태 펀딩 조회
        Page<Funding> fundingPage = fundingRepository.findPendingApprovalFundings(
                request.keyword(),
                request.artistId(),
                request.sort(),
                request.order(),
                pageable
        );

        // Entity → DTO 변환
        List<AdminFundingApprovalResponse.FundingApproval> content = fundingPage.getContent().stream()
                .map(this::convertToFundingApprovalDto)
                .toList();

        log.info("펀딩 승인 대기 목록 조회 완료 - 조회된 펀딩 수: {}, 전체: {}",
                content.size(), fundingPage.getTotalElements());

        return new AdminFundingApprovalResponse(
                content,
                request.page(),
                request.size(),
                fundingPage.getTotalElements(),
                fundingPage.getTotalPages(),
                fundingPage.hasNext(),
                fundingPage.hasPrevious()
        );
    }

    /**
     * Funding Entity → FundingApproval DTO 변환
     */
    private AdminFundingApprovalResponse.FundingApproval convertToFundingApprovalDto(Funding funding) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");

        // 작가 정보
        AdminFundingApprovalResponse.Artist artist = new AdminFundingApprovalResponse.Artist(
                funding.getUser().getId(),
                funding.getUser().getName(),
                funding.getUser().getEmail() != null ? funding.getUser().getEmail() : "N/A"
        );

        return new AdminFundingApprovalResponse.FundingApproval(
                funding.getId(),
                funding.getTitle(),
                artist,
                funding.getTargetAmount(),
                funding.getStartDate().format(dateFormatter),
                funding.getEndDate().format(dateFormatter),
                funding.getCreateDate().format(dateFormatter),
                funding.getImageUrl()
        );
    }

    /**
     * 펀딩 승인 대기 정렬 필드 매핑
     */
    private String mapFundingApprovalSortField(String sort) {
        return switch (sort) {
            case "artistId" -> "user.id";
            case "artistName" -> "user.name";
            case "title" -> "title";
            case "registeredAt" -> "createDate";
            default -> "createDate";
        };
    }
}