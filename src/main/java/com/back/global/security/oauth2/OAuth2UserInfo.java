package com.back.global.security.oauth2;

/**
 * Google, Kakao, Naver 등 각 Provider의 응답 형식이 다르기 때문에
 * OAuth2 Provider 별로 사용자 정보를 추상화하기 위한 인터페이스
 */
public interface OAuth2UserInfo {

    // Provider 고유 ID (예: Google의 sub, Kakao의 id 등)
    String getProviderId();

    // Provider 이름 (예: google, kakao, naver 등)
    String getProvider();

    String getEmail();
    String getName();
    String getProfileImageUrl();
}
