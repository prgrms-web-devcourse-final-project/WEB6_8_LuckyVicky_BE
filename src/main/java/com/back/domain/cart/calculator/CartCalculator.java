package com.back.domain.cart.calculator;

import com.back.domain.cart.dto.response.CartResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 장바구니 총합 계산을 담당하는 클래스
 */
@Component
public class CartCalculator {

    /**
     * 총 수량 계산
     */
    public Integer calculateTotalQuantity(List<CartResponseDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0;
        }
        
        return cartItems.stream()
                .mapToInt(CartResponseDto::quantity)
                .sum();
    }

    /**
     * 총 금액 계산
     */
    public Integer calculateTotalAmount(List<CartResponseDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0;
        }
        
        return cartItems.stream()
                .mapToInt(item -> item.price() * item.quantity())
                .sum();
    }

    /**
     * 장바구니 아이템의 개별 금액 계산
     */
    public Integer calculateItemAmount(CartResponseDto cartItem) {
        if (cartItem == null) {
            return 0;
        }
        
        return cartItem.price() * cartItem.quantity();
    }
}
