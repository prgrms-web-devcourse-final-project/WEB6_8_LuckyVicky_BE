package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingNewsCreateRequest;
import com.back.domain.funding.service.FundingNewsService;
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
@RequestMapping("/api/fundings")
@RequiredArgsConstructor
@Tag(name = "펀딩 새소식", description = "펀딩 새소식 API 컨트롤러")
public class FundingNewsController{
    private final FundingNewsService fundingNewsService;

    @PostMapping("/{id}/news")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 새소식 등록", description = "펀딩 새소식을 등록")
    public ResponseEntity<RsData<?>> addNews(
            @PathVariable @Positive Long id,
            @Valid @RequestBody FundingNewsCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail
    ) {
        Long newsId = fundingNewsService.addFundingNews(id, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RsData<>("201", "새소식이 등록되었습니다.", newsId));
    }
}

