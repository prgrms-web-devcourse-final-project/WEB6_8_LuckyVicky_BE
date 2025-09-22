package com.back.domain.dashboard.customer.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * AccountResponse DTO 테스트
 * Builder 패턴과 데이터 구조의 정확성을 검증
 *2025.09.22 수정
 */
@DisplayName("AccountResponse DTO 테스트")
public class AccountResponseTest {

    @Test
    @DisplayName("Builder 패턴으로 Settings 생성 테스트")
    void builder_Settings_Success() {
        // Given
        AccountResponse.Profile profile = AccountResponse.Profile.builder()
                .userId(123L)
                .nickname("테스트유저")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        AccountResponse.Contact contact = AccountResponse.Contact.builder()
                .email("test@example.com")
                .emailVerified(true)
                .phone("+821012345678")
                .address("서울시 강남구")
                .build();

        AccountResponse.Security security = AccountResponse.Security.builder()
                .lastPasswordChangedAt(LocalDateTime.of(2025, 9, 20, 10, 30))
                .build();

        // When
        AccountResponse.Settings settings = AccountResponse.Settings.builder()
                .profile(profile)
                .contact(contact)
                .security(security)
                .build();

        // Then
        assertAll(
                () -> assertThat(settings).isNotNull(),
                () -> assertThat(settings.getProfile()).isNotNull(),
                () -> assertThat(settings.getContact()).isNotNull(),
                () -> assertThat(settings.getSecurity()).isNotNull(),
                () -> assertThat(settings.getProfile().getUserId()).isEqualTo(123L),
                () -> assertThat(settings.getContact().getEmail()).isEqualTo("test@example.com"),
                () -> assertThat(settings.getSecurity().getLastPasswordChangedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("Profile Builder 패턴 테스트")
    void builder_Profile_Success() {
        // When
        AccountResponse.Profile profile = AccountResponse.Profile.builder()
                .userId(100L)
                .nickname("닉네임테스트")
                .profileImageUrl("https://cdn.test.com/image.jpg")
                .build();

        // Then
        assertAll(
                () -> assertThat(profile.getUserId()).isEqualTo(100L),
                () -> assertThat(profile.getNickname()).isEqualTo("닉네임테스트"),
                () -> assertThat(profile.getProfileImageUrl()).isEqualTo("https://cdn.test.com/image.jpg")
        );
    }

    @Test
    @DisplayName("Contact Builder 패턴 테스트")
    void builder_Contact_Success() {
        // When
        AccountResponse.Contact contact = AccountResponse.Contact.builder()
                .email("contact@test.com")
                .emailVerified(false)
                .phone("+821087654321")
                .address("부산시 해운대구")
                .build();

        // Then
        assertAll(
                () -> assertThat(contact.getEmail()).isEqualTo("contact@test.com"),
                () -> assertThat(contact.getEmailVerified()).isFalse(),
                () -> assertThat(contact.getPhone()).isEqualTo("+821087654321"),
                () -> assertThat(contact.getAddress()).isEqualTo("부산시 해운대구")
        );
    }

    @Test
    @DisplayName("API 명세와 동일한 구조 생성 테스트")
    void createApiResponseStructure() {
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

        // Then
        assertAll(
                () -> assertThat(response.getProfile().getUserId()).isEqualTo(10025L),
                () -> assertThat(response.getProfile().getNickname()).isEqualTo("닉네임입니다"),
                () -> assertThat(response.getContact().getEmail()).isEqualTo("user@example.com"),
                () -> assertThat(response.getContact().getEmailVerified()).isTrue(),
                () -> assertThat(response.getSecurity().getLastPasswordChangedAt()).isNotNull()
        );
    }
}
