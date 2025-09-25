package com.back.global.rq;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 요청 컨텍스트 헬퍼 클래스
 * 현재 로그인된 사용자 정보와 요청 관련 유틸리티 메서드를 제공합니다.
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest request;

    /**
     * 현재 로그인된 사용자의 User 객체를 반환합니다.
     * @return 현재 로그인된 사용자 객체, 로그인되지 않은 경우 null
     */
    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;

        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }


    /**
     * 현재 로그인된 사용자의 ID를 반환합니다.
     * @return 현재 로그인된 사용자의 ID, 로그인되지 않은 경우 null
     */
    public Long getUserId() {
        User user = getUser();
        return (user != null) ? user.getId() : null;
    }


    /**
     * 현재 로그인된 사용자의 역할을 반환합니다.
     * @return 현재 역할, 비로그인 상태면 null
     */
    public Role getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;

        return ((CustomUserDetails) auth.getPrincipal()).getUser().getRole();
    }


    /**
     * 현재 사용자의 로그인 상태 여부를 반환합니다.
     * @return 로그인 상태면 true, 아니면 false
     */
    public boolean isLogin() {
        return getUser() != null;
    }


    /**
     * 현재 사용자가 관리자 권한인지 확인합니다.
     * @return 관리자면 true, 아니면 false
     */
    public boolean isAdmin() {
        User user = getUser();
        return user != null && user.isAdmin();
    }


    /**
     * 현재 사용자가 아티스트 권한인지 확인합니다.
     * @return 아티스트면 true, 아니면 false
     */
    public boolean isArtist() {
        User user = getUser();
        return user != null && user.isArtist();
    }


    /**
     * 현재 사용자가 특정 사용자와 동일한지 확인합니다.
     * @param userId 비교할 사용자 ID
     * @return 동일하면 true, 아니면 false
     */
    public boolean isSameUser(Long userId) {
        User user = getUser();
        return user != null && user.isSameUser(userId);
    }


    /**
     * 현재 사용자가 특정 사용자와 동일한지 확인합니다.
     * @param user 비교할 사용자 객체
     * @return 동일하면 true, 아니면 false
     */
    public boolean isSameUser(User user) {
        User currentUser = getUser();
        return currentUser != null && currentUser.isSameUser(user);
    }


    /**
     * 현재 사용자가 특정 사용자의 리소스에 접근할 권한이 있는지 확인합니다.
     * @param resourceOwnerId 리소스 소유자 ID
     * @return 접근 권한이 있으면 true, 아니면 false
     */
    public boolean canAccessResource(Long resourceOwnerId) {
        User user = getUser();
        return isAdmin() || isSameUser(resourceOwnerId);
    }


    /**
     * 현재 사용자가 충분한 돈이 있는지 확인합니다.
     * @param amount 필요한 금액
     * @return 충분하면 true, 아니면 false
     */
    public boolean hasEnoughMoney(int amount) {
        User user = getUser();
        return user != null && user.hasEnoughMoney(amount);
    }


    /**
     * 로그인이 필요한 작업에 대한 예외를 던집니다.
     */
    public void requireLogin() {
        if (!isLogin()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
    }


    /**
     * 관리자 권한이 필요한 작업에 대한 예외를 던집니다.
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new IllegalStateException("관리자 권한이 필요합니다.");
        }
    }

}