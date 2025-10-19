package com.back.domain.payment.cash.controller;

import com.back.domain.payment.cash.dto.request.CashExchangeRequestDto;
import com.back.domain.payment.cash.dto.response.CashExchangeResponseDto;
import com.back.domain.payment.cash.service.CashExchangeService;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 캐시 환전 Controller (작가 전용)
 */
@Tag(name = "캐시 환전", description = "캐시 환전 관련 API (작가 전용)")
@RestController
@RequestMapping("/api/cash/exchange")
@RequiredArgsConstructor
public class CashExchangeController {

    private final CashExchangeService cashExchangeService;

    /**
     * 환전 신청 (작가만 가능)
     */
    @PostMapping
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<CashExchangeResponseDto> createExchangeRequest(
            @RequestBody CashExchangeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        User artist = customUserDetails.getUser();
        CashExchangeResponseDto response = cashExchangeService.createExchangeRequest(requestDto, artist);
        return ResponseEntity.ok(response);
    }

    /**
     * 환전 승인 (관리자만 가능)
     */
    @PostMapping("/{transactionId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CashExchangeResponseDto> approveExchange(
            @PathVariable Long transactionId,
            @RequestParam String pgTransactionId,
            @RequestParam String pgApprovalNumber) {
        
        CashExchangeResponseDto response = cashExchangeService.approveExchange(
                transactionId, pgTransactionId, pgApprovalNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * 환전 거부 (관리자만 가능)
     */
    @PostMapping("/{transactionId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectExchange(
            @PathVariable Long transactionId,
            @RequestParam String reason) {
        
        cashExchangeService.rejectExchange(transactionId, reason);
        return ResponseEntity.ok().build();
    }
}

