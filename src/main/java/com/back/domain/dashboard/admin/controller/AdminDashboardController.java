package com.back.domain.dashboard.admin.controller;

import com.back.domain.artist.dto.request.RejectArtistApplicationRequest;
import com.back.domain.artist.service.ArtistApplicationAdminService;
import com.back.domain.dashboard.admin.dto.request.*;
import com.back.domain.dashboard.admin.dto.response.*;
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
 * <p>
 * 관리자가 전체 플랫폼 현황을 모니터링할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증과 관리자 권한이 필요
 * <p>
 * 제공 기능:
 * <ul>
 *   <li>전체 현황 조회 (사용자/매출/펀딩 통계, 카테고리별 분포)</li>
 *   <li>매출 및 주문 트렌드 조회</li>
 *   <li>사용자 증가 현황 조회</li>
 *   <li>승인 대기 알림 조회</li>
 *   <li>상품 목록 조회 및 관리</li>
 *   <li>사용자 목록 조회 및 관리</li>
 *   <li>펀딩 모니터링 목록 조회</li>
 *   <li>입점 신청 목록 조회 및 관리</li>
 * </ul>
 * <p>
 * 2025.10.04 수정 - GA4 유입 경로 제거 (작가 대시보드 전용)
 */
@RestController
@RequestMapping("/api/dashboard/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자용 대시보드", description = "관리자 대시보드 컨트롤러")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final ArtistApplicationAdminService artistApplicationAdminService;

    /**
     * 관리자 대시보드 전체 현황 조회
     */
    @GetMapping("/overview")
    @Operation(summary = "관리자 대시보드 전체 현황 조회",
            description = "사용자, 매출, 펀딩 통계와 트렌드 차트, 승인 대기 알림, 카테고리별 상품 분포 정보를 조회합니다")
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

        log.info("관리자 펀딩 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}, status: {}, artistId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword(), request.status(),
                request.artistId());

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

    /**
     * 관리자 작가 입점 신청 승인
     */
    @PostMapping("/artist-applications/{applicationId}/approve")
    @Operation(summary = "관리자 작가 입점 신청 승인", description = "특정 입점 신청을 승인하여 사용자를 작가로 전환합니다")
    public ResponseEntity<RsData<Void>> approveArtistApplication(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @PathVariable Long applicationId) {

        artistApplicationAdminService.approveApplication(
                applicationId,
                adminUser.getUserId(),
                adminUser.getUsername()
        );

        return ResponseEntity.ok(RsData.ok("작가 신청이 승인되었습니다."));
    }

    /**
     * 관리자 작가 입점 신청 거절
     */
    @PostMapping("/artist-applications/{applicationId}/reject")
    @Operation(summary = "관리자 작가 입점 신청 거절", description = "특정 입점 신청을 거절합니다. 거절 사유를 반드시 포함해야 합니다.")
    public ResponseEntity<RsData<Void>> rejectArtistApplication(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @PathVariable Long applicationId,
            @Valid @RequestBody RejectArtistApplicationRequest request) {

        artistApplicationAdminService.rejectApplication(
                applicationId,
                adminUser.getUserId(),
                adminUser.getUsername(),
                request.rejectionReason()
        );

        return ResponseEntity.ok(RsData.ok("작가 신청이 거절되었습니다."));
    }

    /**
     * 관리자 펀딩 승인 대기 목록 조회
     */
    @GetMapping("/fundings/approvals")
    @Operation(summary = "관리자 펀딩 승인 대기 목록 조회",
            description = "PENDING 상태의 펀딩 목록을 조회합니다. 관리자가 승인(APPROVED)할 수 있습니다.")
    public ResponseEntity<RsData<AdminFundingApprovalResponse>> getFundingApprovals(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @Valid @ModelAttribute AdminFundingApprovalSearchRequest request) {

        log.info("관리자 펀딩 승인 대기 목록 조회 - adminId: {}, role: {}, page: {}, size: {}, keyword: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(),
                request.page(), request.size(), request.keyword());

        AdminFundingApprovalResponse response = adminDashboardService.getFundingApprovals(request);

        return ResponseEntity.ok(RsData.ok("펀딩 승인 대기 목록 조회 성공", response));
    }

    /**
     * 관리자 펀딩 승인 대기 상세 조회
     */
    @GetMapping("/fundings/approvals/{fundingId}")
    @Operation(summary = "관리자 펀딩 승인 대기 상세 조회",
            description = "PENDING 상태의 펀딩 상세 정보를 조회합니다. 작가 정보와 사업자 정보를 포함합니다.")
    public ResponseEntity<RsData<AdminFundingApprovalDetailResponse>> getFundingApprovalDetail(
            @AuthenticationPrincipal CustomUserDetails adminUser,
            @PathVariable Long fundingId) {

        log.info("관리자 펀딩 승인 대기 상세 조회 - adminId: {}, role: {}, fundingId: {}",
                adminUser.getUserId(), adminUser.getCurrentRole(), fundingId);

        AdminFundingApprovalDetailResponse response = adminDashboardService.getFundingApprovalDetail(fundingId);

        return ResponseEntity.ok(RsData.ok("펀딩 승인 대기 상세 조회 성공", response));
    }
}
