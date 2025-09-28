package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminFundingSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.request.AdminProductSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminSettlementRequest;
import com.back.domain.dashboard.admin.dto.request.AdminUserSearchRequest;
import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;
import com.back.domain.dashboard.admin.service.AdminDashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 대시보드 컨트롤러
 * 
 * 관리자가 전체 플랫폼 현황을 모니터링할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증과 관리자 권한이 필요
 * 
 * 제공 기능:
 * <ul>
 *   <li>전체 현황 조회 (사용자/매출/펀딩 통계)</li>
 *   <li>매출 및 주문 트렌드 조회</li>
 *   <li>사용자 증가 현황 조회</li>
 *   <li>카테고리별 상품 분포 조회</li>
 *   <li>승인 대기 알림 조회</li>
 *   <li>상품 목록 조회 및 관리</li>
 *   <li>사용자 목록 조회 및 관리</li>
 *   <li>펀딩 모니터링 목록 조회</li>
 * </ul>
 * 
 * 2025.09.26 신규 생성
 */
@RestController
@RequestMapping("/api/dashboard/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AdminDashboardController", description = "관리자 대시보드 컨트롤러")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 관리자 대시보드 전체 현황 조회
     */
    @GetMapping("/overview")
    @Operation(summary = "관리자 대시보드 전체 현황 조회", 
               description = "사용자, 매출, 펀딩 통계와 트렌드 차트, 승인 대기 알림을 조회합니다")
    public ResponseEntity<RsData<AdminOverviewResponse>> getOverview(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @Valid @ModelAttribute AdminOverviewRequest request) {

        log.info("관리자 대시보드 전체 현황 조회 - range: {}, granularity: {}, timezone: {}, adminRole: {}", 
                request.range(), request.granularity(), request.timezone(), adminRole);

        AdminOverviewResponse response = adminDashboardService.getOverview(
                authorization, adminRole, request.range(), request.granularity(), 
                request.period(), request.timezone());

        return ResponseEntity.ok(RsData.ok("관리자 메인 현황 조회 성공", response));
    }

    /**
     * 관리자 상품 목록 조회
     */
    @GetMapping("/products")
    @Operation(summary = "관리자 상품 목록 조회",
               description = "전체 상품을 페이지 단위로 조회하고 필터링/정렬할 수 있습니다. metrics=true 시 평균평점/리뷰/매출 포함")
    public ResponseEntity<RsData<AdminProductResponse>> getProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @Valid @ModelAttribute AdminProductSearchRequest request) {

        log.info("관리자 상품 목록 조회 - page: {}, size: {}, keyword: {}, sellingStatus: {}, metrics: {}, adminRole: {}",
                request.page(), request.size(), request.keyword(), request.sellingStatus(), request.metrics(), adminRole);

        AdminProductResponse response = adminDashboardService.getProducts(
                authorization, adminRole, request.page(), request.size(),
                request.keyword(), request.sellingStatus(), request.categoryId(), request.artistId(),
                request.startDate(), request.endDate(), request.sort(), request.order(), request.metrics());

        return ResponseEntity.ok(RsData.ok("관리자 상품 목록 조회 성공", response));
    }

    /**
     * 관리자 사용자 목록 조회
     */
    @GetMapping("/users")
    @Operation(summary = "관리자 사용자 목록 조회",
               description = "전체 사용자를 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminUserResponse>> getUsers(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @Valid @ModelAttribute AdminUserSearchRequest request) {

        log.info("관리자 사용자 목록 조회 - page: {}, size: {}, keyword: {}, role: {}, accountStatus: {}, grade: {}, adminRole: {}",
                request.page(), request.size(), request.keyword(), request.role(), 
                request.accountStatus(), request.grade(), adminRole);

        AdminUserResponse response = adminDashboardService.getUsers(
                authorization, adminRole, request.page(), request.size(),
                request.keyword(), request.role(), request.accountStatus(), request.grade(),
                request.joinedStartDate(), request.joinedEndDate(), request.artistId(),
                request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("관리자 사용자 목록 조회 성공", response));
    }

    /**
     * 관리자 매출/정산 집계 조회
     */
    @GetMapping("/settlements")
    @Operation(summary = "관리자 매출/정산 집계 조회",
               description = "연도 또는 월별 매출/정산 데이터를 조회합니다. month 전달 시 일별 집계로 전환됩니다")
    public ResponseEntity<RsData<AdminSettlementResponse>> getSettlements(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @Valid @ModelAttribute AdminSettlementRequest request) {

        log.info("관리자 매출/정산 조회 - year: {}, month: {}, granularity: {}, timezone: {}, adminRole: {}",
                request.year(), request.month(), request.granularity(), request.timezone(), adminRole);

        AdminSettlementResponse response = adminDashboardService.getSettlements(
                authorization, adminRole, request.year(), request.month(),
                request.granularity(), request.timezone());

        return ResponseEntity.ok(RsData.ok("관리자 매출/정산 조회 성공", response));
    }

    /**
     * 관리자 펀딩 모니터링 목록 조회
     */
    @GetMapping("/fundings")
    @Operation(summary = "관리자 펀딩 모니터링 목록 조회",
               description = "전체 펀딩을 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminFundingResponse>> getFundings(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Admin-Role", required = false) String adminRole,
            @Valid @ModelAttribute AdminFundingSearchRequest request) {

        log.info("관리자 펀딩 목록 조회 - page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, artistId: {}, adminRole: {}",
                request.page(), request.size(), request.keyword(), request.status(), 
                request.categoryId(), request.artistId(), adminRole);

        AdminFundingResponse response = adminDashboardService.getFundings(
                authorization, adminRole, request.page(), request.size(),
                request.keyword(), request.status(), request.categoryId(), request.artistId(),
                request.minAchievement(), request.maxAchievement(),
                request.registeredFrom(), request.registeredTo(),
                request.dueFrom(), request.dueTo(), request.sort(), request.order());

        return ResponseEntity.ok(RsData.ok("관리자 펀딩 모니터링 조회 성공", response));
    }
}
