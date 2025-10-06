package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.entity.UserToken;
import com.back.domain.auth.repository.UserTokenRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 회원가입
     */
    public SignUpResponse signUp(SignUpRequest request) {
        // 검증을 모두 통과해야 회원가입 진행
        validateSignUpRequest(request);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 사용자 생성 및 저장
        User user = createAndSaveUser(request, encodedPassword);

        log.info("회원가입 성공: userId={}, email={}", user.getId(), user.getEmail());

        return new SignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    /**
     * 로그인
     */
    public AuthResponse login(LoginRequest request) {
        // 검증을 모두 통과해야 로그인 진행
        User user = validateLoginCredentials(request);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                request.selectedRole()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                request.selectedRole()
        );

        // 새 RefreshToken 저장
        UserToken userToken = UserToken.createRefreshToken(
                user,
                refreshToken,
                LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000),
                request.selectedRole()
        );
        userTokenRepository.save(userToken);

        log.info("로그인 성공: userId={}, email={}, selectedRole={}",
                user.getId(), user.getEmail(), request.selectedRole());

        return AuthResponse.loginSuccess(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                request.selectedRole(),
                user.getAvailableLoginRoles(),
                accessTokenExpiration / 1000
        );
    }

    /**
     * RefreshToken 재발급
     */
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        // RefreshToken 검증
        UserToken userToken = validateAndGetRefreshToken(request.refreshToken());
        User user = userToken.getUser();

        // 새로운 AccessToken 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                userToken.getLoginRole()
        );

        // 새로운 RefreshToken 생성
        String newRefreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                userToken.getLoginRole()
        );

        // 기존 토큰 즉시 삭제
        userTokenRepository.delete(userToken);

        // 새 RefreshToken 저장
        UserToken newUserToken = UserToken.createRefreshToken(
                user,
                newRefreshToken,
                LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000),
                userToken.getLoginRole()
        );
        userTokenRepository.save(newUserToken);

        log.info("토큰 갱신 성공: userId={}, role={}", user.getId(), userToken.getLoginRole());

        return AuthResponse.tokenRefreshSuccess(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getEmail(),
                userToken.getLoginRole(),
                user.getAvailableLoginRoles(),
                accessTokenExpiration / 1000
        );
    }

    /**
     * 로그아웃 - 토큰 삭제
     */
    public void logout(String refreshToken) {
        userTokenRepository.findByRefreshToken(refreshToken)
                .ifPresentOrElse(
                        token -> {
                            userTokenRepository.delete(token);
                            log.info("로그아웃 성공: userId={}", token.getUser().getId());
                        },
                        () -> log.warn("존재하지 않는 토큰으로 로그아웃 시도")
                );
    }

    /**
     * 전체 로그아웃 (모든 기기에서 로그아웃) - 토큰 삭제
     */
    public void logoutAll(Long userId) {
        int deletedCount = userTokenRepository.deleteAllRefreshTokenByUserId(userId);

        if (deletedCount > 0) {
            log.info("전체 로그아웃 성공: userId={}, 삭제된 토큰 수={}", userId, deletedCount);
        } else {
            log.info("전체 로그아웃: userId={}, 삭제할 토큰 없음", userId);
        }
    }

    // ===== Private Helper Methods =====

    /**
     * 회원가입 요청에 대한 모든 검증을 수행합니다.
     *
     * @param request 회원가입 요청 정보
     * @throws ServiceException 검증 실패 시 발생
     */
    private void validateSignUpRequest(SignUpRequest request) {
        validateInput(request);
        // 중복 검증은 이제 ValidationService에서 처리하지만, 일단 회원가입시 최종 검증을 위해 체크
        validateDuplication(request);
    }

    /**
     * 입력값 검증 (비밀번호 일치, 약관 동의)
     */
    private void validateInput(SignUpRequest request) {
        if (!request.isPasswordMatching()) {
            throw new ServiceException("400", "비밀번호가 일치하지 않습니다.");
        }

        if (!request.isRequiredTermsAgreed()) {
            throw new ServiceException("400", "필수 약관에 동의해야 합니다.");
        }
    }

    /**
     * 중복 검증 (이메일, 닉네임, 전화번호)
     */
    private void validateDuplication(SignUpRequest request) {
        validateFieldDuplication("email", request.email(),
                () -> userRepository.existsByEmail(request.email()),
                "409", "이미 사용 중인 이메일입니다.");

        validateFieldDuplication("name", request.name(),
                () -> userRepository.existsByName(request.name()),
                "409", "이미 사용 중인 닉네임입니다.");

        validateFieldDuplication("phone", request.phone(),
                () -> userRepository.existsByPhone(request.phone()),
                "409", "이미 사용 중인 전화번호입니다.");
    }

    /**
     * 필드 중복 검증 공통 메서드
     */
    private void validateFieldDuplication(String fieldName, String value,
                                          java.util.function.Supplier<Boolean> duplicateChecker,
                                          String errorCode, String errorMessage) {
        if (duplicateChecker.get()) {
            throw new ServiceException(errorCode, errorMessage);
        }
    }

    /**
     * 사용자 생성 및 저장
     */
    private User createAndSaveUser(SignUpRequest request, String encodedPassword) {
        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone()
        );
        return userRepository.save(user);
    }

    /**
     * 로그인 자격 증명 검증
     */
    private User validateLoginCredentials(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", request.email());
                    return new ServiceException("401", "이메일이 잘못되었습니다.");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", request.email());
            throw new ServiceException("401", "비밀번호가 잘못되었습니다.");
        }

        if (!user.getRole().canLoginAs(request.selectedRole())) {
            log.warn("로그인 실패 - 권한 불일치: email={}, requestedRole={}, userRole={}",
                    request.email(), request.selectedRole(), user.getRole());
            throw new ServiceException("403", "선택한 역할로 로그인할 수 없습니다.");
        }

        return user;
    }

    /**
     * RefreshToken 유효성 검증 및 조회
     */
    private UserToken validateAndGetRefreshToken(String refreshToken) {
        UserToken userToken = userTokenRepository.findByRefreshTokenAndIsActiveTrue(refreshToken)
                .orElseThrow(() -> new ServiceException("401", "유효하지 않은 RefreshToken 입니다."));

        if (userToken.isExpired()) {
            userTokenRepository.delete(userToken);
            throw new ServiceException("401", "RefreshToken이 만료되었습니다.");
        }

        return userToken;
    }
}