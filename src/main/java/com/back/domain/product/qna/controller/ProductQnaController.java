package com.back.domain.product.qna.controller;

import com.back.domain.product.qna.dto.request.ProductQnaRequestDto;
import com.back.domain.product.qna.dto.response.ProductQnaListResponseDto;
import com.back.domain.product.qna.dto.response.ProductQnaResponseDto;
import com.back.domain.product.qna.service.ProductQnaService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/qna/{productUuid}")
@RequiredArgsConstructor
@Tag(name = "상품 Q&A", description = "상품 Q&A 관련 API")
public class ProductQnaController {

    private final ProductQnaService productQnaService;

    /** 상품 Q&A 등록 */
    @PostMapping
    @Operation(
            summary = "상품 Q&A 등록",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 Q&A 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "상품 Q&A가 성공적으로 등록되었습니다.",
                                                      "data": "550e8400-e29b-41d4-a716-446655440000"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (필수값 누락, 존재하지 않는 상품 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "Q&A 카테고리는 필수입니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "403",
                                                      "msg": "상품 Q&A 등록 권한이 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<UUID>> createProductQna(
            @PathVariable UUID productUuid,
            @Valid @RequestBody ProductQnaRequestDto request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UUID createdProductUuid = productQnaService.createProductQna(productUuid, request, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품 Q&A가 성공적으로 등록되었습니다.", createdProductUuid));
    }

    /** 상품 Q&A 상세 조회 */
    @GetMapping("/{productQnaId}")
    @Operation(
            summary = "상품 Q&A 상세 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 Q&A 상세 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductQnaResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 상품 Q&A를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "404",
                                                      "msg": "해당 상품 Q&A를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<ProductQnaResponseDto>> getProductQnaDetail(
            @PathVariable Long productQnaId) {
        ProductQnaResponseDto responseDto = productQnaService.getProductQnaDetail(productQnaId);
        return ResponseEntity.ok(RsData.of("200", "상품 Q&A 상세 조회 성공", responseDto));
    }

    /** 상품 Q&A 목록 조회 (페이지네이션) */
    @GetMapping("/list")
    @Operation(
            summary = "상품 Q&A 목록 조회 (페이지네이션)",
            parameters = {
                    @Parameter(name = "qnaCategory", description = "Q&A 카테고리 (예: 배송, 상품, 교환/환불, 기타. '전체' 또는 미지정 시 전체 카테고리 조회)", example = "배송"),
                    @Parameter(name = "page", description = "페이지 번호", example = "1"),
                    @Parameter(name = "size", description = "페이지당 항목 수", example = "10")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 Q&A 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductQnaListResponseDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<RsData<ProductQnaListResponseDto>> getProductQnaList(
            @PathVariable UUID productUuid,
            @RequestParam(value = "qnaCategory", required = false) String qnaCategory,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        ProductQnaListResponseDto responseDto = productQnaService.getProductQnaList(productUuid, qnaCategory, page, size);
        return ResponseEntity.ok(RsData.of("200", "상품 Q&A 목록 조회 성공", responseDto));
    }
}
