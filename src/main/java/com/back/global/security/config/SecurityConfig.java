package com.back.global.security.config;

import com.back.global.config.AppConfig;
import com.back.global.security.jwt.JwtAuthenticationFilter;
import com.back.global.security.oauth2.CustomOAuth2AuthorizationRequestResolver;
import com.back.global.security.oauth2.CustomOAuth2UserService;
import com.back.global.security.oauth2.OAuth2FailureHandler;
import com.back.global.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // OAuth2 관련 Bean 주입
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
    private final CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // 🔓 인증 불필요 - 공개 API
                        // ========================================

                        // 인증/인가 - 회원가입, 로그인, 토큰 관련
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()

                        // 중복 검증 - 이메일, 닉네임, 전화번호
                        .requestMatchers("/api/auth/duplicate/**").permitAll()

                        // OAuth2 로그인 엔드포인트
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 작가 공개 정보 조회
                        .requestMatchers("/api/artist/profile/**", "/api/artist/list").permitAll()

                        // 공개 API
                        .requestMatchers("/public/**").permitAll()

                        // 상품,카테고리,태그 조회 / 상품 파일 다운로드(테스트용) / 상품 상세 조회 - 로그인 없이 접근 허용
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/categories/**","/api/tag/**", "/api/products/images/download/{productUuid}","/api/products/{productUuid}").permitAll()
                        // 상품 등록, 수정, 삭제 / 상품 이미지 업로드 / 작가 사업자 정보 조회 - ARTIST, ADMIN, ROOT만 접근 가능
                        .requestMatchers("/api/products", "/api/products/*", "/api/artist/business-info").hasAnyRole("ARTIST", "ADMIN", "ROOT")
                        // 카테고리,태그 등록, 수정, 삭제 - ADMIN, ROOT만 접근 가능
                        .requestMatchers("/api/categories/**","/api/tag/**").hasAnyRole("ADMIN", "ROOT")

                        // 펀딩 관련 공개 API - 로그인 없이 접근 허용
                        .requestMatchers(HttpMethod.GET, "/api/fundings/**").permitAll()

                        // 개발 도구들
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()

                        // 헬스 체크 허용
                        .requestMatchers("/actuator/health").permitAll()

                        // 정적 리소스
                        .requestMatchers("/favicon.ico", "/*.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // 에러 페이지
                        .requestMatchers("/error", "/").permitAll()

                        // 대시보드 API - 임시로 테스트용 허용 (TODO: 나중에 인증 추가)
                        //.requestMatchers("/api/dashboard/**").permitAll()

                        // 관리자 대시보드 - ADMIN, ROOT만 접근 가능 (TODO: yoostill 확인 필요)
                        .requestMatchers("/api/dashboard/admin/**").hasAnyRole("ADMIN", "ROOT")

                        // 작가 대시보드 - ARTIST, ADMIN, ROOT 접근 가능 (TODO: yoostill 확인 필요)
                        .requestMatchers("/api/dashboard/artist/**").hasAnyRole("ARTIST", "ADMIN", "ROOT")

                        // 일반 대시보드 - 인증된 모든 사용자 접근 가능 (TODO: yoostill 확인 필요)
                        .requestMatchers("/api/dashboard/**").authenticated()

                        // 역할별 API 권한
                        .requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/artist/**").hasRole("ARTIST")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/root/**").hasRole("ROOT")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // OAuth2 사용자 정보 처리 서비스
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 로그인 성공 핸들러
                        .successHandler(oauth2SuccessHandler)
                        // 로그인 실패 핸들러
                        .failureHandler(oauth2FailureHandler)
                        // Authorization Request 커스터마이징 (redirectUrl 지원)
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                        )
                )

                // 예외 처리 추가
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"resultCode\":\"401\",\"msg\":\"인증이 필요합니다.\",\"data\":null}"
                            );
                        })
                )

                // H2 콘솔 프레임 허용
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // CSRF 비활성화 (H2 콘솔 사용 위해 필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 비활성화 (JWT 사용)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // H2 콘솔 사용 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 기본 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정 (개발 환경)
        configuration.setAllowedOrigins(List.of(
                AppConfig.getSiteFrontUrl(),
                AppConfig.getSiteFrontUrl() // 하드코딩 돼있던 url이 환경에 따라 다르게 잡히도록 수정
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // 프리플라이트 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    /**
     * AuthenticationManager 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
