package com.back.global.security.oauth2;

import java.util.Map;

/**
 * Naver OAuth2 사용자 정보 구현체
 *
 * Naver OAuth2 응답 예시:
 * {
 *   "resultcode": "00",
 *   "message": "success",
 *   "response": {
 *     "id": "1234567890",
 *     "name": "홍길동",
 *     "email": "hong@naver.com",
 *     "profile_image": "https://phinf.pstatic.net/..."
 *   }
 * }
 */
public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    // SpringSecurity가 Naver로부터 받은 JSON을 Map 형태로 전달
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("id");
    }

    @Override
    public String getProvider() {
        return "NAVER";
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return (String) response.get("profile_image");
    }

}
