package com.back.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 실패 시 처리 핸들러
 * 에러 메시지를 포함하여 프론트엔드 로그인 페이지로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * OAuth2 로그인 실패 시 호출
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String errorMessage = exception.getMessage();

        log.error("OAuth2 로그인 실패 - errorMessage: {}", errorMessage, exception);

        // 에러 메시지 URL 인코딩
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // 프론트엔드 로그인 페이지로 리다이렉트 (에러 메시지 포함)
        String targetUrl = String.format(
                "%s/login?error=oauth_failed&message=%s",
                frontendUrl,
                encodedMessage
        );

        log.info("OAuth2 실패 후 리다이렉트 - targetUrl: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
