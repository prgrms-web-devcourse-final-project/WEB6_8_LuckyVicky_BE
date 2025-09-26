package com.back.domain.cart.controller;

import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.service.CartService;
import com.back.domain.user.entity.User;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니", description = "장바구니 관련 API")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @Operation(summary = "장바구니에 상품 추가", description = "새로운 상품을 장바구니에 추가합니다.")
    public ResponseEntity<RsData<CartResponseDto>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartRequestDto requestDto) {
        
        CartResponseDto responseDto = cartService.addToCart(user, requestDto);
        return ResponseEntity.ok(RsData.of("200", "장바구니에 상품이 추가되었습니다.", responseDto));
    }

    @GetMapping
    @Operation(summary = "장바구니 목록 조회", description = "사용자의 장바구니 목록을 조회합니다.")
    public ResponseEntity<RsData<CartListResponseDto>> getCartItems(@AuthenticationPrincipal User user) {
        
        CartListResponseDto responseDto = cartService.getCartItems(user);
        return ResponseEntity.ok(RsData.of("200", "장바구니 목록을 조회했습니다.", responseDto));
    }

    @PutMapping("/{cartId}/quantity")
    @Operation(summary = "장바구니 수량 수정", description = "장바구니 상품의 수량을 수정합니다.")
    public ResponseEntity<RsData<CartResponseDto>> updateQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {
        
        CartResponseDto responseDto = cartService.updateQuantity(user, cartId, quantity);
        return ResponseEntity.ok(RsData.of("200", "수량이 수정되었습니다.", responseDto));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "장바구니에서 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
    public ResponseEntity<RsData<Void>> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartId) {
        
        cartService.removeFromCart(user, cartId);
        return ResponseEntity.ok(RsData.of("200", "장바구니에서 상품이 삭제되었습니다."));
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 삭제", description = "사용자의 모든 장바구니 아이템을 삭제합니다.")
    public ResponseEntity<RsData<Void>> clearCart(@AuthenticationPrincipal User user) {
        
        cartService.clearCart(user);
        return ResponseEntity.ok(RsData.of("200", "장바구니가 비워졌습니다."));
    }

    @DeleteMapping("/type/{cartType}")
    @Operation(summary = "타입별 장바구니 삭제", description = "일반 또는 펀딩 장바구니만 삭제합니다.")
    public ResponseEntity<RsData<Void>> clearCartByType(
            @AuthenticationPrincipal User user,
            @PathVariable String cartType) {
        
        cartService.clearCartByType(user, cartType);
        return ResponseEntity.ok(RsData.of("200", cartType + " 장바구니가 비워졌습니다."));
    }

    @PutMapping("/{cartId}/toggle-selection")
    @Operation(summary = "장바구니 선택 상태 토글", description = "장바구니 상품의 선택 상태를 변경합니다.")
    public ResponseEntity<RsData<CartResponseDto>> toggleSelection(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartId) {
        
        CartResponseDto responseDto = cartService.toggleSelection(user, cartId);
        return ResponseEntity.ok(RsData.of("200", "선택 상태가 변경되었습니다.", responseDto));
    }

    @GetMapping("/selected")
    @Operation(summary = "선택된 장바구니 아이템 조회", description = "선택된 장바구니 아이템들만 조회합니다.")
    public ResponseEntity<RsData<List<CartResponseDto>>> getSelectedCartItems(@AuthenticationPrincipal User user) {
        
        List<CartResponseDto> responseDtos = cartService.getSelectedCartItems(user);
        return ResponseEntity.ok(RsData.of("200", "선택된 장바구니 아이템을 조회했습니다.", responseDtos));
    }
}