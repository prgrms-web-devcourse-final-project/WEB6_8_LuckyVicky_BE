package com.back.domain.payment.settlement.controller;

import com.back.domain.payment.settlement.dto.request.WithdrawalRequestDto;
import com.back.domain.payment.settlement.dto.response.WithdrawalResponseDto;
import com.back.domain.payment.settlement.service.SettlementService;
import com.back.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "정산 관리 (작가)", description = "작가 정산 관련 API")
@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 환전 요청 (즉시 완료 처리)
     */
    @Operation(
        summary = "환전 요청 (데모용)", 
        description = "작가가 보유한 모리캐시를 환전 신청합니다. (영상 촬영용 데모)\n\n" +
                     "- 금액만 입력하면 즉시 정산완료 처리됩니다.\n" +
                     "- 계좌 정보는 더미 데이터로 자동 처리됩니다.\n" +
                     "- 수수료 10%가 자동 차감됩니다.\n" +
                     "- 모리캐시가 차감되고 통계가 자동 업데이트됩니다.\n\n" +
                     "보유 모리캐시는 /api/moricash/balance 에서 조회할 수 있습니다."
    )
    @PostMapping("/withdrawal")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<WithdrawalResponseDto> requestWithdrawal(
            @Validated @RequestBody WithdrawalRequestDto requestDto,
            @AuthenticationPrincipal User artist) {
        
        WithdrawalResponseDto response = settlementService.requestWithdrawal(requestDto, artist);
        return ResponseEntity.ok(response);
    }
}
