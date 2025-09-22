package com.back.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * 테스트 환경 설정
 * 테스트 실행 시 필요한 공통 설정들을 정의합니다.
 *2025.09.20 수정
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * 테스트용 MockMvc 설정
     * 
     * @param context 웹 애플리케이션 컨텍스트
     * @return 설정된 MockMvc 인스턴스
     */
    @Bean
    @Primary
    public MockMvc mockMvc(WebApplicationContext context) {
        return webAppContextSetup(context)
                .build();
    }
}
