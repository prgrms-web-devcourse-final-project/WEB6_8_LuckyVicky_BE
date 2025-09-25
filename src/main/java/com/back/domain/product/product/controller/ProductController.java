package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.CreateProductRequest;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "상품 등록")
    public ResponseEntity<RsData<Long>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long productId = productService.createProduct(request, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품이 성공적으로 등록되었습니다.", productId));
    }
}
