package com.back.domain.artist.controller;

import com.back.domain.artist.dto.request.ArtistApplicationRequest;
import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.dto.response.ArtistBusinessInfoResponse;
import com.back.domain.artist.service.ArtistApplicationService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/artist")
@RequiredArgsConstructor
@Tag(name = "작가", description = "작가 관련 API")
public class ArtistController {

    private final ArtistApplicationService artistApplicationService;

    /**
     * 작가 신청
     */
    @PostMapping("/application")
    @Operation(summary = "작가 신청", description = "사용자가 작가로 신청합니다.")
    public ResponseEntity<RsData<Long>> applyForArtist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ArtistApplicationRequest request) {

        log.info("작가 신청 - userId: {}", userDetails.getUserId());

        Long applicationId = artistApplicationService.createApplication(userDetails.getUserId(), request);

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
    @Operation(summary="사업자 정보 조회", description="작가 본인의 사업자 관련 정보를 불러오기로 확인합니다.")
    public ResponseEntity<RsData<ArtistBusinessInfoResponse>> getBusinessInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("사업자 정보 조회 - userId: {}", userDetails.getUserId());
        ArtistBusinessInfoResponse response = artistApplicationService.getBusinessInfo(userDetails.getUserId());
        return ResponseEntity.ok(RsData.of("200", "사업자 정보 조회 성공", response));
    }
}
