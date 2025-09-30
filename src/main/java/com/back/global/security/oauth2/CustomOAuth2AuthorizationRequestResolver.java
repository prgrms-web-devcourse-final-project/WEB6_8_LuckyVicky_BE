package com.back.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * OAuth2 인증 요청 커스터마이징
 * OAuth 로그인 후 원래 페이지로 돌아가기 위한 설정을 하는 클래스
 */
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Spring Security 기본 OAuth2 인증 요청 Resolver 생성
     */
    private DefaultOAuth2AuthorizationRequestResolver createDefaultResolver() {
        return new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    /**
     * OAuth2 인증 요청 생성 (registrationId 자동 추출)
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = createDefaultResolver().resolve(request);
        return customizeState(authorizationRequest, request);
    }

    /**
     * OAuth2 인증 요청 생성 (registrationId 명시)
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = createDefaultResolver().resolve(request, clientRegistrationId);
        return customizeState(authorizationRequest, request);
    }

    /**
     * state 파라미터 커스터마이징
     * redirectUrl과 CSRF 방지용 nonce를 결합하여 Base64로 인코딩 후 state에 저장
     */
    private OAuth2AuthorizationRequest customizeState(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        if (authorizationRequest == null) return null;

        // 1. 요청 파라미터에서 redirectUrl 가져오기
        String redirectUrl = request.getParameter("redirectUrl");
        if (redirectUrl == null || redirectUrl.isBlank()) redirectUrl = "/";

        // 2. CSRF 방지용 nonce 생성
        String originState = UUID.randomUUID().toString();

        // 3. redirectUrl#originState 결합
        String rawState = redirectUrl + "#" + originState;

        // 4. Base64 URL-safe 인코딩
        String encodedState = Base64.getUrlEncoder().encodeToString(rawState.getBytes(StandardCharsets.UTF_8));

        // 5. state 교체
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(encodedState)
                .build();
    }

}
