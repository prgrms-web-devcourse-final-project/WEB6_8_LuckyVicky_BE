package com.back.domain.user.entity;

import java.util.List;

public enum Role {
    USER(1),       // 일반 사용자
    ARTIST(2),     // 작가
    ADMIN(3),      // 관리자
    ROOT(4);       // 최고 관리자

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    // 로그인 가능한 역할들 (역할별 정책)
    public List<Role> getAvailableRoles() {
        return switch (this) {
            case USER -> List.of(Role.USER);                               // USER만
            case ARTIST -> List.of(Role.USER, Role.ARTIST);                // USER, ARTIST
            case ADMIN -> List.of(Role.ADMIN);                             // ADMIN만
            case ROOT -> List.of(Role.USER, Role.ARTIST, Role.ADMIN, Role.ROOT); // 모든 권한
        };
    }
}
