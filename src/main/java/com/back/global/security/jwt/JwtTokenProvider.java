package com.back.global.security.jwt;

import com.back.domain.auth.entity.TokenType;
import com.back.global.exception.ServiceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * AccessToken 생성
     */
    public String createAccessToken(Long userId, String email, TokenType role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * RefreshToken 생성
     */
    public String createRefreshToken(Long userId, TokenType role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            throw new ServiceException("JWT_INVALID_EMAIL", "토큰에서 이메일 추출에 실패했습니다.");
        }
    }

    /**
     * 토큰에서 userId 추출
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            throw new ServiceException("JWT_INVALID_USER_ID", "토큰에서 사용자 ID 추출에 실패했습니다.");
        }
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("유효하지 않은 서명: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("빈 토큰: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 파싱 (공통 메서드)
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
