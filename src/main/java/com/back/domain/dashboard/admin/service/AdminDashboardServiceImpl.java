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
     *
     * @return CustomUserDetails 인증된 사용자 정보
     * @throws ServiceException 인증되지 않았거나 관리자 권한이 없는 경우
     */
    private CustomUserDetails validateAdminAuthentication() {
        // 1. SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("인증되지 않은 접근 시도");
            throw new ServiceException("401", "인증이 필요합니다.");
        }

        // 2. CustomUserDetails 추출
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            log.error("잘못된 인증 타입: {}", authentication.getPrincipal().getClass());
            throw new ServiceException("401", "유효하지 않은 인증 정보입니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. 관리자 권한 검증 (ADMIN 또는 ROOT만 허용)
        Role role = userDetails.getCurrentRole();
        if (role != Role.ADMIN && role != Role.ROOT) {
            log.error("권한 없는 접근 시도 - userId: {}, role: {}", userDetails.getUserId(), role);
            throw new ServiceException("403", "관리자 권한이 필요합니다.");
        }

        return userDetails;
    }

    @Override
    public AdminOverviewResponse getOverview(AdminOverviewRequest request) {
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 현황 조회 - userId: {}, role: {}, range: {}, granularity: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), request.range(), request.granularity());

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
                new AdminOverviewResponse.ChartMeta(request.range(), request.granularity(), request.timezone()),
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
                calculateCategoryDistribution((int) totalProducts) // 실제 카테고리별 집계
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

        return new AdminOverviewResponse(overview, charts, alerts, LocalDateTime.now(), request.timezone());
    }

    @Override
    public AdminProductResponse getProducts(AdminProductSearchRequest request) {
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 상품 목록 조회 - userId: {}, role: {}, page: {}, size: {}, keyword: {}, sellingStatus: {}, categoryId: {}, artistId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), request.page(), request.size(),
                request.keyword(), request.sellingStatus(), request.categoryId(), request.artistId());

        // 페이징 및 정렬 설정
        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapProductSortField);

        // 실제 DB에서 상품 조회 (논리 삭제된 상품 제외)
        Page<Product> productPage = productRepository.findAll(
                buildProductSpecification(request),
                pageable
        );

        // Entity → DTO 변환
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
     * 상품 검색 조건 빌더 - Request DTO 활용
     */
    private org.springframework.data.jpa.domain.Specification<Product> buildProductSpecification(
            AdminProductSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 논리 삭제된 상품 제외
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            // 키워드 검색 (상품명, 브랜드명, 작가명, UUID)
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("name"), likePattern)); // 상품명
                keywordPredicates.add(criteriaBuilder.like(root.get("brandName"), likePattern)); // 브랜드명
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern)); // 작가명

                // UUID로 검색 (상품번호는 UUID)
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("productUuid"), uuid));
                } catch (IllegalArgumentException e) {
                    // UUID 형식이 아니면 UUID 검색 제외
                }

                // 숫자인 경우 ID로도 검색 (레거시 지원)
                try {
                    Long id = Long.parseLong(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("id"), id));
                } catch (NumberFormatException e) {
                    // 숫자가 아니면 ID 검색 제외
                }

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 판매 상태 필터
            if (request.sellingStatus() != null && !request.sellingStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sellingStatus"), SellingStatus.valueOf(request.sellingStatus())));
            }

            // 카테고리 필터
            if (request.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.categoryId()));
            }

            // 작가 필터
            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.artistId()));
            }

            // 등록일 기간 필터
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
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 사용자 목록 조회 - userId: {}, role: {}, page: {}, size: {}, keyword: {}, userRole: {}, accountStatus: {}, grade: {}, artistId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), request.page(), request.size(),
                request.keyword(), request.role(), request.accountStatus(), request.grade(), request.artistId());

        // 페이징 및 정렬 설정
        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapUserSortField);

        // 실제 DB에서 사용자 조회
        Page<User> userPage = userRepository.findAll(
                buildUserSpecification(request),
                pageable
        );

        // Entity → DTO 변환
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
     * 사용자 검색 조건 빌더 - Request DTO 활용
     */
    private org.springframework.data.jpa.domain.Specification<User> buildUserSpecification(
            AdminUserSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (회원ID=email, 작가명만)
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("email"), likePattern)); // 회원ID (email)

                // 작가인 경우 작가명으로 검색
                jakarta.persistence.criteria.Predicate isArtist = criteriaBuilder.equal(root.get("role"), Role.ARTIST);
                jakarta.persistence.criteria.Predicate artistNameMatch = criteriaBuilder.like(root.get("name"), likePattern);
                keywordPredicates.add(criteriaBuilder.and(isArtist, artistNameMatch));

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 역할 필터
            if (request.role() != null && !request.role().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.valueOf(request.role())));
            }

            // 계정 상태 필터
            if (request.accountStatus() != null && !request.accountStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.valueOf(request.accountStatus())));
            }

            // 등급 필터
            if (request.grade() != null && !request.grade().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("grade"), com.back.domain.user.entity.Grade.valueOf(request.grade())));
            }

            // 작가 ID 필터
            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.artistId()));
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.ARTIST));
            }

            // 가입일 기간 필터
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
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        // 연도 기본값 설정 (현재 연도)
        Integer year = request.year() != null ? request.year() : Year.now().getValue();
        Integer month = request.month();

        log.info("관리자 매출/정산 조회 - userId: {}, role: {}, year: {}, month: {}, granularity: {}, timezone: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), year, month, request.granularity(), request.timezone());

        // 조회 범위
        AdminSettlementResponse.Scope scope = new AdminSettlementResponse.Scope(year, month);

        // 1. DB에서 실제 매출/정산 데이터 조회
        List<MonthlySettlementDto> settlements;
        if (month != null) {
            // 일별 집계
            settlements = orderRepository.findDailySettlements(year, month);
            log.info("일별 정산 데이터 조회 완료 - {}년 {}월, {} 건", year, month, settlements.size());
        } else {
            // 월별 집계
            settlements = orderRepository.findMonthlySettlements(year);
            log.info("월별 정산 데이터 조회 완료 - {}년, {} 건", year, settlements.size());
        }

        // 2. 요약 정보 계산
        BigDecimal totalAmountBd = orderRepository.findTotalSettlementAmount(year, month);
        long totalAmount = totalAmountBd != null ? totalAmountBd.longValue() : 0L;
        long artistPayout = (long) (totalAmount * 0.9);
        long netIncome = (long) (totalAmount * 0.1);

        AdminSettlementResponse.Summary summary = new AdminSettlementResponse.Summary(
                totalAmount,   // 총 매출액
                artistPayout,  // 총 작가 정산금 (90%)
                netIncome      // 총 순수익 (10%)
        );

        log.info("정산 요약 - 총 매출: {}, 작가 정산금: {}, 순수익: {}", totalAmount, artistPayout, netIncome);

        // 3. 차트 데이터 및 테이블 데이터 생성
        List<AdminSettlementResponse.DataPoint> grossSalesData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> artistPayoutData = new ArrayList<>();
        List<AdminSettlementResponse.DataPoint> netIncomeData = new ArrayList<>();
        List<AdminSettlementResponse.TableRow> tableData = new ArrayList<>();

        // DB 조회 결과를 Map으로 변환 (날짜 -> 데이터)
        Map<LocalDate, MonthlySettlementDto> settlementMap = new HashMap<>();
        for (MonthlySettlementDto dto : settlements) {
            settlementMap.put(dto.date(), dto);
        }

        if (month != null) {
            // 일별 집계 - 해당 월의 모든 일자 생성
            LocalDate startDate = LocalDate.of(year, month, 1);
            int daysInMonth = startDate.lengthOfMonth();
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = LocalDate.of(year, month, day);
                MonthlySettlementDto settlement = settlementMap.getOrDefault(currentDate, 
                        new MonthlySettlementDto(currentDate, 0L));
                
                addSettlementDataPoint(settlement, grossSalesData, artistPayoutData, netIncomeData, tableData);
            }
        } else {
            // 월별 집계 - 해당 연도의 모든 월 생성
            for (int m = 1; m <= 12; m++) {
                LocalDate currentDate = LocalDate.of(year, m, 1);
                MonthlySettlementDto settlement = settlementMap.getOrDefault(currentDate,
                        new MonthlySettlementDto(currentDate, 0L));
                
                addSettlementDataPoint(settlement, grossSalesData, artistPayoutData, netIncomeData, tableData);
            }
        }

        // 4. 차트 데이터 통합
        AdminSettlementResponse.Chart chart = new AdminSettlementResponse.Chart(
                new AdminSettlementResponse.Series(grossSalesData, artistPayoutData, netIncomeData)
        );

        return new AdminSettlementResponse(
                scope, request.granularity(), request.timezone(), summary, chart, tableData, LocalDateTime.now()
        );
    }

    /**
     * 정산 데이터 포인트 생성 헬퍼 메서드
     * MonthlySettlementDto를 차트 및 테이블 데이터로 변환
     */
    private void addSettlementDataPoint(MonthlySettlementDto settlement,
                                       List<AdminSettlementResponse.DataPoint> grossSalesData,
                                       List<AdminSettlementResponse.DataPoint> artistPayoutData,
                                       List<AdminSettlementResponse.DataPoint> netIncomeData,
                                       List<AdminSettlementResponse.TableRow> tableData) {
        
        String bucketStart = settlement.date().toString(); // yyyy-MM-dd 형식
        long grossSales = settlement.totalAmount();
        long artistPayout = settlement.getArtistPayout();  // 90%
        long netIncome = settlement.getNetIncome();        // 10%

        grossSalesData.add(new AdminSettlementResponse.DataPoint(bucketStart, grossSales));
        artistPayoutData.add(new AdminSettlementResponse.DataPoint(bucketStart, artistPayout));
        netIncomeData.add(new AdminSettlementResponse.DataPoint(bucketStart, netIncome));
        tableData.add(new AdminSettlementResponse.TableRow(bucketStart, grossSales, artistPayout, netIncome));
    }

    @Override
    public AdminFundingResponse getFundings(AdminFundingSearchRequest request) {
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 펀딩 목록 조회 - userId: {}, role: {}, page: {}, size: {}, keyword: {}, status: {}, artistId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), request.page(), request.size(),
                request.keyword(), request.status(), request.artistId());

        // 페이징 및 정렬 설정
        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(), this::mapFundingSortField);

        // 실제 DB에서 펀딩 조회
        Page<Funding> fundingPage = fundingRepository.findAll(
                buildFundingSpecification(request),
                pageable
        );

        // Entity → DTO 변환
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
     * 펀딩 검색 조건 빌더 - Request DTO 활용
     */
    private org.springframework.data.jpa.domain.Specification<Funding> buildFundingSpecification(
            AdminFundingSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (제목, 작가명, 작가ID)
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String likePattern = "%" + request.keyword() + "%";

                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(criteriaBuilder.like(root.get("title"), likePattern)); // 제목
                keywordPredicates.add(criteriaBuilder.like(root.get("user").get("name"), likePattern)); // 작가명

                // 숫자인 경우 작가ID로도 검색
                try {
                    Long id = Long.parseLong(request.keyword());
                    keywordPredicates.add(criteriaBuilder.equal(root.get("user").get("id"), id));
                } catch (NumberFormatException e) {
                    // 숫자가 아니면 ID 검색 제외
                }

                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            // 펀딩 상태 필터
            if (request.status() != null && !request.status().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), FundingStatus.valueOf(request.status())));
            }

            // 작가 필터
            if (request.artistId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.artistId()));
            }

            // 달성률 필터 - 애플리케이션 레벨에서 처리하는 것이 더 안전
            // JPA Criteria에서 복잡한 계산식은 타입 문제가 발생할 수 있음
            // TODO: 필요시 네이티브 쿼리나 JPQL로 대체

            // 등록일 기간 필터
            if (request.registeredFrom() != null && !request.registeredFrom().isBlank()) {
                LocalDate start = LocalDate.parse(request.registeredFrom());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createDate"), start.atStartOfDay()));
            }
            if (request.registeredTo() != null && !request.registeredTo().isBlank()) {
                LocalDate end = LocalDate.parse(request.registeredTo());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createDate"), end.atTime(23, 59, 59)));
            }

            // 마감일 기간 필터
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
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 입점 신청 목록 조회 - userId: {}, role: {}, page: {}, size: {}, keyword: {}, status: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), request.page(), request.size(),
                request.keyword(), request.status());

        // 페이징 및 정렬 설정
        Pageable pageable = buildPageable(request.page(), request.size(), request.sort(), request.order(),
                sort -> "createDate"); // 기본 정렬은 신청일

        // 실제 DB에서 입점 신청 조회
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
        // JWT 토큰에서 관리자 정보 추출 및 권한 검증
        CustomUserDetails adminUser = validateAdminAuthentication();

        log.info("관리자 입점 신청 상세 조회 - userId: {}, role: {}, applicationId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), applicationId);

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
}
