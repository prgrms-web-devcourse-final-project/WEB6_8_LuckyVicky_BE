package com.back.domain.artist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관리자용 작가 신청서 거절 요청 DTO
 */
public record RejectArtistApplicationRequest(

        @NotBlank(message = "거절 사유는 필수입니다.")
        @Size(min = 10, max = 500, message = "거절 사유는 10자 이상 500자 이하여야 합니다.")
        String rejectionReason
) {
}
