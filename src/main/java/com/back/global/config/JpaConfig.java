package com.back.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 설정 클래스
 * 
 * @EnableJpaAuditing: BaseEntity의 @CreatedDate, @LastModifiedDate 활성화
 * JPAQueryFactory Bean 등록: QueryDSL 사용을 위한 필수 Bean
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // BaseEntity의 자동 생성/수정 시간 기록을 위한 설정

    // QueryDSL 사용을 위한 설정
    private final EntityManager entityManager;

    public JpaConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    // QueryDSL JPAQueryFactory Bean 등록
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
