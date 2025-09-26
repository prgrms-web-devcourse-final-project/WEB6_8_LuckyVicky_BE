package com.back.domain.auth.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

public record ValidationCheckResponse(

        @Schema(description = "검사한 값", example = "test@example.com")
        String value,

        @Schema(description = "검사한 필드 타입", example = "email")
        String fieldType,

        @Schema(description = "중복 여부", example = "false")
        boolean isDuplicate,

        @Schema(description = "검사 결과 메시지", example = "사용 가능한 이메일입니다.")
        String message,

        @Schema(description = "사용 가능 여부", example = "true")
        boolean isAvailable

) {
    public static ValidationCheckResponse of(String value, String fieldType, boolean isDuplicate, String message) {
        return new ValidationCheckResponse(
                value,
                fieldType,
                isDuplicate,
                message,
                !isDuplicate  // 중복이 아니면 사용 가능
        );
    }
}
