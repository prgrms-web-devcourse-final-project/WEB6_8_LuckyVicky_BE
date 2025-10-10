package com.back.domain.auth.dto.response;

import com.back.domain.user.entity.Role;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,

        // 현재 로그인한 역할
        Role selectedRole,

        // 로그인 가능한 역할 목록
        List<Role> availableRoles,

        Long accessTokenExpiresIn,

        // 추가 정보 필요 여부 (소셜로그인 사용자)
        boolean needsAdditionalInfo
) {

    // 로그인 성공 응답용 정적 팩토리 메서드
    public static AuthResponse loginSuccess(
            String accessToken,
            String refreshToken,
            Long userId,
            String email,
            Role selectedRole,
            List<Role> availableRoles,
            Long accessTokenExpiresIn,
            boolean needsAdditionalInfo
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                userId,
                email,
                selectedRole,
                availableRoles,
                accessTokenExpiresIn,
                needsAdditionalInfo
        );
    }

    // 토큰 갱신 성공 응답용 정적 팩토리 메서드
    public static AuthResponse tokenRefreshSuccess(
            String accessToken,
            String refreshToken,
            Long userId,
            String email,
            Role selectedRole,
            List<Role> availableRoles,
            Long accessTokenExpiresIn,
            boolean needsAdditionalInfo
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                userId,
                email,
                selectedRole,
                availableRoles,
                accessTokenExpiresIn,
                needsAdditionalInfo
        );
    }
}
