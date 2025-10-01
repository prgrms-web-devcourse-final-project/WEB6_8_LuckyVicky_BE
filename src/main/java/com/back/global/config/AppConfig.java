package com.back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AppConfig {
    // 도메인 정보 로딩
    private static String cookieDomain;
    private static String siteFrontUrl;
    private static String siteBackUrl;

    public AppConfig(
            @Value("${custom.site.cookieDomain}") String cookieDomain,
            @Value("${custom.site.frontUrl}") String siteFrontUrl,
            @Value("${custom.site.backUrl}") String siteBackUrl
    ) {
        AppConfig.cookieDomain = cookieDomain;
        AppConfig.siteFrontUrl = siteFrontUrl;
        AppConfig.siteBackUrl = siteBackUrl;
    }

    public static String getCookieDomain() {
        return cookieDomain;
    }

    public static String getSiteFrontUrl() {
        return siteFrontUrl;
    }

    public static String getSiteBackUrl() {
        return siteBackUrl;
    }
}
