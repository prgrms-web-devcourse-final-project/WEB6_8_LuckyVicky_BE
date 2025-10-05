package com.back.domain.product.category.controller;

import com.back.domain.product.category.dto.request.CategoryRequest;
import com.back.domain.product.category.dto.response.CategoryResponse;
import com.back.domain.product.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "카테고리", description = "카테고리 관련 API")
public class CategoryController {
    private final CategoryService categoryService;

    /** 전체 카테고리 조회 */
    @GetMapping
    @Operation(
            summary = "전체 카테고리 조회",
            description = "상위/하위 카테고리 전체 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    [
                                                      {
                                                        "id": 1,
                                                        "categoryName": "스티커",
                                                        "subCategories": [
                                                          {
                                                            "id": 2,
                                                            "categoryName": "씰스티커",
                                                            "subCategories": []
                                                          },
                                                          {
                                                            "id": 3,
                                                            "categoryName": "자석스티커",
                                                            "subCategories": []
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /** 카테고리 등록 */
    @PostMapping
    @Operation(
            summary = "카테고리 등록",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "id": 1,
                                                      "categoryName": "스티커",
                                                      "subCategories": []
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복 카테고리 또는 잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "동일한 이름의 카테고리가 이미 존재합니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public CategoryResponse createCategory(@RequestBody @Valid CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    /** 카테고리 수정 */
    @PutMapping("/{id}")
    @Operation(
            summary = "카테고리 수정",
            parameters = {
                    @Parameter(name = "id", description = "수정할 카테고리 ID", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "id": 1,
                                                      "categoryName": "스티커",
                                                      "subCategories": []
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복 카테고리 이름",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "동일한 이름의 카테고리가 이미 존재합니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "수정할 카테고리 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "404",
                                                      "msg": "카테고리를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public CategoryResponse updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    /** 카테고리 삭제 */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "카테고리 삭제",
            parameters = {
                    @Parameter(name = "id", description = "삭제할 카테고리 ID", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "하위 카테고리 또는 상품 존재로 삭제 불가",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "하위 카테고리가 존재하여 삭제할 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "삭제할 카테고리 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "404",
                                                      "msg": "카테고리를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

}
