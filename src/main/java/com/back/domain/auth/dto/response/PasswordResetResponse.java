package com.back.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 찾기 응답")
public record PasswordResetResponse(
        @Schema(description = "이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "응답 메시지", example = "임시 비밀번호가 이메일로 발송되었습니다.")
        String message
) {
    public static PasswordResetResponse success(String email) {
        return new PasswordResetResponse(
                email,
                "임시 비밀번호가 이메일로 발송되었습니다."
        );
    }
}