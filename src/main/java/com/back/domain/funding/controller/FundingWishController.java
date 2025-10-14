package com.back.domain.funding.controller;

import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.service.FundingWishService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
@Tag(name = "펀딩 찜", description = "펀딩 찜 API")
public class FundingWishController {

    private final FundingWishService fundingWishService;

    @PostMapping("/{id}/wish")
    @Operation(summary = "펀딩 찜 추가", description = "특정 펀딩을 찜 목록에 추가합니다.")
    public ResponseEntity<RsData<Void>> addWish(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        fundingWishService.addWish(id, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "찜 목록에 추가되었습니다.", null));
    }

    @DeleteMapping("/{id}/wish")
    @Operation(summary = "펀딩 찜 취소", description = "찜 목록에서 펀딩을 제거합니다.")
    public ResponseEntity<RsData<Void>> removeWish(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        fundingWishService.removeWish(id, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "찜 목록에서 제거되었습니다.", null));
    }

    @GetMapping("/{id}/wish/check")
    @Operation(summary = "찜 여부 확인", description = "현재 사용자가 해당 펀딩을 찜했는지 확인합니다.")
    public ResponseEntity<RsData<Boolean>> checkWish(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        boolean isWished = fundingWishService.isWished(id, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "찜 여부 조회 성공", isWished));
    }

    @GetMapping("/wishes")
    @Operation(summary = "내 찜 목록 조회", description = "현재 사용자의 찜 목록을 조회합니다.")
    public ResponseEntity<RsData<Page<FundingCardDto>>> getMyWishList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FundingCardDto> wishList = fundingWishService.getMyWishList(userEmail, pageable);
        return ResponseEntity.ok(new RsData<>("200", "찜 목록 조회 성공", wishList));
    }
}