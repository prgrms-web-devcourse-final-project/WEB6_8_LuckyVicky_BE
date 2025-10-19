package com.back.domain.support.inquiry.controller;

import com.back.domain.support.inquiry.dto.request.InquiryCreateRequest;
import com.back.domain.support.inquiry.dto.request.InquiryReplyRequest;
import com.back.domain.support.inquiry.dto.request.InquiryUpdateRequest;
import com.back.domain.support.inquiry.dto.response.InquiryDetailResponse;
import com.back.domain.support.inquiry.dto.response.InquiryListResponse;
import com.back.domain.support.inquiry.service.InquiryService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/support/inquiries")
@RequiredArgsConstructor
@Tag(name = "문의", description = "1:1 문의 관련 API")
public class InquiryController {

    private final InquiryService inquiryService;
    private final UserRepository userRepository;

    /**
     * 공개 문의 목록 조회 (비로그인)
     */
    @GetMapping("/public")
    @Operation(
            summary = "공개 문의 목록 조회 (비로그인)",
            description = "비밀문의를 제외한 공개 문의 목록을 조회합니다."
    )
    public ResponseEntity<RsData<InquiryListResponse>> getPublicInquiries(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        log.info("공개 문의 목록 조회 (비로그인) - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponse response = inquiryService.getPublicInquiries(pageable);

        return ResponseEntity.ok(
                RsData.of("200", "공개 문의 목록 조회 성공", response)
        );
    }

    /**
     * 공개 문의 + 내 문의 목록 조회 (로그인)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "문의 목록 조회 (로그인)",
            description = "공개 문의 + 본인의 비밀문의를 조회합니다."
    )
    public ResponseEntity<RsData<InquiryListResponse>> getInquiries(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("문의 목록 조회 (로그인) - userId: {}, page: {}", userDetails.getUserId(), page);

        User user = getUserFromDetails(userDetails);
        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponse response = inquiryService.getPublicInquiriesOrMine(user, pageable);

        return ResponseEntity.ok(
                RsData.of("200", "문의 목록 조회 성공", response)
        );
    }

    /**
     * 내 문의만 조회
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "내 문의 목록 조회",
            description = "본인이 작성한 문의만 조회합니다."
    )
    public ResponseEntity<RsData<InquiryListResponse>> getMyInquiries(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("내 문의 목록 조회 - userId: {}, page: {}", userDetails.getUserId(), page);

        User user = getUserFromDetails(userDetails);
        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponse response = inquiryService.getMyInquiries(user, pageable);

        return ResponseEntity.ok(
                RsData.of("200", "내 문의 목록 조회 성공", response)
        );
    }

    /**
     * 전체 문의 목록 조회 (관리자 전용)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "전체 문의 목록 조회 (관리자 전용)",
            description = "비밀문의를 포함한 모든 문의를 조회합니다."
    )
    public ResponseEntity<RsData<InquiryListResponse>> getAllInquiriesForAdmin(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails adminDetails) {

        log.info("전체 문의 목록 조회 (관리자) - adminId: {}, page: {}", adminDetails.getUserId(), page);

        Pageable pageable = PageRequest.of(page - 1, size);
        InquiryListResponse response = inquiryService.getAllInquiriesForAdmin(pageable);

        return ResponseEntity.ok(
                RsData.of("200", "전체 문의 목록 조회 성공", response)
        );
    }

    // ========================================
    // 문의 CRUD
    // ========================================
    /**
     * 문의 상세 조회
     */
    @GetMapping("/{inquiryId}")
    @Operation(
            summary = "문의 상세 조회",
            description = "문의 상세 정보를 조회합니다. 비밀문의는 작성자와 관리자만 조회 가능합니다."
    )
    public ResponseEntity<RsData<InquiryDetailResponse>> getInquiry(
            @PathVariable Long inquiryId,
            @Parameter(hidden = true)
            Authentication authentication) {

        log.info("문의 상세 조회 - inquiryId: {}", inquiryId);

        Long currentUserId = null;
        boolean isAdmin = false;

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            currentUserId = userDetails.getUserId();
            isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }

        InquiryDetailResponse response = inquiryService.getInquiry(inquiryId, currentUserId, isAdmin);

        return ResponseEntity.ok(
                RsData.of("200", "문의 상세 조회 성공", response)
        );
    }

