package com.back.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "중복 검사 요청")
public record ValidationCheckRequest (

        @Schema(description = "검사할 값", example = "test@example.com")
        @NotBlank(message = "검사할 값은 필수입니다.")
        String value
){

}
