package com.back.domain.funding.controller;

import com.back.domain.funding.service.FundingStatusService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
@Tag(name = "펀딩 상태", description = "펀딩 상태 관리 API")
public class FundingStatusController {

    private final FundingStatusService fundingStatusService;

    @PutMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 종료", description = "펀딩 수동 종료")
    public ResponseEntity<RsData<?>> closeFunding(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {
        fundingStatusService.closeFunding(id, userEmail);
        return ResponseEntity.ok(RsData.of("200", "펀딩이 종료되었습니다."));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 취소", description = "펀딩 취소")
    public ResponseEntity<RsData<?>> cancelFunding(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {
        fundingStatusService.cancelFunding(id, userEmail);
        return ResponseEntity.ok(RsData.of("200", "펀딩이 취소되었습니다."));
    }

    @PutMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "단일 펀딩 최종 처리", description = "펀딩 성공/실패 확정 (관리자 전용) ")
    public ResponseEntity<RsData<?>> finalizeFunding(
            @PathVariable @Positive Long id) {
        fundingStatusService.finalizeFunding(id);
        return ResponseEntity.ok(RsData.of("200", "펀딩이 최종 처리되었습니다."));
    }

    @PutMapping("/finalize/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "모든 펀딩 최종 처리", description = "모든 펀딩 성공/실패 확정 (관리자 전용) ")
    public ResponseEntity<RsData<?>> finalizeAllFundings() {
        fundingStatusService.finalizeAllClosedFundings();
        return ResponseEntity.ok(RsData.of("200", "모든 펀딩이 최종 처리되었습니다."));
    }

    @PutMapping("/process/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "모든 펀딩 통합 처리",
            description = "만료된 펀딩을 종료하고, 종료된 펀딩을 최종 처리합니다. (종료 + 최종 처리 한 번에)")
    public ResponseEntity<RsData<?>> processAllFundings() {
        var result = fundingStatusService.processAllFundings();

        String message = String.format(
                "펀딩 통합 처리 완료 - 종료: %d건, 성공: %d건, 실패: %d건",
                result.closedCount(), result.successCount(), result.failedCount()
        );

        return ResponseEntity.ok(
                RsData.of("200", message)
        );
    }
}
