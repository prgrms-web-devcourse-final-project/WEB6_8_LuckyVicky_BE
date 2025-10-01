package com.back.domain.artist.dto.response;

import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;

import java.time.LocalDateTime;

/**
 * 작가 신청서 목록 조회용 간단한 응답 DTO
 */
public record ArtistApplicationSimpleResponse (
        Long id,
        String artistName,
        String email,
        ApplicationStatus status,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String reviewedByName
) {

    /**
     * Entity -> Response DTO 변환
     */
    public static ArtistApplicationSimpleResponse from(ArtistApplication application) {
        return new ArtistApplicationSimpleResponse(
                application.getId(),
                application.getArtistName(),
                application.getEmail(),
                application.getStatus(),
                application.getRejectionReason(),
                application.getCreateDate(),
                application.getReviewedAt(),
                application.getReviewedByName()
        );
    }
}
