package com.back.global.config;

import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Google Analytics 4 설정
 * GA4 Data API 클라이언트를 빈으로 등록
 * 
 * 2025.10.01 생성
 */
@Configuration
@Slf4j
public class GoogleAnalyticsConfig {

    @Value("${google.analytics.credentials-path}")
    private Resource credentialsResource;

    /**
     * GA4 Data API 클라이언트 빈 생성
     * 서비스 계정 JSON 키로 인증
     */
    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        log.info("GA4 Data API 클라이언트 초기화 중...");

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsResource.getInputStream())
                .createScoped("https://www.googleapis.com/auth/analytics.readonly");

        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        log.info("GA4 Data API 클라이언트 초기화 완료");
        return BetaAnalyticsDataClient.create(settings);
    }
}
