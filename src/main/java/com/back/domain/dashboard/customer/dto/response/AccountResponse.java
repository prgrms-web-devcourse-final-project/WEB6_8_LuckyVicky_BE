package com.back.domain.dashboard.customer.dto.response;

import java.time.LocalDateTime;

/**
 * 계정 관련 응답 DTO
 * 사용자의 계정 설정 정보를 조회할 때 사용되는 응답 객체
 * include 파라미터에 따라 profile, contact, security 정보를 선택적으로 포함할 수 있음.
 * 2025.09.22 수정.
 */
public class AccountResponse {
    
    /**
     * 계정 설정 전체 정보
     * profile, contact, security 정보를 포함하는 최상위 응답 객체
     */
    public record Settings(
            /** 프로필 정보 */
            Profile profile,
            /** 연락처 정보 */
            Contact contact,
            /** 보안 정보 */
            Security security
    ) {}
    
    /**
     * 사용자 프로필 정보
     * 사용자의 기본 프로필 정보(닉네임, 프로필 이미지 등)를 포함
     */
    public record Profile(
            /** 사용자 ID */
            Long userId,
            /** 닉네임 */
            String nickname,
            /** 프로필 이미지 URL */
            String profileImageUrl
    ) {}
    
    /**
     * 사용자 연락처 정보
     * 이메일, 전화번호, 주소 등의 연락처 관련 정보를 포함
     */
    public record Contact(
            /** 이메일 주소 */
            String email,
            /** 이메일 인증 여부 */
            Boolean emailVerified,
            /** 전화번호 */
            String phone,
            /** 주소 */
            String address
    ) {}
    
    /**
     * 보안 관련 정보
     * 비밀번호 변경 이력 등의 보안 관련 정보를 포함
     */
    public record Security(
            /** 마지막 비밀번호 변경 일시 */
            LocalDateTime lastPasswordChangedAt
    ) {}
}
