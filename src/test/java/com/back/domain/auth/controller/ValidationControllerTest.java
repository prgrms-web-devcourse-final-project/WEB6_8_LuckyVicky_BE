package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.ValidationCheckRequest;
import com.back.domain.auth.dto.response.ValidationCheckResponse;
import com.back.domain.auth.service.ValidationService;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationController 단위 테스트")
class ValidationControllerTest {

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ValidationController validationController;

    @Nested
    @DisplayName("이메일 중복 검사 테스트")
    class CheckEmailDuplicateTest {

        @Test
        @DisplayName("성공 - 사용 가능한 이메일")
        void checkEmailDuplicate_Available_Success() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("test@example.com");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "test@example.com", "email", false, "사용 가능한 이메일입니다."
            );

            given(validationService.checkEmailDuplication("test@example.com")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkEmailDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("이메일 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("test@example.com");
            assertThat(response.getBody().data().fieldType()).isEqualTo("email");
            assertThat(response.getBody().data().isDuplicate()).isFalse();
            assertThat(response.getBody().data().isAvailable()).isTrue();
            assertThat(response.getBody().data().message()).isEqualTo("사용 가능한 이메일입니다.");

            verify(validationService).checkEmailDuplication("test@example.com");
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 이메일")
        void checkEmailDuplicate_Duplicate_Fail() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("duplicate@example.com");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "duplicate@example.com", "email", true, "이미 사용 중인 이메일입니다."
            );

            given(validationService.checkEmailDuplication("duplicate@example.com")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkEmailDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("이메일 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("duplicate@example.com");
            assertThat(response.getBody().data().fieldType()).isEqualTo("email");
            assertThat(response.getBody().data().isDuplicate()).isTrue();
            assertThat(response.getBody().data().isAvailable()).isFalse();
            assertThat(response.getBody().data().message()).isEqualTo("이미 사용 중인 이메일입니다.");

            verify(validationService).checkEmailDuplication("duplicate@example.com");
        }
    }

    @Nested
    @DisplayName("닉네임 중복 검사 테스트")
    class CheckNameDuplicateTest {

        @Test
        @DisplayName("성공 - 사용 가능한 닉네임")
        void checkNameDuplicate_Available_Success() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("testuser");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "testuser", "name", false, "사용 가능한 닉네임입니다."
            );

            given(validationService.checkNameDuplicate("testuser")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkNameDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("닉네임 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("testuser");
            assertThat(response.getBody().data().fieldType()).isEqualTo("name");
            assertThat(response.getBody().data().isDuplicate()).isFalse();
            assertThat(response.getBody().data().isAvailable()).isTrue();
            assertThat(response.getBody().data().message()).isEqualTo("사용 가능한 닉네임입니다.");

            verify(validationService).checkNameDuplicate("testuser");
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 닉네임")
        void checkNameDuplicate_Duplicate_Fail() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("duplicateuser");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "duplicateuser", "name", true, "이미 사용 중인 닉네임입니다."
            );

            given(validationService.checkNameDuplicate("duplicateuser")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkNameDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("닉네임 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("duplicateuser");
            assertThat(response.getBody().data().fieldType()).isEqualTo("name");
            assertThat(response.getBody().data().isDuplicate()).isTrue();
            assertThat(response.getBody().data().isAvailable()).isFalse();
            assertThat(response.getBody().data().message()).isEqualTo("이미 사용 중인 닉네임입니다.");

            verify(validationService).checkNameDuplicate("duplicateuser");
        }
    }

    @Nested
    @DisplayName("전화번호 중복 검사 테스트")
    class CheckPhoneDuplicateTest {

        @Test
        @DisplayName("성공 - 사용 가능한 전화번호")
        void checkPhoneDuplicate_Available_Success() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("01012345678");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "01012345678", "phone", false, "사용 가능한 전화번호입니다."
            );

            given(validationService.checkPhoneDuplicate("01012345678")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkPhoneDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("전화번호 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("01012345678");
            assertThat(response.getBody().data().fieldType()).isEqualTo("phone");
            assertThat(response.getBody().data().isDuplicate()).isFalse();
            assertThat(response.getBody().data().isAvailable()).isTrue();
            assertThat(response.getBody().data().message()).isEqualTo("사용 가능한 전화번호입니다.");

            verify(validationService).checkPhoneDuplicate("01012345678");
        }

        @Test
        @DisplayName("실패 - 이미 사용 중인 전화번호")
        void checkPhoneDuplicate_Duplicate_Fail() {
            // given
            ValidationCheckRequest request = new ValidationCheckRequest("01087654321");
            ValidationCheckResponse mockResponse = ValidationCheckResponse.of(
                    "01087654321", "phone", true, "이미 사용 중인 전화번호입니다."
            );

            given(validationService.checkPhoneDuplicate("01087654321")).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<ValidationCheckResponse>> response =
                    validationController.checkPhoneDuplicate(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("전화번호 중복 검사 완료");
            assertThat(response.getBody().data().value()).isEqualTo("01087654321");
            assertThat(response.getBody().data().fieldType()).isEqualTo("phone");
            assertThat(response.getBody().data().isDuplicate()).isTrue();
            assertThat(response.getBody().data().isAvailable()).isFalse();
            assertThat(response.getBody().data().message()).isEqualTo("이미 사용 중인 전화번호입니다.");

            verify(validationService).checkPhoneDuplicate("01087654321");
        }
    }
}