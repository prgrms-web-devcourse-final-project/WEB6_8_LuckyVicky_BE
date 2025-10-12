package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.request.MoriCashRefundRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashRefundResponseDto;
import com.back.domain.payment.moriCash.service.MoriCashRefundService;
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
 * 모리캐시 환불 Controller
 */
@Tag(name = "모리캐시 환불", description = "모리캐시 환불 관련 API")
@RestController
@RequestMapping("/api/moricash/refunds")
@RequiredArgsConstructor
public class MoriCashRefundController {

    private final MoriCashRefundService moriCashRefundService;

    /**
     * 모리캐시 환불 처리
     */
    @PostMapping
    public ResponseEntity<MoriCashRefundResponseDto> processRefund(
            @RequestBody MoriCashRefundRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        MoriCashRefundResponseDto response = moriCashRefundService.processRefund(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 모리캐시 환불 취소
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelRefund(
            @PathVariable Long paymentId,
            @RequestParam String cancellationReason) {
        
        moriCashRefundService.cancelRefund(paymentId, cancellationReason);
        return ResponseEntity.ok().build();
    }

    /**
     * 모리캐시 환불 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<MoriCashRefundResponseDto> getRefund(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal User user) {
        
        MoriCashRefundResponseDto response = moriCashRefundService.getRefund(paymentId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 모리캐시 환불 내역 조회
     */
    @GetMapping
    public ResponseEntity<Page<MoriCashRefundResponseDto>> getMyRefunds(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<MoriCashRefundResponseDto> response = moriCashRefundService.getRefunds(user, pageable);
        return ResponseEntity.ok(response);
    }
}