    /**
     * 문의 작성
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "문의 작성",
            description = "새로운 문의를 작성합니다. 첨부파일은 최대 3개까지 업로드 가능합니다."
    )
    public ResponseEntity<RsData<Long>> createInquiry(
            @Valid @ModelAttribute InquiryCreateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("문의 작성 요청 - userId: {}, category: {}", userDetails.getUserId(), request.category());

        User user = getUserFromDetails(userDetails);
        Long inquiryId = inquiryService.createInquiry(request, user);

        return ResponseEntity.ok(
                RsData.of("200", "문의가 성공적으로 등록되었습니다.", inquiryId)
        );
    }

    /**
     * 문의 수정
     */
    @PutMapping("/{inquiryId}")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "문의 수정",
            description = "문의를 수정합니다. 본인이 작성한 문의만 수정 가능합니다."
    )
    public ResponseEntity<RsData<Void>> updateInquiry(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId,

            @Valid @ModelAttribute InquiryUpdateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("문의 수정 요청 - userId: {}, inquiryId: {}", userDetails.getUserId(), inquiryId);

        inquiryService.updateInquiry(inquiryId, request, userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "문의가 성공적으로 수정되었습니다.")
        );
    }

    /**
     * 문의 삭제
     */
    @DeleteMapping("/{inquiryId}")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "문의 삭제",
            description = "문의를 삭제합니다. 본인이 작성한 문의 또는 관리자만 삭제 가능합니다."
    )
    public ResponseEntity<RsData<Void>> deleteInquiry(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("문의 삭제 요청 - userId: {}, inquiryId: {}", userDetails.getUserId(), inquiryId);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        inquiryService.deleteInquiry(inquiryId, userDetails.getUserId(), isAdmin);

        return ResponseEntity.ok(
                RsData.of("200", "문의가 삭제되었습니다.")
        );
    }

    // ========================================
    // 댓글 CRUD
    // ========================================

    /**
     * 댓글 작성
     */
    @PostMapping("/{inquiryId}/replies")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "댓글 작성",
            description = "문의에 댓글을 작성합니다. 관리자 답변 시 문의 상태가 '답변완료'로 변경됩니다."
    )
    public ResponseEntity<RsData<Long>> createReply(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId,

            @Valid @RequestBody InquiryReplyRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("댓글 작성 요청 - userId: {}, inquiryId: {}", userDetails.getUserId(), inquiryId);

        User user = getUserFromDetails(userDetails);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Long replyId = inquiryService.createReply(inquiryId, request, user, isAdmin);

        return ResponseEntity.ok(
                RsData.of("200", "댓글이 성공적으로 등록되었습니다.", replyId)
        );
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{inquiryId}/replies/{replyId}")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "댓글 수정",
            description = "댓글을 수정합니다. 본인이 작성한 댓글만 수정 가능합니다."
    )
    public ResponseEntity<RsData<Void>> updateReply(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId,

            @Parameter(description = "댓글 ID", example = "1", required = true)
            @PathVariable Long replyId,

            @Valid @RequestBody InquiryReplyRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("댓글 수정 요청 - userId: {}, replyId: {}", userDetails.getUserId(), replyId);

        inquiryService.updateReply(replyId, request, userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "댓글이 성공적으로 수정되었습니다.")
        );
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{inquiryId}/replies/{replyId}")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다. 본인이 작성한 댓글 또는 관리자만 삭제 가능합니다."
    )
    public ResponseEntity<RsData<Void>> deleteReply(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId,

            @Parameter(description = "댓글 ID", example = "1", required = true)
            @PathVariable Long replyId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("댓글 삭제 요청 - userId: {}, replyId: {}", userDetails.getUserId(), replyId);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        inquiryService.deleteReply(replyId, userDetails.getUserId(), isAdmin);

        return ResponseEntity.ok(
                RsData.of("200", "댓글이 삭제되었습니다.")
        );
    }

    // ===== 헬퍼 메서드 =====

    /**
     * CustomUserDetails에서 User 엔티티 조회
     */
    private User getUserFromDetails(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));
    }

}
