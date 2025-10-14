package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingCommunityCreateRequest;
import com.back.domain.funding.service.FundingCommunityService;
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
@Tag(name = "펀딩 커뮤니티", description = "펀딩 커뮤니티 API 컨트롤러")
public class FundingCommunityController {

    private final FundingCommunityService fundingCommunityService;

    @PostMapping("/{id}/communities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 커뮤니티 글 작성")
    public ResponseEntity<RsData<?>> createFundingCommunity(
            @PathVariable @Positive Long id,
            @Valid @RequestBody FundingCommunityCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail
    ) {
        Long createdId = fundingCommunityService.create(id, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RsData<>("201", "커뮤니티 글이 등록되었습니다.", createdId));
    }

    @DeleteMapping("/{fundingId}/communities/{communityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 커뮤니티 글 삭제")
    public ResponseEntity<RsData<?>> deleteFundingCommunity(
            @PathVariable @Positive Long fundingId,
            @PathVariable @Positive Long communityId,
            @AuthenticationPrincipal(expression = "username") String userEmail
    ) {
        fundingCommunityService.delete(fundingId, communityId, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "커뮤니티 글이 삭제되었습니다."));
    }
}
