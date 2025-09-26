package com.back.domain.auth.service;


import com.back.domain.auth.dto.response.ValidationCheckResponse;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationService 테스트")
class ValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ValidationService validationService;

    @Nested
    @DisplayName("이메일 중복 검사")
    class CheckEmailDuplication {

        @Test
        @DisplayName("성공 - 사용 가능한 이메일")
        void checkEmailDuplication_Available_Success() {
            // given
            String email = "test@example.com";
            given(userRepository.existsByEmail(email)).willReturn(false);

            // when
            ValidationCheckResponse response = validationService.checkEmailDuplication(email);

            // then
            assertThat(response.value()).isEqualTo(email);
            assertThat(response.fieldType()).isEqualTo("email");
            assertThat(response.isDuplicate()).isFalse();
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.message()).isEqualTo("사용 가능한 이메일입니다.");

            verify(userRepository).existsByEmail(email);
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 이메일")
        void checkEmailDuplication_Duplicate_Fail() {
            // given
            String email = "duplicate@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);

            // when
            ValidationCheckResponse response = validationService.checkEmailDuplication(email);

            // then
            assertThat(response.value()).isEqualTo(email);
            assertThat(response.fieldType()).isEqualTo("email");
            assertThat(response.isDuplicate()).isTrue();
            assertThat(response.isAvailable()).isFalse();
            assertThat(response.message()).isEqualTo("이미 사용 중인 이메일입니다.");

            verify(userRepository).existsByEmail(email);
        }
    }

    @Nested
    @DisplayName("닉네임 중복 검사")
    class CheckNameDuplicate {

        @Test
        @DisplayName("성공 - 사용 가능한 닉네임")
        void checkNameDuplicate_Available_Success() {
            // given
            String name = "testuser";
            given(userRepository.existsByName(name)).willReturn(false);

            // when
            ValidationCheckResponse response = validationService.checkNameDuplicate(name);

            // then
            assertThat(response.value()).isEqualTo(name);
            assertThat(response.fieldType()).isEqualTo("name");
            assertThat(response.isDuplicate()).isFalse();
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.message()).isEqualTo("사용 가능한 닉네임입니다.");

            verify(userRepository).existsByName(name);
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 닉네임")
        void checkNameDuplicate_Duplicate_Fail() {
            // given
            String name = "duplicateuser";
            given(userRepository.existsByName(name)).willReturn(true);

            // when
            ValidationCheckResponse response = validationService.checkNameDuplicate(name);

            // then
            assertThat(response.value()).isEqualTo(name);
            assertThat(response.fieldType()).isEqualTo("name");
            assertThat(response.isDuplicate()).isTrue();
            assertThat(response.isAvailable()).isFalse();
            assertThat(response.message()).isEqualTo("이미 사용 중인 닉네임입니다.");

            verify(userRepository).existsByName(name);
        }
    }

    @Nested
    @DisplayName("전화번호 중복 검사")
    class CheckPhoneDuplicate {

        @Test
        @DisplayName("성공 - 사용 가능한 전화번호")
        void checkPhoneDuplicate_Available_Success() {
            // given
            String phone = "01012345678";
            given(userRepository.existsByPhone(phone)).willReturn(false);

            // when
            ValidationCheckResponse response = validationService.checkPhoneDuplicate(phone);

            // then
            assertThat(response.value()).isEqualTo(phone);
            assertThat(response.fieldType()).isEqualTo("phone");
            assertThat(response.isDuplicate()).isFalse();
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.message()).isEqualTo("사용 가능한 전화번호입니다.");

            verify(userRepository).existsByPhone(phone);
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 전화번호")
        void checkPhoneDuplicate_Duplicate_Fail() {
            // given
            String phone = "01087654321";
            given(userRepository.existsByPhone(phone)).willReturn(true);

            // when
            ValidationCheckResponse response = validationService.checkPhoneDuplicate(phone);

            // then
            assertThat(response.value()).isEqualTo(phone);
            assertThat(response.fieldType()).isEqualTo("phone");
            assertThat(response.isDuplicate()).isTrue();
            assertThat(response.isAvailable()).isFalse();
            assertThat(response.message()).isEqualTo("이미 사용 중인 전화번호입니다.");

            verify(userRepository).existsByPhone(phone);
        }
    }
}