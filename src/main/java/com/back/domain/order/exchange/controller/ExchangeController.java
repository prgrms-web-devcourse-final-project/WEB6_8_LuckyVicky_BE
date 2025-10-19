package com.back.domain.order.exchange.controller;

import com.back.domain.order.exchange.dto.request.ExchangeRequestDto;
import com.back.domain.order.exchange.dto.response.ExchangeResponseDto;
import com.back.domain.order.exchange.service.ExchangeService;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
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
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
@Tag(name = "교환", description = "교환 관련 API")
public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * 교환 신청
     */
    @PostMapping
    @Operation(summary = "교환 신청", description = "배송완료된 주문에 대해 교환을 신청합니다.")
    public ResponseEntity<ExchangeResponseDto> createExchange(
            @Valid @RequestBody ExchangeRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user = customUserDetails.getUser();
        ExchangeResponseDto responseDto = exchangeService.createExchange(requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 교환 목록 조회
     */
    @GetMapping
    @Operation(summary = "교환 목록 조회", description = "사용자의 교환 목록을 조회합니다.")
    public ResponseEntity<Page<ExchangeResponseDto>> getExchangeList(
            @PageableDefault(
                size = 10,
                sort = "createDate",
                direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user = customUserDetails.getUser();
        Page<ExchangeResponseDto> result = exchangeService.getItemsByUser(user, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 교환 상세 조회
     */
    @GetMapping("/{exchangeId}")
    @Operation(summary = "교환 상세 조회", description = "특정 교환의 상세 정보를 조회합니다.")
    public ResponseEntity<ExchangeResponseDto> getExchangeDetail(
            @PathVariable Long exchangeId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User user = customUserDetails.getUser();
        ExchangeResponseDto result = exchangeService.getItem(exchangeId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * 교환 승인 (관리자용)
     */
    @PutMapping("/{exchangeId}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "교환 승인", description = "관리자가 교환을 승인합니다. (관리자 전용)")
    public ResponseEntity<ExchangeResponseDto> approveExchange(
            @PathVariable Long exchangeId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        User admin = customUserDetails.getUser();
        ExchangeResponseDto responseDto = exchangeService.approveItem(exchangeId, admin);
        return ResponseEntity.ok(responseDto);
    }
}
