package com.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정 클래스
 * 
 * @EnableJpaAuditing: BaseEntity의 @CreatedDate, @LastModifiedDate 활성화
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // BaseEntity의 자동 생성/수정 시간 기록을 위한 설정
}
