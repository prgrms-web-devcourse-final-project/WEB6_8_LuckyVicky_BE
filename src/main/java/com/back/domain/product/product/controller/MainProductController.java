package com.back.domain.product.product.controller;

import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.service.ProductService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "상품", description = "메인페이지에서 주제별 상품 조회 API")
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
                                            example = "{ \"code\": \"200\", \"message\": \"신상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 10,\"discountPrice\": 9000,\"rating\": 4.5}]}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "신상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"신상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getNewProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getAllNewProducts();
        return ResponseEntity.ok(RsData.of("200", "신상품 조회 성공", products));
    }

    /** 할인중 상품 조회 */
    @GetMapping("/onsale")
    @Operation(
            summary = "할인 중인 상품 전체 조회",
            description = "할인율이 0보다 큰 상품을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "할인 상품 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"200\", \"message\": \"할인 상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 10,\"discountPrice\": 9000,\"rating\": 4.5}]} "
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "할인 상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"할인 상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getOnSaleProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getOnSaleProducts();
        return ResponseEntity.ok(RsData.of("200", "할인 상품 조회 성공", products));
    }

    /** 품절 임박 상품 조회 */
    @GetMapping("/low-stock")
    @Operation(
            summary = "재고 5개 이하 상품 전체 조회",
            description = "재고가 5개 이하인 상품을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"200\", \"message\": \"품절 임박 상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 0,\"discountPrice\": 10000,\"rating\": 4.5}]} "
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "품절 임박 상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"품절 임박 상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getLowStockProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getLowStockProducts();
        return ResponseEntity.ok(RsData.of("200", "품절 임박 상품 조회 성공", products));
    }

    /** 재입고 상품 조회 */
    @GetMapping("/restock")
    @Operation(
            summary = "재입고 상품 전체 조회",
            description = "재입고 상품(isRestock=true)만 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"200\", \"message\": \"재입고 상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 0,\"discountPrice\": 10000,\"rating\": 4.5}]} "
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "재입고 상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"재입고 상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getRestockProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getRestockProducts();
        return ResponseEntity.ok(RsData.of("200", "재입고 상품 조회 성공", products));
    }

    /** 기획 상품 조회 */
    @GetMapping("/planned")
    @Operation(
            summary = "기획 상품 전체 조회",
            description = "기획 상품(isPlanned=true)만 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"200\", \"message\": \"기획 상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 0,\"discountPrice\": 10000,\"rating\": 4.5}]} "
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "기획 상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"기획 상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getPlannedProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getPlannedProducts();
        return ResponseEntity.ok(RsData.of("200", "기획 상품 조회 성공", products));
    }

    /** 오픈 예정 상품 조회 */
    @GetMapping("/upcoming")
    @Operation(
            summary = "오픈 예정 상품 전체 조회",
            description = "판매 시작일(sellingStartDate)이 오늘 이후인 상품만 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"200\", \"message\": \"오픈 예정 상품 조회 성공\", \"data\": [{\"productUuid\": \"uuid\",\"url\": \"https://example.com/img.png\",\"brandName\": \"브랜드\",\"name\": \"상품\",\"price\": 10000,\"discountRate\": 0,\"discountPrice\": 10000,\"rating\": 4.5}]} "
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "오픈 예정 상품이 존재하지 않음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = "{ \"code\": \"404\", \"message\": \"오픈 예정 상품이 존재하지 않습니다.\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<ProductListResponse.ProductInfo>>> getUpcomingProducts() {
        List<ProductListResponse.ProductInfo> products = productService.getUpcomingProducts();
        return ResponseEntity.ok(RsData.of("200", "오픈 예정 상품 조회 성공", products));
    }

}
