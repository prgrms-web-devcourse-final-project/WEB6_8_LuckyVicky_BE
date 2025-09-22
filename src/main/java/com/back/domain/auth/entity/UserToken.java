package com.back.domain.auth.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "UsersTokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserToken extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String refreshToken;

    private LocalDateTime expiresAt;

    /**
     * 토큰 활성화 여부
     * true: 활성화
     * false: 비활성화(로그아웃 등)
     */
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    private TokenType tokenType;

    // RefreshToken 생성 메서드
    public static UserToken createRefreshToken(User user, String refreshToken, LocalDateTime expiresAt, TokenType tokenType) {
        return UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .isActive(true)
                .tokenType(tokenType)
                .build();
    }

    // 토큰 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // 토큰 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // 토큰 유효성 확인 (활성화 + 만료되지 않음)
    public boolean isValid() {
        return this.isActive && !isExpired();
    }

}
