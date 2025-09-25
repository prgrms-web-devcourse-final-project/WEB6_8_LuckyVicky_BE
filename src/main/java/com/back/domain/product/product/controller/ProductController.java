package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.CreateProductRequest;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.S3ValidationService;
import com.back.global.s3.UploadResultResponse;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;
    private final S3ValidationService s3ValidationService;

    @PostMapping
    @Operation(summary = "상품 등록")
    public ResponseEntity<RsData<Long>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long productId = productService.createProduct(request, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품이 성공적으로 등록되었습니다.", productId));
    }

    @PostMapping("/images")
    @Operation(summary = "상품 이미지 업로드")
    public ResponseEntity<RsData<List<UploadResultResponse>>> uploadProductImages(
            @RequestPart List<MultipartFile> files,
            @RequestParam List<FileType> types) {
        List<UploadResultResponse> uploaded = s3Service.uploadFiles(files,"product-images", types);
        return ResponseEntity.ok(RsData.of("200","이미지 업로드 성공",uploaded));
    }

}
