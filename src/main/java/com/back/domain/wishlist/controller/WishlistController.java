package com.back.domain.wishlist.controller;

import com.back.domain.wishlist.service.WishlistService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist/{productUuid}")
@Tag(name = "상품 찜", description = "상품 찜 관련 API")
public class WishlistController {

    private final WishlistService wishlistService;

    /** 찜 등록 */
    @PostMapping
    @Operation(
            summary = "찜 등록",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "찜 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                            {
                                              "resultCode": "200",
                                              "msg": "상품이 위시리스트에 추가되었습니다.",
                                              "data": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<UUID>> addWishlist(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UUID addWishlistProductUuid = wishlistService.addWishlist(productUuid, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품이 위시리스트에 추가되었습니다.", addWishlistProductUuid));
    }

    /** 찜 삭제 */
    @DeleteMapping
    @Operation(
            summary = "찜 삭제",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "찜 삭제 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                            {
                                              "resultCode": "200",
                                              "msg": "상품이 위시리스트에서 제거되었습니다.",
                                              "data": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<UUID>> removeWishlist(
            @PathVariable UUID productUuid,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UUID removeWishlistProductUuid = wishlistService.removeWishlist(productUuid, customUserDetails);
        return ResponseEntity.ok(RsData.of("200", "상품이 위시리스트에서 제거되었습니다.", removeWishlistProductUuid));
    }

    /** 상품별 찜 개수 조회 */
    @GetMapping("/count")
    @Operation(
            summary = "상품별 찜 개수 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상품별 찜 개수 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                            {
                                              "resultCode": "200",
                                              "msg": "상품 찜 개수 조회 성공",
                                              "data": 10
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<RsData<Long>> getWishlistCount(
            @PathVariable UUID productUuid) {
        Long count = wishlistService.getWishlistCount(productUuid);
        return ResponseEntity.ok(RsData.of("200", "상품 찜 개수 조회 성공", count));
    }
}
