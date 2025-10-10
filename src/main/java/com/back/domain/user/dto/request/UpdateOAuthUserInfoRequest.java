package com.back.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOAuthUserInfoRequest(
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$",
                message = "전화번호는 010-1234-5678 형식이어야 합니다.")
        String phone
) {
}