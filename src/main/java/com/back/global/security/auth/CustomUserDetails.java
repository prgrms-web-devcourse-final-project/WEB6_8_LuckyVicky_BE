package com.back.global.security.auth;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.Status;
import com.back.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Role currentRole;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TokenType을 Spring Security 권한으로 변환
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + currentRole.name())
        );
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Status.BLOCKED.equals(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Status.ACTIVE.equals(user.getStatus());
    }

}
