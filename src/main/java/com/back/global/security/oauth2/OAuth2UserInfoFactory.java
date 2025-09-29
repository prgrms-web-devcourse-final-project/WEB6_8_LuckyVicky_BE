package com.back.global.security.oauth2;

import com.back.global.exception.ServiceException;

import java.util.Map;

/**
 * OAuth2UserInfo 팩토리 클래스
 * 주어진 Provider에 따라 적절한 OAuth2UserInfo 구현체를 생성
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toUpperCase()) {
            case "GOOGLE" -> new GoogleUserInfo(attributes);
            case "KAKAO" -> new KakaoUserInfo(attributes);
            case "NAVER" -> new NaverUserInfo(attributes);
            default -> throw new ServiceException("400", "지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        };
    }
}
