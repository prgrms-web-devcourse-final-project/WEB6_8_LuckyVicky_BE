package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.response.MoriCashBalanceResponseDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.service.MoriCashBalanceService;
import com.back.domain.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 모리캐시 잔액 Controller
 */
@Tag(name = "모리캐시 잔액", description = "모리캐시 잔액 및 거래 내역 조회 API")
@RestController
@RequestMapping("/api/moricash/balance")
@RequiredArgsConstructor
public class MoriCashBalanceController {

    private final MoriCashBalanceService moriCashBalanceService;

    /**
     * 내 모리캐시 잔액 조회
     */
    @GetMapping
    public ResponseEntity<MoriCashBalanceResponseDto> getMyBalance(
            @AuthenticationPrincipal User user) {
        
        MoriCashBalanceResponseDto response = moriCashBalanceService.getBalance(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 모리캐시 거래 내역 조회 (충전/사용 모두)
     */
    @GetMapping("/history")
    public ResponseEntity<Page<MoriCashPaymentResponseDto>> getMyHistory(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<MoriCashPaymentResponseDto> response = moriCashBalanceService.getHistory(user, pageable);
        return ResponseEntity.ok(response);
    }
}

