package com.back.domain.user.dto.response;

import com.back.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long userId,
        String email,
        String name,
        String profileImageUrl,
        String phone,
        String address,
        String detailAddress,
        String zipCode,
        String role,
        String grade,
        String status,
        String provider,  // OAuth 여부 확인용
        Integer money,
        Integer point,
        LocalDateTime createdAt
) {


    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                user.getPhone(),
                user.getAddress(),
                user.getDetailAddress(),
                user.getZipCode(),
                user.getRole().name(),
                user.getGrade().name(),
                user.getStatus().name(),
                user.getProvider().name(),
                user.getMoney(),
                user.getPoint(),
                user.getCreateDate()
        );
    }

    /**
     * 공개 프로필용 (민감한 정보 제외)
     */
    public static UserProfileResponse publicProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                null, // 이메일 비공개
                user.getName(),
                user.getProfileImageUrl(),
                null, // 전화번호 비공개
                null, // 주소 비공개
                null,
                null,
                user.getRole().name(),
                user.getGrade().name(),
                null, // 계정 상태 비공개
                null, // provider 비공개
                null, // 머니 비공개
                null, // 포인트 비공개
                user.getCreateDate()
        );
    }
}
