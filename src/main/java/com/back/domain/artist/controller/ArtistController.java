package com.back.domain.artist.controller;

import com.back.domain.artist.dto.request.ArtistApplicationRequest;
import com.back.domain.artist.dto.response.*;
import com.back.domain.artist.service.ArtistApplicationService;
import com.back.domain.artist.service.ArtistPublicService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/artist")
@RequiredArgsConstructor
@Tag(name = "작가", description = "작가 관련 API")
public class ArtistController {

    private final ArtistApplicationService artistApplicationService;
    private final ArtistPublicService artistPublicService;

    // ========================================
    // 공개 API
    // ========================================

    /**
     * 전체 작가 목록 조회
     */
    @GetMapping("/list")
    @Operation(
            summary = "전체 작가 목록 조회",
            description = "등록된 모든 작가의 ID와 이름을 조회합니다."
    )
    public ResponseEntity<RsData<List<ArtistListResponse>>> getArtistList() {

        log.info("작가 목록 조회");

        List<ArtistListResponse> response = artistPublicService.getArtistList();

        return ResponseEntity.ok(
                RsData.of("200", "작가 목록 조회 성공", response)
        );
    }

    /**
     * 작가 공개 프로필 상세 조회
     */
    @GetMapping("/profile/{artistId}")
    @Operation(
            summary = "작가 공개 프로필 상세 조회",
            description = "작가의 공개 프로필 상세 정보를 조회합니다. 로그인한 경우 팔로우 여부가 포함됩니다."
    )
    public ResponseEntity<RsData<ArtistPublicProfileResponse>> getArtistPublicProfile(
            @Parameter(description = "작가 ID", example = "42", required = true)
            @PathVariable Long artistId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ArtistPublicProfileResponse response;
        
        if (userDetails != null) {
            // 로그인한 사용자 - 팔로우 상태 포함
            log.info("작가 공개 프로필 조회 (로그인) - artistId: {}, userId: {}", artistId, userDetails.getUserId());
            response = artistPublicService.getPublicProfile(artistId, userDetails.getUserId());
        } else {
            // 비로그인 사용자 - 팔로우 상태 미포함
            log.info("작가 공개 프로필 조회 (비로그인) - artistId: {}", artistId);
            response = artistPublicService.getPublicProfile(artistId);
        }

        return ResponseEntity.ok(
                RsData.of("200", "작가 프로필 조회 성공", response)
        );
    }

    /**
     * 작가의 상품 목록 조회
     */
    @GetMapping("/profile/{artistId}/products")
    @Operation(
            summary = "작가의 상품 목록 조회",
            description = "특정 작가가 등록한 모든 상품을 조회합니다."
    )
    public ResponseEntity<RsData<List<ArtistProductResponse>>> getArtistProducts(
            @Parameter(description = "작가 프로필 ID", example = "42", required = true)
            @PathVariable Long artistId) {

        log.info("작가 상품 목록 조회 - artistId: {}", artistId);

        List<ArtistProductResponse> response = artistPublicService.getArtistProducts(artistId);

        return ResponseEntity.ok(
                RsData.of("200", "작가 상품 목록 조회 성공", response)
        );
    }


    // ========================================
    // 인증 필요 API
    // ========================================

    /**
     * 작가 신청
     */
    @PostMapping(value = "/application", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "작가 신청",
            description = "사용자가 작가 입점을 신청합니다. 필수 서류를 함께 업로드합니다."
    )
    public ResponseEntity<RsData<Long>> applyForArtist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("application") ArtistApplicationRequest request,
            @RequestPart(value = "documents", required = true) List<MultipartFile> documents) {

        log.info("작가 신청 - userId: {}, 서류 개수: {}",
                userDetails.getUserId(), documents.size());

        Long applicationId = artistApplicationService.createApplication(
                userDetails.getUserId(),
                request,
                documents
        );

        return ResponseEntity.ok(
                RsData.of("200", "작가 신청 완료", applicationId)
        );
    }

    /**
     * 내 작가 신청 목록 조회
     */
    @GetMapping("/application/me")
    @Operation(summary = "내 작가 신청 목록 조회", description = "본인의 작가 신청서 목록을 조회합니다.")
    public ResponseEntity<RsData<List<ArtistApplicationSimpleResponse>>> getMyArtistApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("내 작가 신청 목록 조회 - userId: {}", userDetails.getUserId());

        List<ArtistApplicationSimpleResponse> response = artistApplicationService.getMyApplications(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "내 작가 신청 목록 조회 성공", response)
        );
    }

    /**
     * 작가 신청 상세 조회
     */
    @GetMapping("/application/{applicationId}")
    @Operation(summary = "작가 신청 상세 조회", description = "특정 작가 신청서의 상세 정보를 조회합니다.")
    public ResponseEntity<RsData<ArtistApplicationResponse>> getArtistApplicationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId) {

        log.info("작가 신청 상세 조회 - userId: {}, applicationId: {}",
                userDetails.getUserId(), applicationId);

        ArtistApplicationResponse response =
                artistApplicationService.getApplicationById(userDetails.getUserId(), applicationId);

        return ResponseEntity.ok(
                RsData.of("200", "작가 신청 상세 조회 성공", response)
        );
    }

    /**
     * 작가 신청 취소/삭제
     */
    @DeleteMapping("/application/{applicationId}/cancel")
    @Operation(summary = "작가 신청 취소", description = "작가 신청을 취소하거나 삭제합니다. 승인된 신청서는 취소할 수 없습니다.")
    public ResponseEntity<RsData<Void>> cancelArtistApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId) {
        log.info("작가 신청 취소 - userId: {}, applicationId: {}",
                userDetails.getUserId(), applicationId);

        artistApplicationService.cancelApplication(userDetails.getUserId(), applicationId);

        return ResponseEntity.ok(
                RsData.of("200", "작가 신청 취소 성공")
        );
    }

    /**
     * 작가 사업자 관련 정보만 조회 (상품 등록 시 불러오기 기능)
     */
    @GetMapping("/business-info")
    @Operation(summary="상품 등록 시 사업자 정보 조회", description="상품 등록 시, 작가 본인의 사업자 관련 정보를 불러오기로 확인합니다.")
    public ResponseEntity<RsData<ArtistBusinessInfoResponse>> getBusinessInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("사업자 정보 조회 - userId: {}", userDetails.getUserId());
        ArtistBusinessInfoResponse response = artistApplicationService.getBusinessInfo(userDetails.getUserId());
        return ResponseEntity.ok(RsData.of("200", "사업자 정보 조회 성공", response));
    }
}
