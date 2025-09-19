package com.back.domain.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 계정 관련 응답 DTO
 */
public class AccountResponse {
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Settings {
        private Profile profile;
        private Contact contact;
        private Security security;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private Long userId;
        private String nickname;
        private String profileImageUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private String email;
        private Boolean emailVerified;
        private String phone;
        private String address;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Security {
        private LocalDateTime lastPasswordChangedAt;
    }
}
