package com.back.domain.review.controller;

import com.back.domain.review.dto.request.ReviewCreateRequestDto;
import com.back.domain.review.dto.request.ReviewListRequestDto;
import com.back.domain.review.dto.request.ReviewUpdateRequestDto;
import com.back.domain.review.dto.request.ReviewWriteRequestDto;
import com.back.domain.review.dto.response.ReviewDetailResponseDto;
import com.back.domain.review.dto.response.ReviewListResponseDto;
import com.back.domain.review.dto.response.ReviewResponseDto;
import com.back.domain.review.dto.response.ReviewStatsResponseDto;
import com.back.domain.review.service.ReviewService;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 리뷰 Controller
 */
@Tag(name = "리뷰", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    /**
     * 리뷰 이미지 업로드
     */
    @Operation(summary = "리뷰 이미지 업로드", description = "리뷰 작성 전에 이미지를 S3에 업로드합니다.")
    @PostMapping("/images/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RsData<List<UploadResultResponse>>> uploadReviewImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("types") List<FileType> types) {

        log.info("리뷰 이미지 업로드 요청 - 파일 개수: {}, 타입 개수: {}", files.size(), types.size());

        List<UploadResultResponse> uploadResults = s3Service.uploadFiles(files, "review-images", types);
        
        return ResponseEntity.ok(RsData.of("200", "리뷰 이미지 업로드 성공", uploadResults));
    }

    /**
     * 리뷰 작성
     */
    @Operation(summary = "리뷰 작성", description = "상품에 대한 리뷰를 작성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewCreateRequestDto requestDto,
            @AuthenticationPrincipal User user) {

        ReviewResponseDto response = reviewService.createReview(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 목록 조회
     */
    @Operation(summary = "리뷰 목록 조회", description = "상품의 리뷰 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ReviewListResponseDto> getReviewList(
            @Parameter(description = "상품 UUID") @RequestParam UUID productUuid,
            @Parameter(description = "리뷰 타입 (PHOTO, GENERAL, ALL)") @RequestParam(required = false) ReviewListRequestDto.ReviewType reviewType,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "10") Integer size,
            @AuthenticationPrincipal User user) {

        ReviewListRequestDto requestDto = ReviewListRequestDto.builder()
                .productUuid(productUuid)
                .reviewType(reviewType != null ? reviewType : ReviewListRequestDto.ReviewType.ALL)
                .page(page)
                .size(size)
                .build();

        ReviewListResponseDto response = reviewService.getReviewList(requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 상세 조회
     */
    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewDetail(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        ReviewResponseDto response = reviewService.getReviewDetail(reviewId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 수정
     */
    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal User user) {

        ReviewResponseDto response = reviewService.updateReview(reviewId, requestDto, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제
     */
    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * 리뷰 좋아요 토글
     */
    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/{reviewId}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> toggleReviewLike(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        boolean isLiked = reviewService.toggleReviewLike(reviewId, user);
        return ResponseEntity.ok(isLiked);
    }

    /**
     * 리뷰 상세 팝업 조회 (이미지 확대보기용)
     */
    @Operation(summary = "리뷰 상세 팝업 조회", description = "리뷰 클릭 시 상세 팝업에 표시할 정보를 조회합니다.")
    @GetMapping("/{reviewId}/popup")
    public ResponseEntity<ReviewDetailResponseDto> getReviewDetailPopup(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        ReviewDetailResponseDto response = reviewService.getReviewDetailForPopup(reviewId, user);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 작성 팝업용 작성
     */
    @Operation(
        summary = "리뷰 작성 팝업", 
        description = "리뷰 작성 팝업을 처리합니다. 별점, 텍스트, 이미지 업로드, 해시태그, 상품옵션 정보를 포함합니다."
    )
    @PostMapping("/write")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDetailResponseDto> writeReviewFromPopup(
            @Valid @RequestBody ReviewWriteRequestDto requestDto,
            @AuthenticationPrincipal User user) {

        log.info("리뷰 작성 팝업 API 호출 - 사용자: {}, 상품UUID: {}, 평점: {}, 상품옵션: {}", 
                user.getId(), requestDto.getProductUuid(), requestDto.getRating(), requestDto.getProductOption());

        ReviewDetailResponseDto response = reviewService.writeReviewFromPopup(requestDto, user);
        
        log.info("리뷰 작성 팝업 완료 - 리뷰ID: {}, 포토리뷰여부: {}", 
                response.getReviewId(), response.getIsPhotoReview());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 상품별 리뷰 통계 조회
     */
    @Operation(summary = "상품별 리뷰 통계 조회", description = "특정 상품의 리뷰 통계 정보를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsResponseDto> getReviewStats(
            @Parameter(description = "상품 UUID") @RequestParam UUID productUuid) {

        Product product = productRepository.findByProductUuid(productUuid)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        ReviewStatsResponseDto response = reviewService.getReviewStats(product);
        return ResponseEntity.ok(response);
    }
}
