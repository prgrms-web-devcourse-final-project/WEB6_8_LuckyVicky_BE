package com.back.domain.funding.controller;

import com.back.domain.funding.dto.request.FundingNewsCreateRequest;
import com.back.domain.funding.service.FundingNewsService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fundings")
@RequiredArgsConstructor
@Tag(name = "펀딩 새소식", description = "펀딩 새소식 API 컨트롤러")
public class FundingNewsController{
    private final FundingNewsService fundingNewsService;
    private final S3Service s3Service;

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

    @PostMapping(value = "/{id}/news/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(
            summary = "펀딩 새소식 이미지 업로드",
            description = "펀딩 새소식에 사용할 이미지 업로드. 한 장만 업로드 가능"
    )
    public ResponseEntity<RsData<List<UploadResultResponse>>> uploadNewsImage(
            @PathVariable @Positive Long id,
            @Parameter(description = "업로드 할 이미지 파일", required = true)
            @RequestPart("file")MultipartFile file) {
        List<UploadResultResponse> result = s3Service.uploadFile(file, "funding-news-images", FileType.ADDITIONAL);
        return ResponseEntity.ok(RsData.of("200", "이미지 업로드 성공", result));
    }

    @DeleteMapping("/{id}/news/images")
    @PreAuthorize("hasAuthority('ROLE_ARTIST') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_ROOT')")
    @Operation(
            summary = "펀딩 새소식 이미지 삭제",
            description = "S3에 업로드된 이미지 삭제"
    )
    public ResponseEntity<RsData<String>> deleteNewsImages(
            @PathVariable @Positive Long id,
            @Parameter(description = "삭제할 파일의 s3Key", required = true)
            @RequestParam String s3Key) {
        s3Service.deleteFile(s3Key);
        return ResponseEntity.ok(RsData.of("200", "이미지가 성공적으로 삭제되었습니다.", s3Key));
    }

    @DeleteMapping("/{fundingId}/news/{newsId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "펀딩 새소식 삭제", description = "펀딩 새소식을 삭제")
    public ResponseEntity<RsData<?>> deleteNews(
            @PathVariable @Positive Long fundingId,
            @PathVariable @Positive Long newsId,
            @AuthenticationPrincipal(expression = "username") String userEmail
    ) {
        fundingNewsService.deleteFundingNews(fundingId, newsId, userEmail);
        return ResponseEntity.ok(new RsData<>("200", "새소식이 삭제되었습니다."));
    }
}

