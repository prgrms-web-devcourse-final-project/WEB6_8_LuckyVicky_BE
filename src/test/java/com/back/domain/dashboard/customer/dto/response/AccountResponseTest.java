package com.back.domain.dashboard.customer.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * AccountResponse DTO 테스트
 * 계정 설정 구조와 핵심 비즈니스 로직에 집중
 * 2025.09.22 수정
 */
@DisplayName("AccountResponse DTO 테스트")
public class AccountResponseTest {

    @Test
    @DisplayName("전체 계정 설정 구조 생성 및 검증")
    void createCompleteSettings_Success() {
        // When
        AccountResponse.Settings settings = createSampleSettings();

        // Then - 전체 구조 검증
        assertAll(
                () -> assertThat(settings).isNotNull(),
                () -> assertThat(settings.getProfile()).isNotNull(),
                () -> assertThat(settings.getContact()).isNotNull(),
                () -> assertThat(settings.getSecurity()).isNotNull()
        );
    }

    @Test
    @DisplayName("계정 정보 일관성 검증")
    void validateAccountConsistency_Success() {
        // When
        AccountResponse.Settings settings = createSampleSettings();

        // Then - 비즈니스 로직 검증
        assertAll(
                // 프로필 정보 검증
                () -> assertThat(settings.getProfile().getUserId()).isPositive(),
                () -> assertThat(settings.getProfile().getNickname()).isNotBlank(),
                // 연락처 정보 검증
                () -> assertThat(settings.getContact().getEmail()).contains("@"),
                () -> assertThat(settings.getContact().getEmailVerified()).isNotNull(),
                () -> assertThat(settings.getContact().getPhone()).startsWith("+82"),
                // 보안 정보 검증
                () -> assertThat(settings.getSecurity().getLastPasswordChangedAt()).isBeforeOrEqualTo(LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("부분 정보 조회 구조 검증")
    void validatePartialSettings_Success() {
        // Given - 프로필만 있는 설정
        AccountResponse.Settings profileOnly = AccountResponse.Settings.builder()
                .profile(createSampleProfile())
                .build();

        // Given - 연락처만 있는 설정
        AccountResponse.Settings contactOnly = AccountResponse.Settings.builder()
                .contact(createSampleContact())
                .build();

        // Then
        assertAll(
                // 프로필만 조회시
                () -> assertThat(profileOnly.getProfile()).isNotNull(),
                () -> assertThat(profileOnly.getContact()).isNull(),
                () -> assertThat(profileOnly.getSecurity()).isNull(),
                // 연락처만 조회시
                () -> assertThat(contactOnly.getProfile()).isNull(),
                () -> assertThat(contactOnly.getContact()).isNotNull(),
                () -> assertThat(contactOnly.getSecurity()).isNull()
        );
    }

    @Test
    @DisplayName("API 명세와 일치하는 구조 생성")
    void createApiCompatibleStructure_Success() {
        // When
        AccountResponse.Settings response = AccountResponse.Settings.builder()
                .profile(AccountResponse.Profile.builder()
                        .userId(10025L)
                        .nickname("닉네임입니다")
                        .profileImageUrl("https://cdn.example.com/u/10025/profile.jpg")
                        .build())
                .contact(AccountResponse.Contact.builder()
                        .email("user@example.com")
                        .emailVerified(true)
                        .phone("+821012345678")
                        .address("서울특별시 강남구 테헤란로 123 2층")
                        .build())
                .security(AccountResponse.Security.builder()
                        .lastPasswordChangedAt(LocalDateTime.of(2025, 8, 10, 11, 0))
                        .build())
                .build();

        // Then - API 응답 구조 검증
        assertAll(
                () -> assertThat(response.getProfile().getUserId()).isEqualTo(10025L),
                () -> assertThat(response.getProfile().getNickname()).isEqualTo("닉네임입니다"),
                () -> assertThat(response.getContact().getEmail()).isEqualTo("user@example.com"),
                () -> assertThat(response.getContact().getEmailVerified()).isTrue(),
                () -> assertThat(response.getSecurity().getLastPasswordChangedAt()).isNotNull()
        );
    }

    // =========================== 헬퍼 메서드들 ===========================

    private AccountResponse.Settings createSampleSettings() {
        return AccountResponse.Settings.builder()
                .profile(createSampleProfile())
                .contact(createSampleContact())
                .security(createSampleSecurity())
                .build();
    }

    private AccountResponse.Profile createSampleProfile() {
        return AccountResponse.Profile.builder()
                .userId(123L)
                .nickname("테스트유저")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
    }

    private AccountResponse.Contact createSampleContact() {
        return AccountResponse.Contact.builder()
                .email("test@example.com")
                .emailVerified(true)
                .phone("+821012345678")
                .address("서울시 강남구")
                .build();
    }

    private AccountResponse.Security createSampleSecurity() {
        return AccountResponse.Security.builder()
                .lastPasswordChangedAt(LocalDateTime.of(2025, 9, 20, 10, 30))
                .build();
    }
}
