package com.back.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String passwordConfirm,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        Boolean privacyRequiredAgreed,
        Boolean marketingAgreed
) {
    // 기본값 생성자
    public SignUpRequest {
        if (privacyRequiredAgreed == null) privacyRequiredAgreed = false;
        if (marketingAgreed == null) marketingAgreed = false;
    }

    // 비밀번호 확인 검증
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }

    // 필수 약관 동의 검증
    public boolean isRequiredTermsAgreed() {
        return Boolean.TRUE.equals(privacyRequiredAgreed);
    }
}