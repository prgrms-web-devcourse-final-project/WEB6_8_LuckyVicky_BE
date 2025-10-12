package com.back.global.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리용 스레드풀 설정 클래스
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * S3 업로드 전용 스레드풀
     * CorePoolSize: 기본 스레드 수
     * MaxPoolSize: 최대 동시 스레드 수
     * QueueCapacity: 대기 큐 크기
     * CallerRunsPolicy: 큐가 꽉 찼을 때 호출한 스레드에서 실행
     */
    @Bean(name = "s3UploadExecutor")
    public Executor s3UploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("S3Upload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
