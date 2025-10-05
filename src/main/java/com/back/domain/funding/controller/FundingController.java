package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.request.FundingUpdateRequest;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.dto.response.FundingCreateResponse;
import com.back.domain.funding.dto.response.FundingDetailResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.service.FundingService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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
    @Operation(summary = "펀딩 상세 조회")
    public ResponseEntity<RsData<FundingDetailResponse>> getFunding(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(new RsData<>("200", "%d번 펀딩 조회 성공".formatted(id), fundingService.getFunding(id)));
    }

    @GetMapping
    @Operation(summary = "펀딩 목록 조회")
    public ResponseEntity<RsData<Page<FundingCardDto>>> getFundingList(
            @Parameter(description = "[필터링] 펀딩 상태 목록. 진행중(OPEN), 종료(CLOSED), 성공(SUCCESS), 실패(FAILED), 취소(CANCELED)",
                    example = "OPEN,CLOSED")
            @RequestParam(required = false) Set<FundingStatus> status,

            @Parameter(description = "[정렬] 정렬 기준. 인기순(popular), 최신순(recent), 마감임박(deadline), 목표금액높은순(highAmount)",
                    example = "popular")
            @RequestParam(defaultValue = "recent") String sortBy,

            @Parameter(description = "[필터링] 펀딩 제목 검색어 (부분 일치, 대소문자 무시)",
                    example = "키링")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "[필터링] 최소 옵션 가격 (원 단위)",
                    example = "10000")
            @RequestParam(required = false) Long minPrice,

            @Parameter(description = "[필터링] 최대 옵션 가격 (원 단위)",
                    example = "50000")
            @RequestParam(required = false) Long maxPrice,

            @Parameter(description = "[페이징] 조회할 페이지 번호 (0부터 시작)",
                    example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "[페이징] 한 페이지에 보여줄 펀딩 수",
                    example = "12")
            @RequestParam(defaultValue = "12") int size
            ) {
        Page<FundingCardDto> fundingList = fundingService.getFundingList(
                status, sortBy, keyword, minPrice, maxPrice, page, size
        );

        RsData<Page<FundingCardDto>> body = RsData.of("200", "펀딩 목록 조회 성공", fundingList);
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "펀딩 수정", description = "펀딩을 수정합니다. 목표 금액은 참여자가 없을 때만 수정할 수 있습니다.")
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    public ResponseEntity<RsData<FundingDetailResponse>> updateFunding(
            @PathVariable @Positive Long id,
            @RequestBody FundingUpdateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail) {
        fundingService.updateFunding(id, userEmail, request);
        FundingDetailResponse updatedFunding = fundingService.getFunding(id);
        return ResponseEntity.ok(new RsData<>("200", "펀딩이 수정되었습니다.", updatedFunding));
    }
}