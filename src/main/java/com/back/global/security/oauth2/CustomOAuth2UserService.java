package com.back.global.security.oauth2;

import com.back.domain.user.entity.Provider;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 시 사용자 정보를 처리하는 서비스
 * Provider로부터 받은 사용자 정보로 회원 조회 및 회원가입을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Provider로부터 사용자 정보 조회
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. registrationId 추출 (예: google, facebook, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        log.info("OAuth2 로그인 시도: provider={}", registrationId);

        // 3. Provider별 사용자 정보 파싱
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oauth2User.getAttributes()
        );

        // 4. 사용자 조회 또는 생성
        User user = saveOrUpdate(userInfo);

        log.info("OAuth2 로그인 성공: userId={}, email={}, provider={}",
                user.getId(), user.getEmail(), user.getProvider());

        // 5. CustomOAuth2User로 변환하여 반환
        return new CustomOAuth2User(user, oauth2User.getAttributes());
    }

    /**
     * 사용자 조회 또는 생성
     * 기존 사용자면 정보 업데이트, 신규 사용자면 회원가입
     */
    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        Provider provider = Provider.valueOf(userInfo.getProvider().toUpperCase());

        return userRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(existingUser -> updateExistingUser(existingUser, userInfo))
                .orElseGet(() -> createNewUser(userInfo, provider));
    }

    /**
     * 기존 사용자 정보 업데이트
     * 재로그인 시 Provider에서 변경된 정보 반영
     */
    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        user.updateOAuthProfile(
                userInfo.getName(),
                userInfo.getProfileImageUrl()
        );

        log.info("기존 OAuth 사용자 프로필 업데이트: userId={}, name={}",
                user.getId(), userInfo.getName());

        return user;
    }

    /**
     * 신규 사용자 생성
     * OAuth 로그인으로 처음 가입하는 사용자
     */
    private User createNewUser(OAuth2UserInfo userInfo, Provider provider) {
        User newUser = User.createOAuthUser(
                userInfo.getEmail(),
                userInfo.getName(),
                provider,
                userInfo.getProviderId()
        );

        // 프로필 이미지가 있으면 설정
        if (userInfo.getProfileImageUrl() != null) {
            newUser.updateOAuthProfile(
                    userInfo.getName(),
                    userInfo.getProfileImageUrl()
            );
        }

        User savedUser = userRepository.save(newUser);

        log.info("신규 OAuth 사용자 생성: userId={}, email={}, provider={}",
                savedUser.getId(), savedUser.getEmail(), provider);

        return savedUser;
    }

}
