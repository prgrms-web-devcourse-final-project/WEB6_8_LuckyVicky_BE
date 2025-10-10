package com.back.domain.support.faq.controller;

import com.back.domain.support.faq.dto.request.FaqCreateRequest;
import com.back.domain.support.faq.dto.request.FaqUpdateRequest;
import com.back.domain.support.faq.dto.response.FaqDetailResponse;
import com.back.domain.support.faq.dto.response.FaqListResponse;
import com.back.domain.support.faq.entity.FaqCategory;
import com.back.domain.support.faq.service.FaqService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/support/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQ", description = "자주 묻는 질문 관련 API")
public class FaqController {

    private final FaqService faqService;

    /**
     * FAQ 목록 조회
     */
    @GetMapping
    @Operation(
            summary = "FAQ 목록 조회",
            description = "FAQ 목록을 페이징하여 조회합니다. 카테고리 필터링이 가능합니다."
    )
    public ResponseEntity<RsData<FaqListResponse>> getFaqs(
            @Parameter(description = "카테고리", example = "ACCOUNT")
            @RequestParam(required = false) FaqCategory category,

            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("FAQ 목록 조회 - category: {}, page: {}, size: {}", category, page, size);

        // 프론트는 1부터 시작 -> Spring Pageable은 0부터 시작하므로 -1 처리
        Pageable pageable = PageRequest.of(page - 1, size);

        FaqListResponse response = faqService.getFaqs(category, pageable);

        return ResponseEntity.ok(
                RsData.of("200", "FAQ 목록 조회 성공", response)
        );
    }

    /**
     * FAQ 상세 조회
     */
    @GetMapping("/{faqId}")
    @Operation(
            summary = "FAQ 상세 조회",
            description = "FAQ 상세 정보를 조회합니다. 조회 시 조회수가 1 증가합니다."
    )
    public ResponseEntity<RsData<FaqDetailResponse>> getFaq(
            @Parameter(description = "FAQ ID", example = "1", required = true)
            @PathVariable Long faqId) {

        log.info("FAQ 상세 조회 - faqId: {}", faqId);

        FaqDetailResponse response = faqService.getFaq(faqId);

        return ResponseEntity.ok(
                RsData.of("200", "FAQ 상세 조회 성공", response)
        );
    }

    // ========================================
    // 관리자 전용 API
    // ========================================

    /**
     * FAQ 생성 (관리자 전용)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "FAQ 생성 (관리자 전용)",
            description = "새로운 FAQ를 생성합니다."
    )
    public ResponseEntity<RsData<Long>> createFaq(
            @Valid @RequestBody FaqCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("FAQ 생성 요청 - adminId: {}, question: {}",
                adminUser.getUserId(), request.question());

        Long faqId = faqService.createFaq(request);

        return ResponseEntity.ok(
                RsData.of("200", "FAQ가 성공적으로 등록되었습니다.", faqId)
        );
    }

    /**
     * FAQ 수정 (관리자 전용)
     */
    @PutMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "FAQ 수정 (관리자 전용)",
            description = "FAQ를 수정합니다."
    )
    public ResponseEntity<RsData<Void>> updateFaq(
            @Parameter(description = "FAQ ID", example = "1", required = true)
            @PathVariable Long faqId,

            @Valid @RequestBody FaqUpdateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("FAQ 수정 요청 - adminId: {}, faqId: {}", adminUser.getUserId(), faqId);

        faqService.updateFaq(faqId, request);

        return ResponseEntity.ok(
                RsData.of("200", "FAQ가 성공적으로 수정되었습니다.")
        );
    }

    /**
     * FAQ 삭제 (관리자 전용)
     */
    @DeleteMapping("/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "FAQ 삭제 (관리자 전용)",
            description = "FAQ를 삭제합니다."
    )
    public ResponseEntity<RsData<Void>> deleteFaq(
            @Parameter(description = "FAQ ID", example = "1", required = true)
            @PathVariable Long faqId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("FAQ 삭제 요청 - adminId: {}, faqId: {}", adminUser.getUserId(), faqId);

        faqService.deleteFaq(faqId);

        return ResponseEntity.ok(
                RsData.of("200", "FAQ가 삭제되었습니다.")
        );
    }

}
