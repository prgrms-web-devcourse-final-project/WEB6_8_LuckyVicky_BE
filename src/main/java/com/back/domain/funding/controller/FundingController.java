package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.response.FundingCreateResponse;
import com.back.domain.funding.dto.response.FundingDetailResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.service.FundingService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
@Tag(name = "펀딩", description = "펀딩 API 컨트롤러")
public class FundingController {

    private final FundingService fundingService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "펀딩 생성")
    public ResponseEntity<RsData<FundingCreateResponse>> createFunding(
            @Valid @RequestBody FundingCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        Funding funding = fundingService.createFunding(request, userEmail);

        FundingCreateResponse response = FundingCreateResponse.from(funding);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RsData<>("201", "펀딩이 생성되었습니다.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "펀딩 조회")
    public ResponseEntity<RsData<FundingDetailResponse>> getFunding(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(new RsData<>("200", "%d번 펀딩 조회 성공".formatted(id), fundingService.getFunding(id)));
    }
}