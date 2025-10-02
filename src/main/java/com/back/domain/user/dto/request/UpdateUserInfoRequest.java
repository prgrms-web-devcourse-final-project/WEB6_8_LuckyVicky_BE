package com.back.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자 정보 수정 요청 DTO
 */
public record UpdateUserInfoRequest(

    String profileImageUrl,

    @NotBlank(message = "닉네임은 필수 입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    String name,

    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$",
            message = "전화번호는 010-1234-5678 형식이어야 합니다.")
    String phone,

    String address,

    String detailAddress,

    String zipCode,

    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    String password,

    String passwordConfirm
) {

    // 비밀번호 변경 여부 확인
    public boolean isPasswordChange() {
        return password != null && !password.isBlank();
    }

    // 비밀번호 확인 일치 여부 확인
    public boolean isPasswordMatching() {
        if (!isPasswordChange()) {
            return true; // 비밀번호 변경이 없으면 항상 일치하는 것으로 간주
        }
        return password.equals(passwordConfirm);
    }

}
