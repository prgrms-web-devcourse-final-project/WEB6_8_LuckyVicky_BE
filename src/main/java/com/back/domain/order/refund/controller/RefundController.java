package com.back.domain.order.refund.controller;

import com.back.domain.order.refund.dto.request.RefundRequestDto;
import com.back.domain.order.refund.dto.response.RefundResponseDto;
import com.back.domain.order.refund.service.RefundService;
import com.back.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "환불", description = "환불 관련 API")
public class RefundController {

    private final RefundService refundService;

    /**
     * 환불 신청
     */
    @PostMapping
    @Operation(summary = "환불 신청", description = "배송완료된 주문에 대해 환불을 신청합니다.")
    public ResponseEntity<RefundResponseDto> createRefund(
            @Valid @RequestBody RefundRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        RefundResponseDto responseDto = refundService.createRefund(requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 환불 목록 조회
     */
    @GetMapping
    @Operation(summary = "환불 목록 조회", description = "사용자의 환불 목록을 조회합니다.")
    public ResponseEntity<Page<RefundResponseDto>> getRefundList(
            @PageableDefault(
                size = 10,
                sort = "createDate",
                direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {
        Page<RefundResponseDto> result = refundService.getItemsByUser(user, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 환불 상세 조회
     */
    @GetMapping("/{refundId}")
    @Operation(summary = "환불 상세 조회", description = "특정 환불의 상세 정보를 조회합니다.")
    public ResponseEntity<RefundResponseDto> getRefundDetail(
            @PathVariable Long refundId,
            @AuthenticationPrincipal User user
    ) {
        RefundResponseDto result = refundService.getItem(refundId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * 환불 승인 (관리자용)
     */
    @PutMapping("/{refundId}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "환불 승인", description = "관리자가 환불을 승인합니다. (관리자 전용)")
    public ResponseEntity<RefundResponseDto> approveRefund(
            @PathVariable Long refundId,
            @AuthenticationPrincipal User admin
    ) {
        RefundResponseDto responseDto = refundService.approveItem(refundId, admin);
        return ResponseEntity.ok(responseDto);
    }
}
