package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingCreateRequest;
import com.back.domain.funding.dto.request.FundingUpdateRequest;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.dto.response.FundingCreateResponse;
import com.back.domain.funding.dto.response.FundingDetailResponse;
import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingImage;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.service.FundingService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
@Tag(name = "펀딩", description = "펀딩 API 컨트롤러")
public class FundingController {

    private final FundingService fundingService;
    private final S3Service s3Service;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(summary = "펀딩 생성",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "기본 예시",
                                            value = """
                    {
                      "title": "한정판 키링 펀딩",
                      "description": "한정판 키링입니다.",
                      "categoryId": 1,
                      "imageUrl": "https://test.jpg",
                      "targetAmount": 500000,
                      "price": 30000,
                      "stock": 200,
                      "startDate": "2025-11-01 00:00:00",
                      "endDate": "2025-12-15 23:59:59",
                      "images": [
                          {
                              "url": "https://test.jpg",
                              "type": "MAIN",
                              "s3Key": "funding-images/test.jpg",
                              "originalFileName": "test.JPG"
                          }
                        ]
                    }
                    """
                                    )
                            }
                    )
            )
    )    public ResponseEntity<RsData<FundingCreateResponse>> createFunding(
            @Valid @RequestBody FundingCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String userEmail) {

        Funding funding = fundingService.createFunding(request, userEmail);

        FundingCreateResponse response = FundingCreateResponse.from(funding);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RsData<>("201", "펀딩이 생성되었습니다.", response));
    }

    @PostMapping("/images")
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(
            summary = "펀딩 이미지 업로드",
            description = "펀딩에 사용될 이미지를 업로드합니다. " +
                    "files -> 업로드할 파일 리스트. 이미지(jpg, png 등), 문서(pdf, doc 등)<br>" +
                    "types -> 업로드할 파일 타입. 대표 이미지(MAIN), 추가 이미지(ADDITIONAL), 썸네일(THUMBNAIL), 문서(DOCUMENT)"
    ) public ResponseEntity<RsData<List<UploadResultResponse>>> uploadFundingImages(
            @RequestPart List<MultipartFile> files,
            @Parameter(hidden = true)
            @RequestParam List<FileType> types) {
        List<UploadResultResponse> uploaded = s3Service.uploadFiles(files, "funding-images", types);
        return ResponseEntity.ok(RsData.of("200", "이미지 업로드 성공", uploaded));
    }

    /** 펀딩 이미지 개별 삭제 (S3) */
    @DeleteMapping("/images")
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(
            summary = "S3 펀딩 이미지 개별 삭제",
            description = "s3Key를 사용하여 S3에 업로드된 펀딩 이미지를 삭제합니다. " +
                    "펀딩 등록/수정 중 사용자가 업로드한 이미지를 다시 삭제할 때 사용됩니다."
    )
    public ResponseEntity<RsData<String>> deleteFundingImage(
            @Parameter(description = "삭제할 파일의 s3Key", required = true)
            @RequestParam String s3Key) {

        s3Service.deleteFile(s3Key);
        return ResponseEntity.ok(RsData.of("200", "파일이 성공적으로 삭제되었습니다.", s3Key));
    }

    /** 펀딩 문서 다운로드 */
    @GetMapping("/images/download/{id}")
    @Operation(
            summary = "펀딩 문서 다운로드",
            description = "DOCUMENT 타입의 문서 파일 다운로드.<br>" +
                    "브라우저는 Content-Disposition 헤더를 보고 파일 다운로드 처리합니다."
    )
    public ResponseEntity<byte[]> downloadFundingDocument(
            @Parameter(description = "펀딩 ID", required = true)
            @PathVariable @Positive Long id) {

        // 펀딩 ID로 DOCUMENT 타입 이미지 조회
        FundingImage document = fundingService.getFundingDocument(id);
        // S3에서 파일 다운로드
        byte[] fileBytes = s3Service.downloadFile(document.getS3Key());

        // 원본 파일명으로 응답
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(fileBytes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "펀딩 상세 조회")
    public ResponseEntity<RsData<FundingDetailResponse>> getFunding(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(new RsData<>("200", "%d번 펀딩 조회 성공".formatted(id), fundingService.getFunding(id)));
    }

    @GetMapping
    @Operation(summary = "펀딩 목록 조회")
    public ResponseEntity<RsData<Page<FundingCardDto>>> getFundingList(
            @Parameter(description = "[필터링] 펀딩 상태 목록. 심사중(PENDING), 승인됨(APPROVED), 거절됨(REJECTED), 진행중(OPEN), 종료(CLOSED), 성공(SUCCESS), 실패(FAILED), 취소(CANCELED)",
                    example = "OPEN,CLOSED")
            @RequestParam(required = false) Set<FundingStatus> status,

            @Parameter(description = "[정렬] 정렬 기준. 인기순(popular), 최신순(recent), 마감임박(deadline), 목표금액높은순(highAmount)",
                    example = "popular")
            @RequestParam(defaultValue = "recent") String sortBy,

            @Parameter(description = "[필터링] 펀딩 제목 검색어 (부분 일치, 대소문자 무시)",
                    example = "키링")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "[필터링] 카테고리 ID (상위 카테고리)",
                    example = "1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "[필터링] 최소 가격 (원 단위)",
                    example = "10000")
            @RequestParam(required = false) Long minPrice,

            @Parameter(description = "[필터링] 최대 가격 (원 단위)",
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
                status, sortBy, keyword, categoryId, minPrice, maxPrice, page, size
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

    @DeleteMapping("/{id}")
    @Operation(summary = "펀딩 삭제", description = "펀딩을 삭제합니다. 심사 중이거나 거절, 취소 상태에서만 삭제할 수 있습니다.")
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    public ResponseEntity<RsData<Void>> deleteFunding(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail) {
        fundingService.deleteFunding(id, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "펀딩이 삭제되었습니다.", null));
    }
}