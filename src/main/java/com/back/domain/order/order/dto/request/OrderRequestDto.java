package com.back.domain.order.order.dto.request;

import com.back.domain.order.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

/**
 * 주문 생성 요청 DTO
 */
public record OrderRequestDto(
        @NotEmpty(message = "주문 상품 목록은 필수입니다.")
        List<OrderItemRequestDto> orderItems,
        
        @NotBlank(message = "배송지 주소는 필수입니다.")
        String shippingAddress1,
        
        String shippingAddress2,
        
        @NotBlank(message = "우편번호는 필수입니다.")
        String shippingZip,
        
        @NotBlank(message = "수령인 이름은 필수입니다.")
        String recipientName,
        
        @NotBlank(message = "수령인 전화번호는 필수입니다.")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String recipientPhone,
        
        String deliveryRequest,
        
        @NotNull(message = "결제 방법은 필수입니다.")
        PaymentMethod paymentMethod
) {
    
    /**
     * 주문 상품 요청 DTO (NORMAL/FUNDING 통합)
     */
    public record OrderItemRequestDto(
            // NORMAL 상품용 (FUNDING일 때는 null 가능)
            UUID productUuid,

            // FUNDING 상품용 (NORMAL일 때는 null 가능)
            Long fundingId,
            Integer fundingPrice,
            Integer fundingStock,

            @NotNull(message = "수량은 필수입니다.")
            Integer quantity,

            String optionInfo,

            // 장바구니 타입: NORMAL 또는 FUNDING
            String cartType
    ) {}
}
