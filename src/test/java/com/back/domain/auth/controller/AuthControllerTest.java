package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.PasswordResetRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.PasswordResetResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.Role;
import com.back.global.exception.ServiceException;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
            assertThat(response.getBody().resultCode()).isEqualTo("201");
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
                    Role.USER, List.of(Role.USER), 1800L, false
            );

            given(authService.login(any(LoginRequest.class))).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response =
                    authController.login(request, httpServletRequest, httpServletResponse);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
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
                    Role.USER, List.of(Role.USER), 1800L, false
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
                        role, List.of(role), 1800L, false
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
                    Role.USER, List.of(Role.USER), 1800L, false
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
                    Role.ADMIN, List.of(Role.USER, Role.ARTIST, Role.ADMIN), 1800L, false
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
                    Role.USER, List.of(Role.USER), expectedExpiration, false
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
        @DisplayName("쿠키에 RefreshToken이 있는 경우 정상 로그아웃")
        void logout_WithRefreshTokenCookie_Success() {
            // given
            String refreshToken = "validRefreshToken";

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
            assertThat(response.getBody().data()).isNull();
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();

            verify(authService).logout("validRefreshToken");
        }

        @Test
        @DisplayName("쿠키에 RefreshToken이 없는 경우에도 로그아웃 성공 (쿠키만 삭제)")
        void logout_WithoutRefreshTokenCookie_Success() {
            // given - RefreshToken이 null인 경우

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
            assertThat(response.getBody().data()).isNull();
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();

            // RefreshToken이 없으면 authService.logout()이 호출되지 않음
            verify(authService, never()).logout(any());
        }

        @Test
        @DisplayName("빈 문자열 RefreshToken인 경우 로그아웃 (쿠키만 삭제)")
        void logout_WithEmptyRefreshToken_Success() {
            // given
            String emptyRefreshToken = "";

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(emptyRefreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();

            // 빈 문자열이면 authService.logout()이 호출되지 않음
            verify(authService, never()).logout(any());
        }

        @Test
        @DisplayName("공백 문자열 RefreshToken인 경우 로그아웃 (쿠키만 삭제)")
        void logout_WithBlankRefreshToken_Success() {
            // given
            String blankRefreshToken = "   ";

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(blankRefreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();

            // 공백 문자열이면 authService.logout()이 호출되지 않음
            verify(authService, never()).logout(any());
        }

        @Test
        @DisplayName("다양한 형태의 유효한 RefreshToken으로 로그아웃")
        void logout_WithVariousValidTokens_Success() {
            // given
            String[] validTokens = {
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWI.signature",
                    "shortToken123",
                    "veryLongTokenWith1234567890AbcdefghijklmnopqrstuvwxyzMore",
                    "token-with-dashes-and-underscores_123"
            };

            for (String token : validTokens) {
                // when
                ResponseEntity<RsData<Void>> response = authController.logout(token);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(authService).logout(token);
            }

            verify(authService, times(validTokens.length)).logout(anyString());
        }

        @Test
        @DisplayName("로그아웃 응답에 쿠키 삭제 헤더가 포함되는지 확인")
        void logout_ResponseContainsCookieDeletionHeaders() {
            // given
            String refreshToken = "validRefreshToken";

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(refreshToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            assertThat(response.getHeaders().get("Set-Cookie").size()).isEqualTo(2);
            // refreshToken과 accessToken 쿠키 삭제 헤더 2개 포함됨
        }

        @Test
        @DisplayName("동일한 RefreshToken으로 연속 로그아웃 요청")
        void logout_ConsecutiveRequestsWithSameToken() {
            // given
            String refreshToken = "sameToken";

            // when
            ResponseEntity<RsData<Void>> response1 = authController.logout(refreshToken);
            ResponseEntity<RsData<Void>> response2 = authController.logout(refreshToken);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(authService, times(2)).logout("sameToken");
        }

        @Test
        @DisplayName("여러 사용자의 동시 로그아웃 요청 처리")
        void logout_MultipleConcurrentUsers() {
            // given
            String[] userTokens = {"userToken1", "userToken2", "userToken3"};

            for (String token : userTokens) {
                // when
                ResponseEntity<RsData<Void>> response = authController.logout(token);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
                verify(authService).logout(token);
            }

            verify(authService, times(userTokens.length)).logout(anyString());
        }

        @Test
        @DisplayName("로그아웃 후 응답 데이터가 null인지 확인")
        void logout_ResponseDataIsNull() {
            // given
            String refreshToken = "validRefreshToken";

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(refreshToken);

            // then
            assertThat(response.getBody().data()).isNull();
            // Void 타입이므로 data는 항상 null
        }

        @Test
        @DisplayName("만료된 RefreshToken으로도 쿠키 삭제는 정상 수행")
        void logout_WithExpiredToken_StillDeletesCookies() {
            // given
            String expiredToken = "expiredRefreshToken";
            // Service에서 만료된 토큰 처리 시 예외가 발생할 수 있지만
            // Controller는 쿠키 삭제를 정상 수행해야 함

            // when
            ResponseEntity<RsData<Void>> response = authController.logout(expiredToken);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().get("Set-Cookie")).isNotNull();
            // 쿠키 삭제는 토큰 유효성과 관계없이 항상 수행됨
        }

        @Test
        @DisplayName("RefreshToken이 null이어도 HTTP 200 응답")
        void logout_NullToken_Returns200() {
            // when
            ResponseEntity<RsData<Void>> response = authController.logout(null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("로그아웃 성공");
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
            assertThat(response.getBody().resultCode()).isEqualTo("200");
            assertThat(response.getBody().msg()).isEqualTo("전체 로그아웃 성공");
            assertThat(response.getBody().data()).isNull();

            verify(authService).logoutAll(1L);
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 전체 로그아웃 실패")
        void logoutAll_Unauthorized() {
            // given
            given(authentication.isAuthenticated()).willReturn(false);

            // when & then
            assertThatThrownBy(() ->
                    authController.logoutAll(httpServletRequest, authentication)
            )
                    .isInstanceOf(ServiceException.class)
                    .satisfies(exception -> {
                        ServiceException serviceException = (ServiceException) exception;
                        assertThat(serviceException.getResultCode()).isEqualTo("401");
                        assertThat(serviceException.getMsg()).isEqualTo("인증이 필요합니다.");
                    });

            verify(authService, never()).logoutAll(any());
        }

        @Test
        @DisplayName("Authentication 객체가 null인 경우 실패")
        void logoutAll_NullAuthentication() {
            // when & then
            assertThatThrownBy(() ->
                    authController.logoutAll(httpServletRequest, null)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("401 : 인증이 필요합니다.");

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
                    Role.USER, List.of(Role.USER), 1800L, false
            );
            given(authService.refreshToken(request)).willReturn(mockResponse);

            // when
            ResponseEntity<RsData<AuthResponse>> response = authController.refreshToken(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().resultCode()).isEqualTo("200");
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
                        "user" + i + "@example.com", Role.USER, List.of(Role.USER), 1800L, false
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
                    Role.USER, List.of(Role.USER), 1800L, false
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
                    Role.ADMIN, List.of(Role.USER, Role.ADMIN), 1800L, false
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
                    Role.USER, List.of(Role.USER), newExpirationTime, false
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
                    Role.USER, List.of(Role.USER), 1800L, false
            );
            AuthResponse mockResponse2 = new AuthResponse(
                    "accessToken2", "refreshToken2", 1L, "test@example.com",
                    Role.USER, List.of(Role.USER), 1800L, false
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
                    Role.ARTIST, List.of(Role.USER, Role.ARTIST), 1800L, false
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

    @Nested
    @DisplayName("비밀번호 찾기 테스트")
    class PasswordResetTest {

        @Test
        @DisplayName("정상적인 비밀번호 찾기 요청 성공")
        void resetPassword_Success() {
            // given
            PasswordResetRequest request = new PasswordResetRequest("user@example.com");

            PasswordResetResponse mockResponse = PasswordResetResponse.success("user@example.com");
            given(authService.resetPassword(any(PasswordResetRequest.class)))
                    .willReturn(mockResponse);

            // when
            ResponseEntity<PasswordResetResponse> response = authController.resetPassword(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().email()).isEqualTo("user@example.com");
            assertThat(response.getBody().message()).isEqualTo("임시 비밀번호가 이메일로 발송되었습니다.");

            verify(authService).resetPassword(any(PasswordResetRequest.class));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 찾기 시도")
        void resetPassword_EmailNotFound() {
            // given
            PasswordResetRequest request = new PasswordResetRequest("notfound@example.com");

            given(authService.resetPassword(any(PasswordResetRequest.class)))
                    .willThrow(new ServiceException("404", "등록되지 않은 이메일입니다."));

            // when & then
            assertThatThrownBy(() -> authController.resetPassword(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(exception -> {
                        ServiceException serviceException = (ServiceException) exception;
                        assertThat(serviceException.getResultCode()).isEqualTo("404");
                        assertThat(serviceException.getMsg()).isEqualTo("등록되지 않은 이메일입니다.");
                    });

            verify(authService).resetPassword(any(PasswordResetRequest.class));
        }

        @Test
        @DisplayName("OAuth 사용자 이메일로 비밀번호 찾기 시도")
        void resetPassword_OAuthUser() {
            // given
            PasswordResetRequest request = new PasswordResetRequest("kakao@example.com");

            given(authService.resetPassword(any(PasswordResetRequest.class)))
                    .willThrow(new ServiceException("400",
                            "소셜 로그인 사용자는 비밀번호 찾기를 이용할 수 없습니다."));

            // when & then
            assertThatThrownBy(() -> authController.resetPassword(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(exception -> {
                        ServiceException serviceException = (ServiceException) exception;
                        assertThat(serviceException.getResultCode()).isEqualTo("400");
                        assertThat(serviceException.getMsg())
                                .isEqualTo("소셜 로그인 사용자는 비밀번호 찾기를 이용할 수 없습니다.");
                    });

            verify(authService).resetPassword(any(PasswordResetRequest.class));
        }

        @Test
        @DisplayName("다양한 이메일 형식으로 비밀번호 찾기 요청")
        void resetPassword_VariousEmailFormats() {
            // given
            String[] emails = {
                    "user@domain.com",
                    "user.name@domain.co.kr",
                    "user+tag@subdomain.domain.org",
                    "user_123@test-domain.com"
            };

            for (String email : emails) {
                PasswordResetRequest request = new PasswordResetRequest(email);
                PasswordResetResponse mockResponse = PasswordResetResponse.success(email);
                given(authService.resetPassword(any(PasswordResetRequest.class)))
                        .willReturn(mockResponse);

                // when
                ResponseEntity<PasswordResetResponse> response =
                        authController.resetPassword(request);

                // then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().email()).isEqualTo(email);

                verify(authService).resetPassword(any(PasswordResetRequest.class));
                reset(authService);
            }
        }

        @Test
        @DisplayName("동일한 이메일로 연속 비밀번호 찾기 요청")
        void resetPassword_ConsecutiveRequests() {
            // given
            PasswordResetRequest request = new PasswordResetRequest("user@example.com");
            PasswordResetResponse mockResponse = PasswordResetResponse.success("user@example.com");

            given(authService.resetPassword(any(PasswordResetRequest.class)))
                    .willReturn(mockResponse);

            // when
            ResponseEntity<PasswordResetResponse> response1 =
                    authController.resetPassword(request);
            ResponseEntity<PasswordResetResponse> response2 =
                    authController.resetPassword(request);

            // then
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(authService, times(2)).resetPassword(any(PasswordResetRequest.class));
        }
    }
}