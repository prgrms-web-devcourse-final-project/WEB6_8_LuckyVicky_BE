package com.back.domain.product.tag.controller;

import com.back.domain.product.tag.dto.request.TagRequest;
import com.back.domain.product.tag.dto.response.TagResponse;
import com.back.domain.product.tag.service.TagService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
@Tag(name="태그", description="태그(스타일) 관련 API")
public class TagController {
    private final TagService tagService;

    /** 전체 태그 조회 */
    @GetMapping
    @Operation(
            summary= "전체 태그 조회",
            description = "등록된 모든 태그 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "태그 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    [
                                                      {"id": 1, "tagName": "심플"},
                                                      {"id": 2, "tagName": "감성"},
                                                      {"id": 3, "tagName": "빈티지"}
                                                    ]
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(RsData.of("200","태그 조회 성공", tags));
    }

    /** 태그 등록 */
    @PostMapping
    @Operation(
            summary = "태그 등록",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "태그 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {"id": 4, "tagName": "귀염"}
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복 태그 또는 잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "동일한 이름의 태그가 이미 존재합니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<TagResponse>> createTag(@RequestBody @Valid TagRequest request) {
        TagResponse created = tagService.createTag(request);
        return ResponseEntity.ok(RsData.of("200", "태그가 등록되었습니다.", created));
    }

    /** 태그 수정 */
    @PutMapping("/{id}")
    @Operation(
            summary = "태그 수정",
            parameters = {
                    @Parameter(name = "id", description = "수정할 태그 ID", required = true, example="2")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "태그 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {"id": 2, "tagName": "동양풍"}
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "중복 태그 이름",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "400",
                                                      "msg": "동일한 이름의 태그가 이미 존재합니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "수정할 태그 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "404",
                                                      "msg": "태그를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<TagResponse>> updateTag(
            @PathVariable Long id,
            @RequestBody @Valid TagRequest request) {
        TagResponse updated = tagService.updateTag(id, request);
        return ResponseEntity.ok(RsData.of("200", "태그가 수정되었습니다.", updated));
    }

    /** 태그 삭제 */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "태그 삭제",
            parameters = {
                    @Parameter(name = "id", description = "삭제할 태그 ID", required = true, example="1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "태그 삭제 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "200",
                                                      "msg": "태그가 삭제되었습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "삭제할 태그 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                      "resultCode": "404",
                                                      "msg": "태그를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "해당 태그를 가진 상품이 있어 삭제할 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                {
                                                  "resultCode": "400",
                                                  "msg": "해당 태그를 가진 상품이 있어 삭제할 수 없습니다.",
                                                  "data": null
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(RsData.of("200", "태그가 삭제되었습니다."));
    }
}
