package com.back.domain.notice.controller;

import com.back.domain.notice.dto.request.NoticeCreateRequest;
import com.back.domain.notice.dto.request.NoticeUpdateRequest;
import com.back.domain.notice.dto.response.NoticeDetailResponse;
import com.back.domain.notice.dto.response.NoticeListResponse;
import com.back.domain.notice.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "공지사항", description = "공지사항 관련 API")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록 조회
     */
    @GetMapping
    @Operation(
            summary = "공지사항 목록 조회",
            description = "공지사항 목록을 페이징하여 조회합니다. 검색 키워드를 입력하면 제목/내용 검색이 가능합니다."
    )
    public ResponseEntity<RsData<NoticeListResponse>> getNotices(
            @Parameter(description = "검색 키워드 (제목 또는 내용)", example = "배송")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("공지사항 목록 조회 - keyword: {}, page: {}, size: {}", keyword, page, size);

        // 프론트는 1부터 시작 -> Spring Pageable은 0부터 시작하므로 -1 처리
        Pageable pageable = PageRequest.of(page - 1, size);

        NoticeListResponse response = noticeService.getNotices(keyword, pageable);

        return ResponseEntity.ok(
                RsData.of("200", "공지사항 목록 조회 성공", response)
        );
    }

    /**
     * 공지사항 상세 조회
     */
    @GetMapping("/{noticeId}")
    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 상세 정보를 조회합니다. 조회 시 조회수가 1 증가합니다."
    )
    public ResponseEntity<RsData<NoticeDetailResponse>> getNotice(
            @Parameter(description = "공지사항 ID", example = "1", required = true)
            @PathVariable Long noticeId) {

        log.info("공지사항 상세 조회 - noticeId: {}", noticeId);

        NoticeDetailResponse response = noticeService.getNotice(noticeId);

        return ResponseEntity.ok(
                RsData.of("200", "공지사항 상세 조회 성공", response)
        );
    }


    // ========================================
    // 관리자 전용 API
    // ========================================

    /**
     * 공지사항 생성
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "공지사항 생성 (관리자 전용)",
            description = "새로운 공지사항을 생성합니다. 첨부파일은 최대 5개까지 업로드 가능합니다."
    )
    public ResponseEntity<RsData<Long>> createNotice(
            @Valid @ModelAttribute NoticeCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("공지사항 생성 요청 - adminId: {}, title: {}", adminUser.getUserId(), request.title());

        Long noticeId = noticeService.createNotice(request);

        return ResponseEntity.ok(
                RsData.of("200", "공지사항이 성공적으로 등록되었습니다.", noticeId)
        );
    }

    /**
     * 공지사항 수정
     */
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "공지사항 수정 (관리자 전용)",
            description = "공지사항을 수정합니다. 기존 첨부파일 삭제 및 새 첨부파일 추가가 가능합니다."
    )
    public ResponseEntity<RsData<Void>> updateNotice(
            @Parameter(description = "공지사항 ID", example = "1", required = true)
            @PathVariable Long noticeId,

            @Valid @ModelAttribute NoticeUpdateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("공지사항 수정 요청 - adminId: {}, noticeId: {}", adminUser.getUserId(), noticeId);

        noticeService.updateNotice(noticeId, request);

        return ResponseEntity.ok(
                RsData.of("200", "공지사항이 성공적으로 수정되었습니다.")
        );
    }

    /**
     * 공지사항 삭제
     */
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "공지사항 삭제 (관리자 전용)",
            description = "공지사항을 삭제합니다. 첨부파일도 함께 삭제됩니다."
    )
    public ResponseEntity<RsData<Void>> deleteNotice(
            @Parameter(description = "공지사항 ID", example = "1", required = true)
            @PathVariable Long noticeId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails adminUser) {

        log.info("공지사항 삭제 요청 - adminId: {}, noticeId: {}", adminUser.getUserId(), noticeId);

        noticeService.deleteNotice(noticeId);

        return ResponseEntity.ok(
                RsData.of("200", "공지사항이 삭제되었습니다.")
        );
    }

}
