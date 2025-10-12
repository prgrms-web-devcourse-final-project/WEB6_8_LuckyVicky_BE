package com.back.domain.payment.moriCash.controller;

import com.back.domain.payment.moriCash.dto.request.MoriCashPaymentRequestDto;
import com.back.domain.payment.moriCash.dto.response.MoriCashPaymentResponseDto;
import com.back.domain.payment.moriCash.service.MoriCashPaymentService;
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
 * 모리캐시 결제 Controller
 */
@Tag(name = "모리캐시 결제", description = "모리캐시 결제 관련 API")
@RestController
@RequestMapping("/api/moricash/payments")
@RequiredArgsConstructor
public class MoriCashPaymentController {

    private final MoriCashPaymentService moriCashPaymentService;

    /**
     * 모리캐시 결제
     */
    @PostMapping
    public ResponseEntity<MoriCashPaymentResponseDto> createPayment(
            @RequestBody MoriCashPaymentRequestDto requestDto,
            @AuthenticationPrincipal User user) {
        
        MoriCashPaymentResponseDto response = moriCashPaymentService.createPayment(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 모리캐시 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal User user) {
        
        moriCashPaymentService.cancelPayment(paymentId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 모리캐시 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<MoriCashPaymentResponseDto> getPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal User user) {
        
        MoriCashPaymentResponseDto response = moriCashPaymentService.getPayment(paymentId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 모리캐시 결제 내역 조회
     */
    @GetMapping
    public ResponseEntity<Page<MoriCashPaymentResponseDto>> getMyPayments(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<MoriCashPaymentResponseDto> response = moriCashPaymentService.getPayments(user, pageable);
        return ResponseEntity.ok(response);
    }
}

