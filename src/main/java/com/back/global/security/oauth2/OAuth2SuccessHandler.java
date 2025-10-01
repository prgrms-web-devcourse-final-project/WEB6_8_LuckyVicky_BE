package com.back.global.security.oauth2;

import com.back.domain.auth.entity.UserToken;
import com.back.domain.auth.repository.UserTokenRepository;
import com.back.domain.user.entity.User;
import com.back.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * OAuth2 로그인 성공 시 처리 핸들러
 * JWT 토큰 발급 후 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenRepository userTokenRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * OAuth2 로그인 성공 시 호출
     * JWT 토큰 생성 후 프론트엔드로 리다이렉트
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 1. 인증된 사용자 정보 가져오기
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        log.info("OAuth2 로그인 성공 - userId: {}, email: {}, provider: {}",
                user.getId(), user.getEmail(), user.getProvider());

        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                user.getRole()
        );

        // 3. RefreshToken DB 저장
        UserToken userToken = UserToken.createRefreshToken(
                user,
                refreshToken,
                LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000),
                user.getRole()
        );
        userTokenRepository.save(userToken);

        log.info("JWT 토큰 발급 완료 - userId: {}", user.getId());

        // 4. 쿠키 생성 (기존 로그인 방식과 동일)
        ResponseCookie accessTokenCookie = createTokenCookie(
                "accessToken",
                accessToken,
                accessTokenExpiration / 1000
        );

        ResponseCookie refreshTokenCookie = createTokenCookie(
                "refreshToken",
                refreshToken,
                refreshTokenExpiration / 1000
        );

        // 5. 쿠키 설정
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // 6. state 파라미터에서 redirectUrl 추출
        String redirectUrl = extractRedirectUrl(request);

        // 7. 프론트엔드로 리다이렉트 (쿠키로 토큰 전달, URL에 노출 안됨)
        String targetUrl = frontendUrl + redirectUrl;

        log.info("프론트엔드로 리다이렉트 - targetUrl: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 쿠키 생성 헬퍼 메서드 (기존 로그인과 동일)
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)  // 개발: false, 운영: true
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    /**
     * state 파라미터에서 redirectUrl 추출
     */
    private String extractRedirectUrl(HttpServletRequest request) {
        String stateParam = request.getParameter("state");

        if (stateParam == null || stateParam.isBlank()) {
            log.warn("state 파라미터가 없습니다. 기본 페이지로 이동합니다.");
            return "/";
        }

        try {
            // 1. Base64 디코딩
            String decodedState = new String(
                    Base64.getUrlDecoder().decode(stateParam),
                    StandardCharsets.UTF_8
            );

            // 2. redirectUrl#nonce 형태에서 redirectUrl만 추출
            String[] parts = decodedState.split("#", 2);
            String redirectUrl = parts[0];

            log.debug("redirectUrl 추출 성공: {}", redirectUrl);
            return redirectUrl;

        } catch (Exception e) {
            log.error("state 파라미터 디코딩 실패: {}", e.getMessage());
            return "/";
        }
    }
}
