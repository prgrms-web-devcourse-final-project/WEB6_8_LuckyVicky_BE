package com.back.domain.artist.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 관리자용 작가 신청서 승인/거절 요청 DTO
 */
public record ArtistApplicationReviewRequest(

        // 거절 사유 (거절 시에만 필요)
        @Size(max = 500, message = "거절 사유는 최대 500자까지 입력할 수 있습니다.")
        String rejectionReason
) {

    // 거절 사유가 유효한지 검증
    public boolean hasValidRejectionReason() {
        return rejectionReason != null && !rejectionReason.trim().isEmpty();
    }
}
