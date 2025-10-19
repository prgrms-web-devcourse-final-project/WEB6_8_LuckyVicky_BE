package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.PasswordResetRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.PasswordResetResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.entity.UserToken;
import com.back.domain.auth.repository.UserTokenRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import com.back.global.service.EmailService;
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
    private final EmailService emailService;

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
                accessTokenExpiration / 1000,
                user.needsAdditionalInfo() // 소셜로그인 사용자 추가 정보 필요 여부
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
                accessTokenExpiration / 1000,
                user.needsAdditionalInfo() // 소셜로그인 사용자 추가 정보 필요 여부
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

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));

        // 현재 활성화된 토큰 조회 (가장 최근 토큰 하나만)
        UserToken userToken = userTokenRepository
                .findFirstByUserIdAndIsActiveTrueOrderByCreateDateDesc(userId)
                .orElseThrow(() -> new ServiceException("401", "유효한 토큰이 없습니다."));

        log.info("현재 사용자 정보 조회: userId={}, needsAdditionalInfo={}",
                userId, user.needsAdditionalInfo());

        return AuthResponse.loginSuccess(
                null,  // accessToken은 이미 쿠키에 있으므로 null
                null,  // refreshToken도 이미 쿠키에 있으므로 null
                user.getId(),
                user.getEmail(),
                userToken.getLoginRole(),
                user.getAvailableLoginRoles(),
                accessTokenExpiration / 1000,
                user.needsAdditionalInfo()
        );
    }

    /**
     * 비밀번호 찾기 - 임시 비밀번호 발급
     */
    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ServiceException("404", "등록되지 않은 이메일입니다."));

        // OAuth 사용자는 비밀번호 찾기 불가
        if (user.isOAuthUser()) {
            throw new ServiceException("400",
                    "소셜 로그인 사용자는 비밀번호 찾기를 이용할 수 없습니다.");
        }

        // 임시 비밀번호 생성 (8자리 영문+숫자 조합)
        String temporaryPassword = generateTemporaryPassword();

        // 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(temporaryPassword);
        user.changePassword(encodedPassword);
        userRepository.save(user);

        // 모든 기기에서 로그아웃 처리 (보안)
        logoutAll(user.getId());

        // 임시 비밀번호 이메일 발송
        emailService.sendTemporaryPassword(user.getEmail(), temporaryPassword);

        log.info("비밀번호 찾기 완료: userId={}, email={}", user.getId(), user.getEmail());

        return PasswordResetResponse.success(user.getEmail());
    }

    /**
     * 임시 비밀번호 생성 (8자리 영문 대소문자 + 숫자 + 특수문자 조합)
     */
    private String generateTemporaryPassword() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specials = "!@#$%^&*";

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();

        // 필수 문자 각 1개씩 추가 (최소 조건 보장)
        password.append(letters.charAt(random.nextInt(letters.length())));    // 영문 1개
        password.append(numbers.charAt(random.nextInt(numbers.length())));    // 숫자 1개
        password.append(specials.charAt(random.nextInt(specials.length())));  // 특수문자 1개

        // 나머지 5자리는 모든 문자에서 랜덤 선택
        String allChars = letters + numbers + specials;
        for (int i = 0; i < 5; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 문자 순서 섞기 (첫 3자리가 항상 영문-숫자-특수문자 순서가 되지 않도록)
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}