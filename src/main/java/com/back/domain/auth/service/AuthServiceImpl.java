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
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 회원가입
     */
    @Override
    public SignUpResponse signUp(SignUpRequest request) {
        // 비밀번호 확인 검증
        if (!request.isPasswordMatching()) {
            throw new ServiceException("PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다.");
        }

        // 필수 약관 동의 검증
        if (!request.isRequiredTermsAgreed()) {
            throw new ServiceException("REQUIRED_TERMS_NOT_AGREED", "필수 약관에 동의해야 합니다.");
        }

        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new ServiceException("EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 검증
        if (userRepository.existsByName(request.name())) {
            throw new ServiceException("EMAIL_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다.");
        }

        // 전화번호 중복 검증
        if (userRepository.existsByPhone(request.phone())) {
            throw new ServiceException("PHONE_ALREADY_EXISTS", "이미 사용 중인 전화번호입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 사용자 생성 및 저장
        User user = User.createLocalUser(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone(),
                request.agreementIp()
        );

        userRepository.save(user);

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
    @Override
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ServiceException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ServiceException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // 선택한 역할로 로그인 가능한지 검증
        if (!user.getRole().canLoginAs(request.selectedRole())) {
            throw new ServiceException("ROLE_MISMATCH", "선택한 역할로 로그인할 수 없습니다.");
        }

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
                LocalDateTime.now().plusDays(7),
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
        // RefreshToken 조회 및 검증
        UserToken userToken = userTokenRepository.findByRefreshTokenAndIsActiveTrue(request.refreshToken())
                .orElseThrow(() -> new ServiceException("INVALID_REFRESH_TOKEN", "유효하지 않은 RefreshToken 입니다."));

        // 토큰 만료 확인
        if (userToken.isExpired()) {
            userTokenRepository.delete(userToken);
            userTokenRepository.save(userToken);
            throw new ServiceException("EXPIRED_REFRESH_TOKEN", "RefreshToken이 만료되었습니다.");
        }

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
                LocalDateTime.now().plusDays(7),
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
    @Override
    public void logout(String refreshToken) {
        userTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(userTokenRepository::delete);
        log.info("로그아웃 성공: refreshToken={}", refreshToken);
    }

    /**
     * 전체 로그아웃 (모든 기기에서 로그아웃) - 토큰 삭제
     */
    @Override
    public void logoutAll(Long userId) {
        userTokenRepository.deleteAllRefreshTokenByUserId(userId);
        log.info("전체 로그아웃 성공: userId={}", userId);
    }
}
