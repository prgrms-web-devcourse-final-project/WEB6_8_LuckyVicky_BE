package com.back.domain.payment.settlement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequestDto {

    @NotNull(message = "환전 금액은 필수입니다")
    @Min(value = 1000, message = "최소 환전 금액은 1,000원입니다")
    private Integer amount; // 환전 금액만 입력
}
