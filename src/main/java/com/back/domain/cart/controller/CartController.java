package com.back.domain.cart.controller;

import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.service.CartService;
import com.back.domain.user.entity.User;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
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
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CartRequestDto requestDto) {
        
        User user = customUserDetails.getUser();
        CartResponseDto responseDto = cartService.addToCart(user, requestDto);
        return ResponseEntity.ok(RsData.of("200", "장바구니에 상품이 추가되었습니다.", responseDto));
    }

    @GetMapping
    @Operation(summary = "장바구니 목록 조회", description = "사용자의 장바구니 목록을 조회합니다.")
    public ResponseEntity<RsData<CartListResponseDto>> getCartItems(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        User user = customUserDetails.getUser();
        CartListResponseDto responseDto = cartService.getCartItems(user);
        return ResponseEntity.ok(RsData.of("200", "장바구니 목록을 조회했습니다.", responseDto));
    }

    @PutMapping("/{cartId}/quantity")
    @Operation(summary = "장바구니 수량 수정", description = "장바구니 상품의 수량을 수정합니다.")
    public ResponseEntity<RsData<CartResponseDto>> updateQuantity(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {
        
        User user = customUserDetails.getUser();
        CartResponseDto responseDto = cartService.updateQuantity(user, cartId, quantity);
        return ResponseEntity.ok(RsData.of("200", "수량이 수정되었습니다.", responseDto));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "장바구니에서 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
    public ResponseEntity<RsData<Void>> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long cartId) {
        
        User user = customUserDetails.getUser();
        cartService.removeFromCart(user, cartId);
        return ResponseEntity.ok(RsData.of("200", "장바구니에서 상품이 삭제되었습니다."));
    }

    @DeleteMapping
    @Operation(summary = "장바구니 전체 삭제", description = "사용자의 모든 장바구니 아이템을 삭제합니다.")
    public ResponseEntity<RsData<Void>> clearCart(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        User user = customUserDetails.getUser();
        cartService.clearCart(user);
        return ResponseEntity.ok(RsData.of("200", "장바구니가 비워졌습니다."));
    }

    @DeleteMapping("/type/{cartType}")
    @Operation(summary = "타입별 장바구니 삭제", description = "일반 또는 펀딩 장바구니만 삭제합니다.")
    public ResponseEntity<RsData<Void>> clearCartByType(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable String cartType) {
        
        User user = customUserDetails.getUser();
        cartService.clearCartByType(user, cartType);
        return ResponseEntity.ok(RsData.of("200", cartType + " 장바구니가 비워졌습니다."));
    }

    @PutMapping("/{cartId}/toggle-selection")
    @Operation(summary = "장바구니 선택 상태 토글", description = "장바구니 상품의 선택 상태를 변경합니다.")
    public ResponseEntity<RsData<CartResponseDto>> toggleSelection(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long cartId) {
        
        User user = customUserDetails.getUser();
        CartResponseDto responseDto = cartService.toggleSelection(user, cartId);
        return ResponseEntity.ok(RsData.of("200", "선택 상태가 변경되었습니다.", responseDto));
    }

    @PutMapping("/toggle-all-selection")
    @Operation(summary = "장바구니 전체 선택 토글", description = "모든 장바구니 아이템의 선택 상태를 일괄 변경합니다.")
    public ResponseEntity<RsData<List<CartResponseDto>>> toggleAllSelection(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam boolean isSelected) {
        
        User user = customUserDetails.getUser();
        List<CartResponseDto> responseDtos = cartService.toggleAllSelection(user, isSelected);
        String message = isSelected ? "모든 장바구니 아이템이 선택되었습니다." : "모든 장바구니 아이템이 해제되었습니다.";
        return ResponseEntity.ok(RsData.of("200", message, responseDtos));
    }

    @GetMapping("/selected")
    @Operation(
        summary = "선택된 장바구니 아이템 조회", 
        description = "선택된 장바구니 아이템들을 조회합니다. validateForOrder=true 시 유효한 아이템만 반환합니다."
    )
    public ResponseEntity<RsData<List<CartResponseDto>>> getSelectedCartItems(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "false") boolean validateForOrder) {
        
        User user = customUserDetails.getUser();
        List<CartResponseDto> responseDtos = cartService.getSelectedCartItems(user, validateForOrder);
        String message = validateForOrder ? 
            "선택 주문 가능한 장바구니 아이템을 조회했습니다." : 
            "선택된 장바구니 아이템을 조회했습니다.";
        return ResponseEntity.ok(RsData.of("200", message, responseDtos));
    }

    @GetMapping("/all")
    @Operation(
        summary = "전체 장바구니 아이템 조회", 
        description = "모든 장바구니 아이템을 조회합니다. validateForOrder=true 시 유효한 아이템만 반환합니다."
    )
    public ResponseEntity<RsData<List<CartResponseDto>>> getAllCartItems(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "false") boolean validateForOrder) {
        
        User user = customUserDetails.getUser();
        List<CartResponseDto> responseDtos = cartService.getAllCartItems(user, validateForOrder);
        String message = validateForOrder ? 
            "전체 주문 가능한 장바구니 아이템을 조회했습니다." : 
            "전체 장바구니 아이템을 조회했습니다.";
        return ResponseEntity.ok(RsData.of("200", message, responseDtos));
    }

    @PostMapping("/validate")
    @Operation(summary = "장바구니 주문 가능 여부 검증", description = "전체 또는 선택된 장바구니 아이템들의 주문 가능 여부를 검증합니다.")
    public ResponseEntity<RsData<Void>> validateCartItemsForOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "false") boolean isFullOrder) {
        
        User user = customUserDetails.getUser();
        cartService.validateCartItemsForOrder(user, isFullOrder);
        String message = isFullOrder ? "전체 주문 가능합니다." : "선택 주문 가능합니다.";
        return ResponseEntity.ok(RsData.of("200", message));
    }

    @GetMapping("/total-amount")
    @Operation(summary = "장바구니 총 금액 계산", description = "전체 또는 선택된 장바구니 아이템들의 총 금액을 계산합니다.")
    public ResponseEntity<RsData<Integer>> calculateTotalAmount(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "false") boolean isFullOrder) {
        
        User user = customUserDetails.getUser();
        Integer totalAmount = cartService.calculateTotalAmount(user, isFullOrder);
        String message = isFullOrder ? "전체 장바구니 총 금액" : "선택된 장바구니 총 금액";
        return ResponseEntity.ok(RsData.of("200", message, totalAmount));
    }
}