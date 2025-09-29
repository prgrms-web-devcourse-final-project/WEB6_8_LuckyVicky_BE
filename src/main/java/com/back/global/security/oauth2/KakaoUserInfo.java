package com.back.global.security.oauth2;

import java.util.Map;

/**
 * Kakao Oauth2 사용자 정보 구현체
 * 
 * Kakao OAuth2 응답 예시:
 * {
 *   "id": 1234567890,
 *   "properties": {
 *     "nickname": "홍길동",
 *     "profile_image": "https://k.kakaocdn.net/..."
 *   },
 *   "kakao_account": {
 *     "email": "hong@kakao.com"
 *   }
 * }
 */
public class KakaoUserInfo implements OAuth2UserInfo{

    private final Map<String, Object> attributes;

    // SpringSecurity가 Kakao로부터 받은 JSON을 Map 형태로 전달
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        // Kakao는 id를 숫자로 주므로 String으로 변환
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "KAKAO";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("profile_image");
    }

}
