package com.back.global.security.jwt;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // JWT 필터를 적용하지 않을 경로들
    private static final Set<String> EXCLUDED_PATH_PREFIXES = Set.of(
            "/api/auth/signup", "/api/auth/login", "/api/auth/refresh",
            "/h2-console/", "/v3/api-docs", "/swagger-ui",
            "/swagger-resources", "/webjars/", "/ws/", "/chat/",
            "/topic/", "/app/", "/css/", "/js/", "/images/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청에서 JWT 토큰 추출
        String token = resolveToken(request);

        try {
            // 2. 토큰이 있고 유효한 경우
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // 3. 토큰에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                Role currentRole = jwtTokenProvider.getRoleFromToken(token);

                // 4. DB에서 사용자 조회
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

                // 5. CustomUserDetails 생성
                CustomUserDetails userDetails = new CustomUserDetails(user, currentRole);

                // 6. Authentication 객체 생성 - Principal에 CustomUserDetails 설정
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, currentRole);
            }
        } catch (Exception e) {
            // 인증 실패 시 로그 기록 및 SecurityContext 초기화
            log.debug("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰을 추출하는 메서드 (헤더 또는 쿠키)
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 확인 (앱 환경용)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2. 쿠키에서 토큰 확인 (웹 환경용)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null; // 토큰이 없으면 null 반환
    }

    /**
     * 특정 경로에 대해 필터를 적용하지 않도록 설정 (예: Swagger, H2 콘솔 등)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Set을 이용한 효율적인 경로 체크
        for (String prefix : EXCLUDED_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        // HTML 파일들과 루트 경로
        if (path.endsWith(".html") || path.equals("/") || path.equals("/favicon.ico")) {
            return true;
        }

        return false;
    }
}
