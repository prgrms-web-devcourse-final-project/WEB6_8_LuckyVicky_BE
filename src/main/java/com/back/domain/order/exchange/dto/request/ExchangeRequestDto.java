package com.back.domain.order.exchange.dto.request;

import com.back.domain.order.exchange.entity.Exchange;
import com.back.domain.order.exchange.entity.ExchangeReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 교환 신청 요청 DTO
 */
public record ExchangeRequestDto(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,
        
        @NotNull(message = "교환할 주문상품 ID 목록은 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 주문상품을 선택해야 합니다.")
        List<Long> orderItemIds,
        
        @NotNull(message = "교환 사유 타입은 필수입니다.")
        ExchangeReasonType reasonType, // 교환 사유 타입 (재고 복원 여부 자동 판단)
        
        @NotBlank(message = "교환 사유는 필수입니다.")
        @Size(max = 100, message = "교환 사유는 100자를 초과할 수 없습니다.")
        String reason,
        
        @Size(max = 1000, message = "상세 사유는 1000자를 초과할 수 없습니다.")
        String detailReason,
        
        @NotNull(message = "교환 방법은 필수입니다.")
        Exchange.ExchangeMethod exchangeMethod,
        
        List<String> attachmentFiles, // 첨부파일 목록
        
        // 새 배송지 정보 (교환 시 필요)
        @NotBlank(message = "새 배송지 주소는 필수입니다.")
        String newShippingAddress1,
        
        String newShippingAddress2,
        
        @NotBlank(message = "새 우편번호는 필수입니다.")
        String newShippingZip,
        
        @NotBlank(message = "새 수령인 이름은 필수입니다.")
        String newRecipientName,
        
        @NotBlank(message = "새 수령인 전화번호는 필수입니다.")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String newRecipientPhone
) {}
