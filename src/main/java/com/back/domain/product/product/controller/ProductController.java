package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.CreateProductRequestDto;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "ProductController", description = "상품 CRUD 작업 컨트롤러")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "새로운 상품을 등록합니다.")
    public ResponseEntity<RsData<?>> createProduct(@RequestBody CreateProductRequestDto createProductRequestdto){

    }
}
