package com.back.global.security.oauth2;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 구현체
 * 
 * Google OAuth2 응답 예시:
 * {
 *   "sub": "1234567890",           // Google 고유 ID
 *   "name": "홍길동",
 *   "email": "hong@gmail.com",
 *   "picture": "https://lh3.googleusercontent.com/..."
 * }
 */
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    // SpringSecurity가 Google로부터 받은 JSON을 Map 형태로 전달
    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "GOOGLE";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture");
    }

}
