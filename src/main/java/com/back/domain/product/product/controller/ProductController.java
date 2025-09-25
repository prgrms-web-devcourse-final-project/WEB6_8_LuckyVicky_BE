package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.CreateProductRequest;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.S3ValidationService;
import com.back.global.s3.UploadResultResponse;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;
    private final S3ValidationService s3ValidationService;

    @PostMapping
    @Operation(
            summary = "상품 등록",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "상품이 성공적으로 등록되었습니다.",
                                                      "data": 1
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (필수값 누락 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "상품명은 필수입니다.",
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
                                                      "msg": "상품 등록 권한이 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<Long>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long productId = productService.createProduct(request, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품이 성공적으로 등록되었습니다.", productId));
    }


    @PostMapping("/images")
    @Operation(
            summary = "상품 이미지 업로드",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "파일과 파일타입을 multipart/form-data 형식으로 전송",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(
                                    type = "object",
                                    example = """
{
  "files": ["image1.jpg", "image2.jpg"],
  "types": ["MAIN", "THUMBNAIL"]
}
"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이미지 업로드 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "이미지 업로드 성공",
                                                      "data": [
                                                        {
                                                          "url": "https://bucket.s3.amazonaws.com/product-images/uuid1.png",
                                                          "type": "MAIN",
                                                          "s3Key": "product-images/uuid1.png",
                                                          "originalFileName": "example.png"
                                                        },
                                                        {
                                                          "url": "https://bucket.s3.amazonaws.com/product-images/uuid2.png",
                                                          "type": "THUMBNAIL",
                                                          "s3Key": "product-images/thumbnail-uuid2.png",
                                                          "originalFileName": "thumb_example.png"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (파일 누락, files/types 개수 불일치 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "업로드할 파일이 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<UploadResultResponse>>> uploadProductImages(
            @RequestPart List<MultipartFile> files,
            @RequestParam List<FileType> types) {
        List<UploadResultResponse> uploaded = s3Service.uploadFiles(files,"product-images", types);
        return ResponseEntity.ok(RsData.of("200","이미지 업로드 성공",uploaded));
    }


    @GetMapping("/images/download/{productId}")
    @Operation(
            summary = "상품 문서 다운로드 (테스트용)",
            description = "DOCUMENT 타입의 문서 파일을 다운로드. 브라우저는 Content-Disposition 헤더를 보고 파일을 다운로드 처리합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "문서 다운로드 성공",
                            content = @Content(
                                    mediaType = "application/octet-stream",
                                    schema = @Schema(
                                            type = "string",
                                            format = "binary",
                                            example = "example-document.pdf"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "문서가 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                {
                                                  "resultCode": "404",
                                                  "msg": "다운로드할 문서가 존재하지 않습니다.",
                                                  "data": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<byte[]> downloadProductDocument(@PathVariable Long productId) {
        ProductImage document = productService.getProductDocument(productId);
        // s3에서 파일 다운로드
        byte[] fileBytes = s3Service.downloadFile(document.getS3Key());
        //원본 파일명으로 응담
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getOriginalFilename() + "\"") // 브라우저가 파일 다운로드 처리하게함
                .header("Content-Type", "application/octet-stream") // 바이너리 데이터임을 나타냄
                .body(fileBytes); // 실제 파일 바이트
    }
}
