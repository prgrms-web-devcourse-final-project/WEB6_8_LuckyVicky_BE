package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.response.FundingCreateResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.service.FundingService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
@Tag(name = "FundingController", description = "펀딩 API 컨트롤러")
public class FundingController {

    private final FundingService fundingService;

    @PostMapping
    public ResponseEntity<RsData<FundingCreateResponse>> createFunding(
            @Valid @RequestBody FundingCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        Funding funding = fundingService.createFunding(request, userEmail);

        FundingCreateResponse response = FundingCreateResponse.from(funding);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RsData<>("201", "펀딩이 생성되었습니다.", response));
    }
}