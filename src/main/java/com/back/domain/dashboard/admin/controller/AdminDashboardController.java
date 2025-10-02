package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminArtistApplicationSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminFundingSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.request.AdminProductSearchRequest;
import com.back.domain.dashboard.admin.dto.request.AdminSettlementRequest;
import com.back.domain.dashboard.admin.dto.request.AdminUserSearchRequest;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationDetailResponse;
import com.back.domain.dashboard.admin.dto.response.AdminArtistApplicationResponse;
import com.back.domain.dashboard.admin.dto.response.AdminFundingResponse;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
import com.back.domain.dashboard.admin.dto.response.AdminProductResponse;
import com.back.domain.dashboard.admin.dto.response.AdminSettlementResponse;
import com.back.domain.dashboard.admin.dto.response.AdminUserResponse;
import com.back.domain.dashboard.admin.service.AdminDashboardService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 대시보드 컨트롤러
<<<<<<< HEAD
 * 관리자가 전체 플랫폼 현황을 모니터링할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증과 관리자 권한이 필요
=======
 * <p>
 * 관리자가 전체 플랫폼 현황을 모니터링할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증과 관리자 권한이 필요
 * <p>
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
 * 제공 기능:
 * <ul>
 *   <li>전체 현황 조회 (사용자/매출/펀딩 통계, 유입 경로)</li>
 *   <li>매출 및 주문 트렌드 조회</li>
 *   <li>사용자 증가 현황 조회</li>
 *   <li>카테고리별 상품 분포 조회</li>
 *   <li>승인 대기 알림 조회</li>
 *   <li>상품 목록 조회 및 관리</li>
 *   <li>사용자 목록 조회 및 관리</li>
 *   <li>펀딩 모니터링 목록 조회</li>
 *   <li>입점 신청 목록 조회 및 관리</li>
 * </ul>
<<<<<<< HEAD
 * 2025.10.01 GA4 유입 경로 통합 - 메인 현황에 포함
 * 2025.10.02 JWT 표준 패턴 적용 - @AuthenticationPrincipal 사용
=======
 * <p>
 * 2025.10.01 GA4 유입 경로 통합 - 메인 현황에 포함
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
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
            description = "사용자, 매출, 펀딩 통계와 트렌드 차트, 승인 대기 알림, 유입 경로(GA4) 정보를 조회합니다")
    public ResponseEntity<RsData<AdminOverviewResponse>> getOverview(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminOverviewRequest request) {

        log.info("관리자 대시보드 전체 현황 조회 - adminId: {}, role: {}, range: {}, granularity: {}, timezone: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.range(), request.granularity(), request.timezone());

        AdminOverviewResponse response = adminDashboardService.getOverview(request);

        return ResponseEntity.ok(RsData.ok("관리자 메인 현황 조회 성공", response));
    }

    /**
     * 관리자 상품 목록 조회
     */
    @GetMapping("/products")
    @Operation(summary = "관리자 상품 목록 조회",
            description = "전체 상품을 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminProductResponse>> getProducts(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminProductSearchRequest request) {

        log.info("관리자 상품 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}, sellingStatus: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword(), request.sellingStatus());

        AdminProductResponse response = adminDashboardService.getProducts(request);

        return ResponseEntity.ok(RsData.ok("관리자 상품 목록 조회 성공", response));
    }

    /**
     * 관리자 사용자 목록 조회
     */
    @GetMapping("/users")
    @Operation(summary = "관리자 사용자 목록 조회",
            description = "전체 사용자를 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminUserResponse>> getUsers(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminUserSearchRequest request) {

        log.info("관리자 사용자 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}, role: {}, accountStatus: {}, grade: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword(), request.role(),
                request.accountStatus(), request.grade());

        AdminUserResponse response = adminDashboardService.getUsers(request);

        return ResponseEntity.ok(RsData.ok("관리자 사용자 목록 조회 성공", response));
    }

    /**
     * 관리자 매출/정산 집계 조회
     */
    @GetMapping("/settlements")
    @Operation(summary = "관리자 매출/정산 집계 조회",
            description = "연도 또는 월별 매출/정산 데이터를 조회합니다. month 전달 시 일별 집계로 전환됩니다")
    public ResponseEntity<RsData<AdminSettlementResponse>> getSettlements(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminSettlementRequest request) {

        log.info("관리자 매출/정산 조회 - adminId: {}, role: {}, year: {}, month: {}, granularity: {}, timezone: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.year(), request.month(), request.granularity(), request.timezone());

        AdminSettlementResponse response = adminDashboardService.getSettlements(request);

        return ResponseEntity.ok(RsData.ok("관리자 매출/정산 조회 성공", response));
    }

    /**
     * 관리자 펀딩 모니터링 목록 조회
     */
    @GetMapping("/fundings")
    @Operation(summary = "관리자 펀딩 모니터링 목록 조회",
            description = "전체 펀딩을 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminFundingResponse>> getFundings(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminFundingSearchRequest request) {

        log.info("관리자 펀딩 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}, status: {}, categoryId: {}, artistId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword(), request.status(),
                request.categoryId(), request.artistId());

        AdminFundingResponse response = adminDashboardService.getFundings(request);

        return ResponseEntity.ok(RsData.ok("관리자 펀딩 모니터링 조회 성공", response));
    }

    /**
     * 관리자 입점 신청 목록 조회
     */
    @GetMapping("/artist-applications")
    @Operation(summary = "관리자 입점 신청 목록 조회",
            description = "전체 입점 신청을 페이지 단위로 조회하고 필터링/정렬할 수 있습니다")
    public ResponseEntity<RsData<AdminArtistApplicationResponse>> getArtistApplications(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminArtistApplicationSearchRequest request) {

        log.info("관리자 입점 신청 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}, status: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword(), request.status());

        AdminArtistApplicationResponse response = adminDashboardService.getArtistApplications(request);

        return ResponseEntity.ok(RsData.ok("입점 신청 목록 조회 성공", response));
    }

    /**
     * 관리자 입점 신청 상세 조회
     */
    @GetMapping("/artist-applications/{applicationId}")
    @Operation(summary = "관리자 입점 신청 상세 조회",
            description = "특정 입점 신청의 상세 정보를 조회합니다")
    public ResponseEntity<RsData<AdminArtistApplicationDetailResponse>> getArtistApplicationDetail(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @PathVariable Long applicationId) {

        log.info("관리자 입점 신청 상세 조회 - adminId: {}, role: {}, applicationId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), applicationId);

        AdminArtistApplicationDetailResponse response = adminDashboardService.getArtistApplicationDetail(applicationId);

        return ResponseEntity.ok(RsData.ok("입점 신청 상세 조회 성공", response));
    }
}
