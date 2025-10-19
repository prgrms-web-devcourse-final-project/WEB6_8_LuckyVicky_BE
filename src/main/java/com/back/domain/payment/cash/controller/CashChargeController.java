package com.back.domain.payment.cash.controller;

import com.back.domain.payment.cash.dto.request.CashChargeRequestDto;
import com.back.domain.payment.cash.dto.response.CashChargeResponseDto;
import com.back.domain.payment.cash.service.CashChargeService;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 캐시 충전 Controller
 */
@Tag(name = "캐시 충전", description = "캐시 충전 관련 API")
@RestController
@RequestMapping("/api/cash/charge")
@RequiredArgsConstructor
public class CashChargeController {

    private final CashChargeService cashChargeService;

    /**
     * 캐시 충전 신청
     */
    @PostMapping
    public ResponseEntity<CashChargeResponseDto> createChargeRequest(
            @RequestBody CashChargeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        User user = customUserDetails.getUser();
        CashChargeResponseDto response = cashChargeService.createChargeRequest(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 캐시 충전 완료 (PG 승인 후)
     */
    @PostMapping("/{transactionId}/complete")
    public ResponseEntity<CashChargeResponseDto> completeCharge(
            @PathVariable Long transactionId,
            @RequestParam String paymentKey,
            @RequestParam String orderId) {
        
        CashChargeResponseDto response = cashChargeService.completeCharge(transactionId, paymentKey, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 캐시 충전 실패
     */
    @PostMapping("/{transactionId}/fail")
    public ResponseEntity<Void> failCharge(
            @PathVariable Long transactionId,
            @RequestParam String failureReason) {
        
        cashChargeService.failCharge(transactionId, failureReason);
        return ResponseEntity.ok().build();
    }

    /**
     * 캐시 충전 취소
     */
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<Void> cancelCharge(
            @PathVariable Long transactionId,
            @RequestParam String paymentKey,
            @RequestParam String cancellationReason) {
        
        cashChargeService.cancelCharge(transactionId, paymentKey, cancellationReason);
        return ResponseEntity.ok().build();
    }
}

