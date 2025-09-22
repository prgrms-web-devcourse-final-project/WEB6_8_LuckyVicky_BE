package com.back.domain.dashboard.artist.controller;

import com.back.domain.dashboard.artist.dto.response.ArtistMainResponse;
import com.back.domain.dashboard.artist.service.ArtistDashboardService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 작가용 대시보드 컨트롤러
 * 추후 도메인 되면 GA 사용법 적용하기.
 * 작가가 자신의 판매 현황, 통계, 트렌드 등을 조회할 수 있는 대시보드 기능을 제공
 * 모든 API는 JWT 인증이 필요
 * 
 * 제공 기능:
 * <ul>
 *   <li>메인 현황 조회 (프로필, 통계, 트렌드, 알림)</li>
 *   <li>매출/주문/팔로워 트렌드 분석</li>
 *   <li>실시간 알림 확인</li>
 * </ul>
 * 2025.09.22 생성
 */
@RestController
@RequestMapping("/api/dashboard/artist")
@RequiredArgsConstructor
@Tag(name = "ArtistDashboardController", description = "작가용 대시보드 컨트롤러")
public class ArtistDashboardController {
    
    private final ArtistDashboardService artistDashboardService;
    
    /**
     * 작가 대시보드 메인 현황 조회
     * 작가의 프로필, 통계, 트렌드, 알림 정보를 조회
     * 기간별 필터링과 트렌드 분석이 가능
     * 
     * @param authorization Bearer 토큰 (필수)
     * @param range 기간 범위 (1M, 3M, 6M, 1Y, ALL 중 선택, 기본값: 1M)
     * @param from 시작일 (yyyy-MM-dd 형식, 직접 범위 지정 시 사용)
     * @param to 종료일 (yyyy-MM-dd 형식, 직접 범위 지정 시 사용)
     * @param interval 트렌드 간격 (AUTO, DAY, WEEK, MONTH 중 선택, 기본값: AUTO)
     * @param tz 타임존 (기본값: Asia/Seoul)
     * @return 작가 대시보드 메인 현황 정보
     * 
     * @throws SecurityException 인증 토큰이 유효하지 않은 경우
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    @GetMapping("/main")
    @Operation(summary = "작가 대시보드 메인 현황 조회", description = "작가의 프로필, 통계, 트렌드, 알림 정보를 조회합니다")
    public ResponseEntity<RsData<ArtistMainResponse>> getMainStats(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "1M") 
            @Pattern(regexp = "^(1M|3M|6M|1Y|ALL)$", 
                    message = "range는 1M, 3M, 6M, 1Y, ALL 중 하나여야 합니다")
            String range,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String from,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다")
            String to,
            @RequestParam(defaultValue = "AUTO") 
            @Pattern(regexp = "^(AUTO|DAY|WEEK|MONTH)$", 
                    message = "interval은 AUTO, DAY, WEEK, MONTH 중 하나여야 합니다")
            String interval,
            @RequestParam(defaultValue = "Asia/Seoul") String tz) {
        
        ArtistMainResponse response = artistDashboardService.getMainStats(
                authorization, range, from, to, interval, tz);
        
        return ResponseEntity.ok(RsData.of(
                "200-OK",
                "작가 대시보드 메인 조회 성공",
                response
        ));
    }
}
