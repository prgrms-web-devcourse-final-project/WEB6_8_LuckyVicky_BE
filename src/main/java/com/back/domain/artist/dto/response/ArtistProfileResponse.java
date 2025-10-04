package com.back.domain.artist.dto.response;

import com.back.domain.artist.entity.ArtistProfile;

import java.time.LocalDateTime;

public record ArtistProfileResponse(
        Long id,
        Long userId,
        String artistName,
        String email,                    // User에서 가져옴
        String phone,                    // User에서 가져옴
        String description,
        String profileImageUrl,
        String snsAccount,
        String mainProducts,

        // 사업자 정보
        String businessAddress,
        String businessAddressDetail,
        String businessZipCode,
        String managerPhone,

        // 은행 정보
        String bankName,
        String bankAccount,
        String accountName,

        // 통계 정보
        Integer followerCount,
        Long totalSales,
        Integer productCount,

        LocalDateTime createdAt
) {
    /**
     * Entity -> DTO 변환
     */
    public static ArtistProfileResponse from(ArtistProfile profile) {
        return new ArtistProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getArtistName(),
                profile.getEmail(),          // User 참조 (자동 동기화)
                profile.getPhone(),          // User 참조 (자동 동기화)
                profile.getDescription(),
                profile.getProfileImageUrl(),
                profile.getSnsAccount(),
                profile.getMainProducts(),
                profile.getBusinessAddress(),
                profile.getBusinessAddressDetail(),
                profile.getBusinessZipCode(),
                profile.getManagerPhone(),
                profile.getBankName(),
                profile.getBankAccount(),
                profile.getAccountName(),
                profile.getFollowerCount(),
                profile.getTotalSales(),
                profile.getProductCount(),
                profile.getCreateDate()
        );
    }
}
