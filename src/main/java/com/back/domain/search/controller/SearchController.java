package com.back.domain.search.controller;

import com.back.domain.search.dto.SearchResponseDto;
import com.back.domain.search.service.SearchService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "통합 검색", description = "상품, 작가, 펀딩 통합 검색 API")
public class SearchController {

    private final SearchService searchService;

    /** 통합 검색 */
    @GetMapping
    @Operation(
            summary = "키워드 통합 검색",
            description = "키워드를 기반으로 통합 검색(상품, 작가, 펀딩)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "검색 성공",
                                                      "data": {
                                                        "products": [
                                                          {
                                                            "productUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                            "url": "https://bucket.s3.amazonaws.com/product-images/uuid1.png",
                                                            "brandName": "브랜드1",
                                                            "name": "상품1",
                                                            "price": 10000,
                                                            "discountRate": 10,
                                                            "discountPrice": 9000,
                                                            "rating": 4.6
                                                          }
                                                        ],
                                                        "artists": [
                                                          {
                                                            "artistId": 1,
                                                            "artistName": "김작가",
                                                            "profileImageUrl": "https://example.com/profile.jpg"
                                                          }
                                                        ],
                                                        "fundings": [
                                                          {
                                                            "id": 101,
                                                            "title": "펀딩명",
                                                            "imageUrl": "https://bucket.s3.amazonaws.com/funding-images/uuid1.png",
                                                            "categoryName": "스티커",
                                                            "authorName": "김작가",
                                                            "targetAmount": "600000",
                                                            "currentAmount": 500000,
                                                            "progress": "50",
                                                            "remainingDays": "10"  
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public RsData<SearchResponseDto> search(
            @Parameter(description = "검색할 키워드 (상품명/작가명/펀딩 제목)", example = "스티커")
            @RequestParam(required = false)
            String keyword) {
        SearchResponseDto results = searchService.search(keyword);
        return RsData.of("200", "검색 성공", results);
    }
}
