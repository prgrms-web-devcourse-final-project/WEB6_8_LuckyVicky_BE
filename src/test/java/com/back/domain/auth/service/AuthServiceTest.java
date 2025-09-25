package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.entity.UserToken;
import com.back.domain.auth.repository.UserTokenRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserTokenRepository userTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 1800000L);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {
        private SignUpRequest validSignUpRequest;

        @BeforeEach
        void setUp() {
            validSignUpRequest = new SignUpRequest(
                    "test@example.com", "password123!", "password123!",
                    "testUser", "010-1234-5678", true, false
            );
        }

        @Test
        @DisplayName("정상적인 회원가입 성공")
        void signUp_Success() {
            // given
            given(userRepository.existsByEmail(validSignUpRequest.email())).willReturn(false);
            given(userRepository.existsByName(validSignUpRequest.name())).willReturn(false);
            given(userRepository.existsByPhone(validSignUpRequest.phone())).willReturn(false);
            given(passwordEncoder.encode(validSignUpRequest.password())).willReturn("encodedPassword");
            
            User savedUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            SignUpResponse response = authService.signUp(validSignUpRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(validSignUpRequest.email());
            assertThat(response.name()).isEqualTo(validSignUpRequest.name());

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 실패")
        void signUp_EmailAlreadyExists() {
            // given
            given(userRepository.existsByEmail(validSignUpRequest.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(validSignUpRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("409");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 사용 중인 이메일입니다.");
                    });
        }

        @Test
        @DisplayName("비밀번호 불일치로 회원가입 실패")
        void signUp_PasswordMismatch() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com", "password123!", "differentPassword!",
                    "testUser", "010-1234-5678", true, false
            );

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("비밀번호가 일치하지 않습니다.");
                    });
        }

        @Test
        @DisplayName("필수 약관 미동의로 회원가입 실패")
        void signUp_RequiredTermsNotAgreed() {
            // given
            SignUpRequest request = new SignUpRequest(
                    "test@example.com", "password123!", "password123!",
                    "testUser", "010-1234-5678", false, false
            );

            // when & then
            assertThatThrownBy(() -> authService.signUp(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("필수 약관에 동의해야 합니다.");
                    });
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {
        @Test
        @DisplayName("정상적인 로그인 성공")
        void login_Success() {
            // given
            LoginRequest loginRequest = new LoginRequest("test@example.com", "password123!", Role.USER);
            User testUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(testUser, "id", 1L);

            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.password(), testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getEmail(), Role.USER)).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(testUser.getId(), Role.USER)).willReturn("refreshToken");
            given(userTokenRepository.save(any(UserToken.class))).willReturn(any(UserToken.class));

            // when
            AuthResponse response = authService.login(loginRequest);

            // then
            assertThat(response.accessToken()).isEqualTo("accessToken");
            assertThat(response.refreshToken()).isEqualTo("refreshToken");
            assertThat(response.selectedRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 실패")
        void login_UserNotFound() {
            // given
            LoginRequest loginRequest = new LoginRequest("test@example.com", "password123!", Role.USER);
            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("401");
                        assertThat(serviceEx.getMsg()).isEqualTo("이메일 또는 비밀번호가 잘못되었습니다.");
                    });
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void login_WrongPassword() {
            // given
            LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword!", Role.USER);
            User testUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(testUser, "id", 1L);

            given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(loginRequest.password(), testUser.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("401");
                        assertThat(serviceEx.getMsg()).isEqualTo("이메일 또는 비밀번호가 잘못되었습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {
        @Test
        @DisplayName("정상적인 로그아웃 성공")
        void logout_Success() {
            // given
            String refreshToken = "validRefreshToken";
            User testUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(testUser, "id", 1L);

            UserToken userToken = UserToken.createRefreshToken(
                    testUser,
                    refreshToken,
                    LocalDateTime.now().plusDays(7),
                    Role.USER
            );

            given(userTokenRepository.findByRefreshToken(refreshToken))
                    .willReturn(Optional.of(userToken));

            // when
            authService.logout(refreshToken);

            // then
            verify(userTokenRepository).findByRefreshToken(refreshToken);
            verify(userTokenRepository).delete(userToken);
        }

        @Test
        @DisplayName("존재하지 않는 토큰으로 로그아웃 시도")
        void logout_TokenNotFound() {
            // given
            String refreshToken = "nonExistentToken";
            given(userTokenRepository.findByRefreshToken(refreshToken))
                    .willReturn(Optional.empty());

            // when
            authService.logout(refreshToken);

            // then
            verify(userTokenRepository).findByRefreshToken(refreshToken);
            verify(userTokenRepository, never()).delete(any(UserToken.class));
            // 예외가 발생하지 않고 조용히 처리됨을 검증
        }

        @Test
        @DisplayName("전체 로그아웃 성공")
        void logoutAll_Success() {
            // given
            Long userId = 1L;

            // when
            authService.logoutAll(userId);

            // then
            verify(userTokenRepository).deleteAllRefreshTokenByUserId(userId);
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class RefreshTokenTest {

        @Test
        @DisplayName("정상적인 토큰 재발급 성공")
        void refreshToken_Success() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("validRefreshToken");
            User testUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(testUser, "id", 1L);

            UserToken userToken = UserToken.createRefreshToken(
                    testUser,
                    "validRefreshToken",
                    LocalDateTime.now().plusDays(7),
                    Role.USER
            );

            given(userTokenRepository.findByRefreshTokenAndIsActiveTrue(request.refreshToken()))
                    .willReturn(Optional.of(userToken));
            given(jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getEmail(), Role.USER))
                    .willReturn("newAccessToken");
            given(jwtTokenProvider.createRefreshToken(testUser.getId(), Role.USER))
                    .willReturn("newRefreshToken");
            given(userTokenRepository.save(any(UserToken.class))).willReturn(any(UserToken.class));

            // when
            AuthResponse response = authService.refreshToken(request);

            // then
            assertThat(response.accessToken()).isEqualTo("newAccessToken");
            assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
            assertThat(response.selectedRole()).isEqualTo(Role.USER);
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("test@example.com");

            verify(userTokenRepository).save(any(UserToken.class));
        }

        @Test
        @DisplayName("유효하지 않은 RefreshToken으로 재발급 실패")
        void refreshToken_InvalidToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("invalidRefreshToken");
            given(userTokenRepository.findByRefreshTokenAndIsActiveTrue(request.refreshToken()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("401");
                        assertThat(serviceEx.getMsg()).isEqualTo("유효하지 않은 RefreshToken 입니다.");
                    });
        }

        @Test
        @DisplayName("만료된 RefreshToken으로 재발급 실패")
        void refreshToken_ExpiredToken() {
            // given
            TokenRefreshRequest request = new TokenRefreshRequest("expiredRefreshToken");
            User testUser = User.createLocalUser("test@example.com", "encodedPassword", "testUser", "010-1234-5678");
            ReflectionTestUtils.setField(testUser, "id", 1L);

            // 만료된 토큰 생성 (과거 시간으로 설정)
            UserToken expiredToken = UserToken.createRefreshToken(
                    testUser,
                    "expiredRefreshToken",
                    LocalDateTime.now().minusDays(1), // 어제 만료
                    Role.USER
            );

            given(userTokenRepository.findByRefreshTokenAndIsActiveTrue(request.refreshToken()))
                    .willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("401");
                        assertThat(serviceEx.getMsg()).isEqualTo("RefreshToken이 만료되었습니다.");
                    });

            // 만료된 토큰이 삭제되는지 확인
            verify(userTokenRepository).delete(expiredToken);
        }
    }
}