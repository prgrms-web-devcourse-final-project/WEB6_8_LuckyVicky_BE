package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.SignUpResponse;
import com.back.domain.auth.service.AuthService;
import com.back.domain.user.entity.Role;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증/인가", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    public ResponseEntity<RsData<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request,
            HttpServletRequest httpServletRequest
    ) {

        // 클라이언트 IP 주소를 포함한 새로운 요청 객체 생성
        SignUpRequest requestWithIp = new SignUpRequest(
                request.email(),
                request.password(),
                request.passwordConfirm(),
                request.name(),
                request.phone(),
                request.privacyRequiredAgreed(),
                request.marketingAgreed()
        );

        SignUpResponse response = authService.signUp(requestWithIp);

        return ResponseEntity.ok(
                RsData.of("201", "회원가입 성공", response)
        );
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    public ResponseEntity<RsData<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {

        // Role이 없으면 기본값 USER로 설정
        Role selectedRole = loginRequest.selectedRole() != null ? loginRequest.selectedRole() : Role.USER;

        // 클라이언트 IP 주소를 포함한 새로운 요청 객체 생성
        LoginRequest request = new LoginRequest(
                loginRequest.email(),
                loginRequest.password(),
                selectedRole
        );

        AuthResponse response = authService.login(request);

        // 쿠키 생성 (RefreshToken: 7일, AccessToken: 15분)
        ResponseCookie refreshTokenCookie = createTokenCookie("refreshToken", response.refreshToken(), 7 * 24 * 60 * 60);
        ResponseCookie accessTokenCookie = createTokenCookie("accessToken", response.accessToken(), 15 * 60);

        // HttpHeaders를 사용하여 여러 Set-Cookie 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(RsData.of("200", "로그인 성공", response));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 기기에서 로그아웃을 처리합니다.")
    public ResponseEntity<RsData<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {

        // RefreshToken이 있으면 DB에서 삭제
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                authService.logout(refreshToken);
            } catch (Exception e) {
                log.warn("로그아웃 처리 중 오류 발생 (쿠키는 삭제됨): {}", e.getMessage());
            }
        }

        // 쿠키 삭제 (토큰 유무와 관계없이 실행)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("refreshToken").toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("accessToken").toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(RsData.of("200", "로그아웃 성공"));
    }

    /**
     * 전체 로그아웃
     */
    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃", description = "모든 기기에서 로그아웃을 처리합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RsData<Void>> logoutAll(
            HttpServletRequest request,
            Authentication authentication
    ) {
        // null 체크 및 인증 여부 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceException("401", "인증이 필요합니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getUserId();

        authService.logoutAll(authenticatedUserId);
        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie("refreshToken").toString())
                .header("Set-Cookie", deleteCookie("accessToken").toString())
                .body(RsData.of("200", "전체 로그아웃 성공"));
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "RefreshToken을 사용하여 새로운 토큰을 발급받습니다.")
    public ResponseEntity<RsData<AuthResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);

        // 쿠키 생성 (RefreshToken: 7일, AccessToken: 15분)
        ResponseCookie refreshTokenCookie = createTokenCookie("refreshToken", response.refreshToken(), 7 * 24 * 60 * 60);
        ResponseCookie accessTokenCookie = createTokenCookie("accessToken", response.accessToken(), 15 * 60);

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .header("Set-Cookie", accessTokenCookie.toString())
                .body(RsData.of("200", "토큰 재발급 성공", response));
    }


    /**
     * 쿠키 생성 헬퍼 메서드
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure) // 환경변수로 제어, 개발: false, 운영: true
                .domain(cookieDomain)  // 환경별 도메인 설정
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    /**
     * 쿠키 삭제 헬퍼 메서드
     */
    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure) // 환경변수로 제어, 개발: false, 운영: true
                .domain(cookieDomain)  // 환경별 도메인 설정
                .path("/")
                .maxAge(0) // 즉시 만료
                .sameSite("Strict")
                .build();
    }

}
