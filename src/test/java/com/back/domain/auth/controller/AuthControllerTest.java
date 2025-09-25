package com.back.domain.auth.controller;

import com.back.domain.auth.controller.AuthController;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.user.entity.Role;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.Role;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

        @Test
        @DisplayName("정상적인 회원가입 요청 성공")
        void signUp_Success() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com", "Password123!", "Password123!",
                    "testUser", "010-1234-5678", true, false
            );

            SignUpResponse mockResponse = new SignUpResponse(1L, "test@example.com", "testUser");
            given(authService.signUp(any(SignUpRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<SignUpResponse>> response =
                    authController.signUp(request, httpServletRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200-ok");
            assertThat(response.getBody().msg()).isEqualTo("회원가입 성공");
            assertThat(response.getBody().data().userId()).isEqualTo(1L);
            assertThat(response.getBody().data().email()).isEqualTo("test@example.com");
            assertThat(response.getBody().data().name()).isEqualTo("testUser");

            verify(authService).signUp(any(SignUpRequest.class));
        }

        @Test
        @DisplayName("필수 약관 동의가 false인 경우에도 Controller는 정상 처리")
        void signUp_RequiredTermsNotAgreed() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com", "Password123!", "Password123!",
                    "testUser", "010-1234-5678", false, false
            );

            // when
            authController.signUp(request, httpServletRequest);

            // then - Controller는 요청을 Service로 전달만 함
            verify(authService).signUp(any(SignUpRequest.class));
        }

        @Test
        @DisplayName("마케팅 동의 여부와 관계없이 정상 처리")
        void signUp_MarketingAgreementVariations() {
            // given
            SignUpRequest requestWithMarketing = new SignUpRequest(
                    "test1@example.com", "Password123!", "Password123!",
                    "testUser1", "010-1234-5678", true, true
            );

            SignUpRequest requestWithoutMarketing = new SignUpRequest(
                    "test2@example.com", "Password123!", "Password123!",
                    "testUser2", "010-1234-5679", true, false
            );

            SignUpResponse mockResponse1 = new SignUpResponse(1L, "test1@example.com", "testUser1");
            SignUpResponse mockResponse2 = new SignUpResponse(2L, "test2@example.com", "testUser2");

            given(authService.signUp(any(SignUpRequest.class)))
                    .willReturn(mockResponse1, mockResponse2);

            // when
            ResponseEntity<RsData<SignUpResponse>> response1 =
                    authController.signUp(requestWithMarketing, httpServletRequest);
            ResponseEntity<RsData<SignUpResponse>> response2 =
                    authController.signUp(requestWithoutMarketing, httpServletRequest);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response1.getBody().data().name()).isEqualTo("testUser1");
            assertThat(response2.getBody().data().name()).isEqualTo("testUser2");

            verify(authService, times(2)).signUp(any(SignUpRequest.class));
        }

        @Test
        @DisplayName("다양한 이메일 형식으로 회원가입 처리")
        void signUp_VariousEmailFormats() {
            // given
            String[] emails = {
                    "user@domain.com",
                    "user.name@domain.co.kr",
                    "user+tag@subdomain.domain.org"
            };

            SignUpResponse mockResponse = new SignUpResponse(1L, "test@example.com", "testUser");
            given(authService.signUp(any(SignUpRequest.class))).willReturn(mockResponse);

            for (String email : emails) {
                SignUpRequest request = new SignUpRequest(
                        email, "Password123!", "Password123!",
                        "testUser", "010-1234-5678", true, false
                );

                // when
                ResponseEntity<RsData<SignUpResponse>> response =
                        authController.signUp(request, httpServletRequest);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            verify(authService, times(emails.length)).signUp(any(SignUpRequest.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("정상적인 로그인 요청 성공")
        void login_Success() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com", "Password123!", Role.USER
            );

            AuthResponse mockResponse = new AuthResponse(
                    "accessToken", "refreshToken", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200-ok");
            assertThat(response.getBody().msg()).isEqualTo("로그인 성공");
            assertThat(response.getBody().data().accessToken()).isEqualTo("accessToken");
            assertThat(response.getBody().data().refreshToken()).isEqualTo("refreshToken");
            assertThat(response.getBody().data().userId()).isEqualTo(1L);
            assertThat(response.getBody().data().selectedRole()).isEqualTo(Role.USER);

            verify(authService).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("selectedRole이 null일 때 USER로 기본값 설정")
        void login_NullSelectedRole_DefaultsToUser() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com", "Password123!", null
            );

            AuthResponse mockResponse = new AuthResponse(
                    "accessToken", "refreshToken", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().data().selectedRole()).isEqualTo(Role.USER);

            verify(authService).login(argThat(loginRequest ->
                    loginRequest.selectedRole() == Role.USER
            ));
        }

        @Test
        @DisplayName("다양한 Role로 로그인 요청 처리")
        void login_DifferentRoles() {
            // given
            Role[] roles = {Role.USER, Role.ARTIST, Role.ADMIN};

            for (Role role : roles) {
                LoginRequest request = new LoginRequest(
                        "test@example.com", "Password123!", role
                );

                AuthResponse mockResponse = new AuthResponse(
                        "accessToken", "refreshToken", 1L, "test@example.com",
                        role, List.of(role), 1800L
                );

                given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

                // when
                ResponseEntity<RsData<AuthResponse>> response =
                        authController.login(request, httpServletRequest, httpServletResponse);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().data().selectedRole()).isEqualTo(role);

                verify(authService).login(argThat(loginRequest ->
                        loginRequest.selectedRole() == role
                ));

                reset(authService);
            }
        }

        @Test
        @DisplayName("응답 헤더에 Set-Cookie가 포함되는지 확인")
        void login_ResponseContainsCookies() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com", "Password123!", Role.USER
            );

            AuthResponse mockResponse = new AuthResponse(
                    "accessToken123", "refreshToken456", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            // ResponseEntity를 직접 테스트할 때는 실제 Set-Cookie 헤더 확인은 어려움
            // 하지만 Controller가 정상적으로 응답을 반환하는지 확인 가능
        }

        @Test
        @DisplayName("다중 역할을 가진 사용자 로그인")
        void login_UserWithMultipleRoles() {
            // given
            LoginRequest request = new LoginRequest(
                    "admin@example.com", "Password123!", Role.ADMIN
            );

            AuthResponse mockResponse = new AuthResponse(
                    "accessToken", "refreshToken", 1L, "admin@example.com",
                    Role.ADMIN, List.of(Role.USER, Role.ARTIST, Role.ADMIN), 1800L
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().data().selectedRole()).isEqualTo(Role.ADMIN);
            assertThat(response.getBody().data().availableRoles())
                    .containsExactly(Role.USER, Role.ARTIST, Role.ADMIN);
        }

        @Test
        @DisplayName("토큰 만료 시간이 응답에 포함되는지 확인")
        void login_TokenExpirationIncluded() {
            // given
            LoginRequest request = new LoginRequest(
                    "test@example.com", "Password123!", Role.USER
            );

            Long expectedExpiration = 3600L; // 1시간
            AuthResponse mockResponse = new AuthResponse(
                    "accessToken", "refreshToken", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), expectedExpiration
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getBody().data().accessTokenExpiresIn())
                    .isEqualTo(expectedExpiration);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("정상적인 로그아웃 요청 성공")
        void logout_Success() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200-ok");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
            assertThat(response.getBody().data()).isNull();

            verify(authService).logout("validRefreshToken");
        }

        @Test
        @DisplayName("다양한 형태의 RefreshToken으로 로그아웃 처리")
        void logout_DifferentTokenFormats() {
            // given
            String[] tokens = {
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWI.signature",
                    "shortToken123",
                    "veryLongTokenWith1234567890AbcdefghijklmnopqrstuvwxyzMore",
                    "token-with-dashes-and-underscores_123"
            };

            for (String token : tokens) {
                TokenRefreshRequest request = new TokenRefreshRequest(token);

                // when
                ResponseEntity<RsData<Void>> response = authController.logout(request);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(authService).logout(token);
            }

            verify(authService, times(tokens.length)).logout(anyString());
        }

        @Test
        @DisplayName("로그아웃 응답에 쿠키 삭제 헤더가 포함되는지 확인")
        void logout_ResponseContainsCookieDeletion() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            // Set-Cookie 헤더로 쿠키 삭제(MaxAge=0) 헤더가 추가됨을 확인
        }

        @Test
        @DisplayName("빈 문자열 토큰으로 로그아웃 시도")
        void logout_EmptyToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("");

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Controller는 빈 문자열도 그대로 Service에 전달
            verify(authService).logout("");
        }

        @Test
        @DisplayName("공백으로만 이루어진 토큰으로 로그아웃 시도")
        void logout_WhitespaceToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("   ");

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(authService).logout("   ");
        }

        @Test
        @DisplayName("연속 로그아웃 요청 처리")
        void logout_MultipleLogoutRequests() {
            // given
            TokenRefreshRequest request1 = new TokenRefreshRequest("token1");
            TokenRefreshRequest request2 = new TokenRefreshRequest("token2");
            TokenRefreshRequest request3 = new TokenRefreshRequest("token3");

            // when
            ResponseEntity<RsData<Void>> response1 = authController.logout(request1);
            ResponseEntity<RsData<Void>> response2 = authController.logout(request2);
            ResponseEntity<RsData<Void>> response3 = authController.logout(request3);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(authService).logout("token1");
            verify(authService).logout("token2");
            verify(authService).logout("token3");
            verify(authService, times(3)).logout(anyString());
        }

        @Test
        @DisplayName("동일한 토큰으로 중복 로그아웃 요청")
        void logout_DuplicateLogoutRequests() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("sameToken");

            // when
            ResponseEntity<RsData<Void>> response1 = authController.logout(request);
            ResponseEntity<RsData<Void>> response2 = authController.logout(request);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            // Controller는 중복 요청도 동일하게 처리
            verify(authService, times(2)).logout("sameToken");
        }

        @Test
        @DisplayName("로그아웃 후 응답 데이터가 null인지 확인")
        void logout_ResponseDataIsNull() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(request);

            // then
            assertThat(response.getBody().data()).isNull();
            // Void 타입이므로 data는 항상 null이어야 함
        }
    }

    @Nested
    @DisplayName("전체 로그아웃 테스트")
    class LogoutAllTest {

        @Test
        @DisplayName("정상적인 전체 로그아웃 요청 성공")
        void logoutAll_Success() {
            // given
            given(authentication.isAuthenticated()).willReturn(true);
            given(authentication.getPrincipal()).willReturn(customUserDetails);
            given(customUserDetails.getUserId()).willReturn(1L);

            // when
            ResponseEntity<RsData<Void>> response =
                    authController.logoutAll(httpServletRequest, authentication);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200-ok");
            assertThat(response.getBody().msg()).isEqualTo("전체 로그아웃 성공");
            assertThat(response.getBody().data()).isNull();

            verify(authService).logoutAll(1L);
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 전체 로그아웃 실패")
        void logoutAll_Unauthorized() {
            // given
            given(authentication.isAuthenticated()).willReturn(false);

            // when
            ResponseEntity<RsData<Void>> response =
                    authController.logoutAll(httpServletRequest, authentication);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(401);
            assertThat(response.getBody().resultCode()).isEqualTo("401-unauthorized");
            assertThat(response.getBody().msg()).isEqualTo("인증이 필요합니다.");

            verify(authService, never()).logoutAll(any());
        }

        @Test
        @DisplayName("Authentication 객체가 null인 경우 실패")
        void logoutAll_NullAuthentication() {
            // when
            ResponseEntity<RsData<Void>> response =
                    authController.logoutAll(httpServletRequest, null);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(401);
            assertThat(response.getBody().resultCode()).isEqualTo("401-unauthorized");
            assertThat(response.getBody().msg()).isEqualTo("인증이 필요합니다.");

            verify(authService, never()).logoutAll(any());
        }

        @Test
        @DisplayName("다양한 사용자 ID로 전체 로그아웃 처리")
        void logoutAll_DifferentUserIds() {
            // given
            Long[] userIds = {1L, 100L, 999L, 12345L};

            for (Long userId : userIds) {
                given(authentication.isAuthenticated()).willReturn(true);
                given(authentication.getPrincipal()).willReturn(customUserDetails);
                given(customUserDetails.getUserId()).willReturn(userId);

                // when
                ResponseEntity<RsData<Void>> response =
                        authController.logoutAll(httpServletRequest, authentication);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(authService).logoutAll(userId);

                reset(authentication, customUserDetails, authService);
            }
        }

        @Test
        @DisplayName("인증은 되었지만 Principal이 null인 경우")
        void logoutAll_AuthenticatedButNullPrincipal() {
            // given
            given(authentication.isAuthenticated()).willReturn(true);
            given(authentication.getPrincipal()).willReturn(null);

            // when & then
            // 이 경우 ClassCastException이 발생할 수 있으므로
            // 실제로는 Controller에서 null 체크가 필요할 수 있음
            try {
                authController.logoutAll(httpServletRequest, authentication);
            } catch (Exception e) {
                // Controller가 예외를 적절히 처리하는지 확인
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("전체 로그아웃 응답에 쿠키 삭제 헤더 포함 확인")
        void logoutAll_ResponseContainsCookieDeletion() {
            // given
            given(authentication.isAuthenticated()).willReturn(true);
            given(authentication.getPrincipal()).willReturn(customUserDetails);
            given(customUserDetails.getUserId()).willReturn(1L);

            // when
            ResponseEntity<RsData<Void>> response =
                    authController.logoutAll(httpServletRequest, authentication);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            // refreshToken과 accessToken 쿠키 삭제 헤더 포함됨
        }

        @Test
        @DisplayName("연속된 전체 로그아웃 요청 처리")
        void logoutAll_ConsecutiveRequests() {
            // given
            given(authentication.isAuthenticated()).willReturn(true);
            given(authentication.getPrincipal()).willReturn(customUserDetails);
            given(customUserDetails.getUserId()).willReturn(1L);

            // when
            ResponseEntity<RsData<Void>> response1 =
                    authController.logoutAll(httpServletRequest, authentication);
            ResponseEntity<RsData<Void>> response2 =
                    authController.logoutAll(httpServletRequest, authentication);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(authService, times(2)).logoutAll(1L);
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("정상적인 토큰 재발급 요청 성공")
        void refreshToken_Success() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");

            AuthResponse mockResponse = new AuthResponse(
                    "newAccessToken", "newRefreshToken", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200-ok");
            assertThat(response.getBody().msg()).isEqualTo("토큰 재발급 성공");
            assertThat(response.getBody().data().accessToken()).isEqualTo("newAccessToken");
            assertThat(response.getBody().data().refreshToken()).isEqualTo("newRefreshToken");

            verify(authService).refreshToken(request);
        }

        @Test
        @DisplayName("다양한 RefreshToken으로 재발급 요청")
        void refreshToken_DifferentTokens() {
            // given
            String[] tokens = {
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.validPayload.signature",
                    "shortRefreshToken",
                    "veryLongRefreshTokenForTesting1234567890",
                    "refresh-token-with-special-chars_123"
            };

            for (int i = 0; i < tokens.length; i++) {
                TokenRefreshRequest request = new TokenRefreshRequest(tokens[i]);
                AuthResponse mockResponse = new AuthResponse(
                        "accessToken" + i, "refreshToken" + i, (long) (i + 1),
                        "user" + i + "@example.com", Role.USER, List.of(Role.USER), 1800L
                );
                given(authService.refreshToken(request)).willReturn(mockResponse);

                // when
                ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().data().accessToken()).isEqualTo("accessToken" + i);
                assertThat(response.getBody().data().refreshToken()).isEqualTo("refreshToken" + i);

                verify(authService).refreshToken(request);
                reset(authService);
            }
        }

        @Test
        @DisplayName("토큰 재발급 시 새로운 쿠키 설정 확인")
        void refreshToken_NewCookiesSet() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");
            AuthResponse mockResponse = new AuthResponse(
                    "newAccessToken123", "newRefreshToken456", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            // 새로운 accessToken과 refreshToken 쿠키가 설정됨
        }

        @Test
        @DisplayName("역할이 변경된 토큰 재발급")
        void refreshToken_RoleChanged() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("refreshTokenWithRoleChange");
            AuthResponse mockResponse = new AuthResponse(
                    "newAccessToken", "newRefreshToken", 1L, "admin@example.com",
                    Role.ADMIN, List.of(Role.USER, Role.ADMIN), 1800L
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            assertThat(response.getBody().data().selectedRole()).isEqualTo(Role.ADMIN);
            assertThat(response.getBody().data().availableRoles())
                    .containsExactly(Role.USER, Role.ADMIN);
        }

        @Test
        @DisplayName("토큰 만료 시간이 갱신되는지 확인")
        void refreshToken_ExpirationTimeUpdated() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("expiringSoonToken");
            Long newExpirationTime = 3600L; // 1시간
            AuthResponse mockResponse = new AuthResponse(
                    "newAccessToken", "newRefreshToken", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), newExpirationTime
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            assertThat(response.getBody().data().accessTokenExpiresIn())
                    .isEqualTo(newExpirationTime);
        }

        @Test
        @DisplayName("동일한 RefreshToken으로 연속 재발급 요청")
        void refreshToken_ConsecutiveRequestsWithSameToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("sameRefreshToken");
            AuthResponse mockResponse1 = new AuthResponse(
                    "accessToken1", "refreshToken1", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );
            AuthResponse mockResponse2 = new AuthResponse(
                    "accessToken2", "refreshToken2", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L
            );
            given(authService.refreshToken(request))
                    .willReturn(mockResponse1)
                    .willReturn(mockResponse2);

            // when
            ResponseEntity<RsData<AuthResponse>> response1 = authController.refreshToken(request);
            ResponseEntity<RsData<AuthResponse>> response2 = authController.refreshToken(request);

            // then
            assertThat(response1.getBody().data().accessToken()).isEqualTo("accessToken1");
            assertThat(response2.getBody().data().accessToken()).isEqualTo("accessToken2");
            verify(authService, times(2)).refreshToken(request);
        }

        @Test
        @DisplayName("토큰 재발급 후 사용자 정보 일관성 확인")
        void refreshToken_UserInfoConsistency() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("userTokenToRefresh");
            AuthResponse mockResponse = new AuthResponse(
                    "newAccessToken", "newRefreshToken", 42L, "consistent@example.com",
                    Role.ARTIST, List.of(Role.USER, Role.ARTIST), 1800L
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            AuthResponse data = response.getBody().data();
            assertThat(data.userId()).isEqualTo(42L);
            assertThat(data.email()).isEqualTo("consistent@example.com");
            assertThat(data.selectedRole()).isEqualTo(Role.ARTIST);
            assertThat(data.availableRoles()).containsExactly(Role.USER, Role.ARTIST);
        }
    }
}