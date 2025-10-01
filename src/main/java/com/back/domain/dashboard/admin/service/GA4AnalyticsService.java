package com.back.domain.dashboard.admin.service;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Analytics 4 데이터 조회 서비스
 * 
 * GA4 Data API를 사용하여 유입 경로 등의 분석 데이터를 조회합니다.
 * 2025.10.01 생성
 */
@Service
@Slf4j
public class GA4AnalyticsService {

    @Value("${google.analytics.property-id}")
    private String propertyId;

    @Value("${google.analytics.credentials-path}")
    private Resource credentialsResource;

    /**
     * GA4 클라이언트 생성
     */
    private BetaAnalyticsDataClient createClient() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsResource.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/analytics.readonly"));

        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        return BetaAnalyticsDataClient.create(settings);
    }

    /**
     * 유입 경로별 세션 및 사용자 수 조회
     * 
     * @param days 최근 며칠간의 데이터를 조회할지 (기본: 7일)
     * @return 소스명 → (세션수, 사용자수) 맵
     */
    public Map<String, TrafficData> getTrafficSources(int days) {
        log.info("GA4 유입 경로 데이터 조회 시작 - days: {}, propertyId: {}", days, propertyId);

        try (BetaAnalyticsDataClient analyticsData = createClient()) {
            
            // 날짜 범위 설정
            String startDate = days + "daysAgo";
            String endDate = "today";

            // GA4 리포트 요청 생성
            RunReportRequest request = RunReportRequest.newBuilder()
                    .setProperty(propertyId)
                    // Dimension: 유입 소스 (sessionSource)
                    .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                    // Metrics: 세션 수, 사용자 수
                    .addMetrics(Metric.newBuilder().setName("sessions"))
                    .addMetrics(Metric.newBuilder().setName("totalUsers"))
                    // 날짜 범위
                    .addDateRanges(DateRange.newBuilder()
                            .setStartDate(startDate)
                            .setEndDate(endDate))
                    // 정렬: 세션 수 기준 내림차순
                    .addOrderBys(OrderBy.newBuilder()
                            .setMetric(OrderBy.MetricOrderBy.newBuilder()
                                    .setMetricName("sessions"))
                            .setDesc(true))
                    .build();

            // API 호출
            RunReportResponse response = analyticsData.runReport(request);

            // 결과 파싱
            Map<String, TrafficData> trafficMap = new HashMap<>();
            
            for (Row row : response.getRowsList()) {
                String source = row.getDimensionValues(0).getValue();
                long sessions = Long.parseLong(row.getMetricValues(0).getValue());
                long users = Long.parseLong(row.getMetricValues(1).getValue());
                
                trafficMap.put(source, new TrafficData(sessions, users));
                
                log.debug("유입 소스 파싱 - source: {}, sessions: {}, users: {}", 
                        source, sessions, users);
            }

            log.info("GA4 유입 경로 데이터 조회 완료 - 총 {} 개 소스", trafficMap.size());
            return trafficMap;

        } catch (Exception e) {
            log.error("GA4 데이터 조회 중 오류 발생", e);
            throw new RuntimeException("GA4 데이터 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 유입 경로 데이터 (세션수, 사용자수)
     */
    public record TrafficData(long sessions, long users) {}
}
