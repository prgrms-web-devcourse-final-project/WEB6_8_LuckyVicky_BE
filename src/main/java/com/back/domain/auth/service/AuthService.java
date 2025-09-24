package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignUpRequest;
import com.back.domain.auth.dto.request.TokenRefreshRequest;
import com.back.domain.auth.dto.response.AuthResponse;
import com.back.domain.auth.dto.response.SignUpResponse;

public interface AuthService {

    // 회원가입
    SignUpResponse signUp(SignUpRequest request);

    // 로그인 (역할 선택 포함)
    AuthResponse login(LoginRequest request);

    // 로그아웃
    void logout(String refreshToken);

    // 토큰 재발급
    AuthResponse refreshToken(TokenRefreshRequest request);

    // 전체 로그아웃
    void logoutAll(Long userId);
}
