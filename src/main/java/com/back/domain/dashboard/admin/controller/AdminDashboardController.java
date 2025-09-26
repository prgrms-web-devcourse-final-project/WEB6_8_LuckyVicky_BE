package com.back.domain.dashboard.admin.controller;

import com.back.domain.dashboard.admin.dto.request.AdminOverviewRequest;
import com.back.domain.dashboard.admin.dto.response.AdminOverviewResponse;
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
}
