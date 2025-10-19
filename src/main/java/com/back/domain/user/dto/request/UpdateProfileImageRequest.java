package com.back.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank(message = "프로필 이미지 URL은 필수입니다.")
        String profileImageUrl
) {
}