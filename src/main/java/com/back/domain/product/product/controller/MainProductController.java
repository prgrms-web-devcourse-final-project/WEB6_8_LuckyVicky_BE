package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품", description = "메인페이지에서 상품 조회 API")
public class MainProductController {
    private final ProductService productService;

    /** 신상품 조회 API */
    @GetMapping("/new")
    @Operation(
            summary = "14일 이내 등록된 신상품 전체 조회",
            description = "최근 14일 이내 등록된 상품을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "신상품 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "products": [
                                                        {
                                                          "productUuid": "550e8400-e29b-41d4-a716-446655440000",
                                                          "url": "https://bucket.s3.amazonaws.com/product-images/uuid1.png",
                                                          "brandName": "브랜드1",
                                                          "name": "상품1",
                                                          "price": 10000,
                                                          "discountRate": 10,
                                                          "discountPrice": 9000,
                                                          "rating": 4.5
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getNewProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getAllNewProducts();
        return ResponseEntity.ok(RsData.of("200", "신상품 조회 성공", products));
    }

}
