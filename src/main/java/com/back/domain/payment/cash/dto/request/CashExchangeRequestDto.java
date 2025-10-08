package com.back.domain.payment.cash.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캐시 환전 요청 DTO (작가 전용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashExchangeRequestDto {

    @NotNull(message = "환전 금액은 필수입니다")
    @Min(value = 10000, message = "최소 환전 금액은 10,000원입니다")
    private Integer amount; // 환전 금액

    @NotBlank(message = "환전 계좌 은행은 필수입니다")
    private String bankName; // 은행명

    @NotBlank(message = "환전 계좌 번호는 필수입니다")
    private String accountNumber; // 계좌번호

    @NotBlank(message = "예금주명은 필수입니다")
    private String accountHolder; // 예금주
}

