package com.back.domain.artist.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 작가 프로필 수정 요청 DTO
 */
public record UpdateArtistProfileRequest(
        @Size(max = 500, message = "프로필 이미지 URL은 최대 500자까지 가능합니다.")
        String profileImageUrl,

        @Size(min = 2, max = 20, message = "작가명은 2자 이상 20자 이하여야 합니다.")
        String artistName,

        @Size(max = 100, message = "SNS 계정은 최대 100자까지 가능합니다.")
        String snsAccount,

        @Size(max = 2000, message = "작가 소개는 최대 2000자까지 가능합니다.")
        String description,

        @Size(max = 200, message = "사업장 주소는 최대 200자까지 가능합니다.")
        String businessAddress,

        @Size(max = 200, message = "사업장 상세주소는 최대 200자까지 가능합니다.")
        String businessAddressDetail,

        @Size(max = 10, message = "우편번호는 최대 10자까지 가능합니다.")
        String businessZipCode,

        @Size(max = 20, message = "예금주명은 최대 20자까지 가능합니다.")
        String accountName,

        @Size(max = 50, message = "은행명은 최대 50자까지 가능합니다.")
        String bankName,

        @Size(max = 50, message = "계좌번호는 최대 50자까지 가능합니다.")
        String bankAccount,

        String managerPhone
) {

    /**
     * 수정할 필드가 하나라도 있는지 확인
     */
    public boolean hasAnyUpdate() {
        return profileImageUrl != null ||
                artistName != null ||
                snsAccount != null ||
                description != null ||
                businessAddress != null ||
                businessAddressDetail != null ||
                businessZipCode != null ||
                accountName != null ||
                bankName != null ||
                bankAccount != null ||
                managerPhone != null;
    }
}
