package com.back.global.security.oauth2;

import com.back.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring Security의 OAuth2User 인터페이스 구현
 * OAuth2 인증 정보를 Spring Security에 전달하는 역할
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    // Spring Security에서 사용자 식별용으로 사용
    @Override
    public String getName() {
        return user.getEmail();  // OAuth 사용자는 이메일로 식별
    }


    // ===== 편의 메서드 ===== //

    // 사용자 ID
    public Long getUserId() {
        return user.getId();
    }

    // 사용자 이메일
    public String getUserEmail() {
        return user.getEmail();
    }

    // 사용자 이름
    public String getUserName() {
        return user.getName();
    }

}
