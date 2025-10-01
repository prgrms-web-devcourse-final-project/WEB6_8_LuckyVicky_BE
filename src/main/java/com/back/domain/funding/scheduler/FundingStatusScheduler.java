package com.back.domain.funding.scheduler;

import com.back.domain.funding.service.FundingStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FundingStatusScheduler {

    private final FundingStatusService fundingStatusService;

    // 매시간 정각에 종료일이 지난 펀딩을 CLOSED로 변경
    @Scheduled(cron = "0 0 * * * *")
    public void closeExpiredFundings() {
        long startTime = System.currentTimeMillis();

        int closed = fundingStatusService.closeExpiredFundings();

        long duration = System.currentTimeMillis() - startTime;

        if (closed > 0) {
            log.info("[스케줄러] 펀딩 자동 종료 완료 - 처리: {}건, 소요: {}ms", closed, duration);

            // 성능 모니터링: 5초 이상 걸리면 경고
            if (duration > 5000) {
                log.warn("[성능 경고] 펀딩 종료 처리 시간 초과: {}ms", duration);
            }
        }
    }

    // 매시간 5분에 CLOSED 펀딩을 SUCCESS/FAILED로 최종 처리
    @Scheduled(cron = "0 5 * * * *")
    public void finalizeFundings() {
        long startTime = System.currentTimeMillis();

        FundingStatusService.FinalizeResult result = fundingStatusService.finalizeAllClosedFundings();

        long duration = System.currentTimeMillis() - startTime;

        if (result.totalProcessed() > 0) {
            log.info("[스케줄러] 펀딩 최종 처리 완료 - 성공 펀딩: {}건, 실패 펀딩: {}건, 오류: {}건, 소요: {}ms",
                    result.successCount(), result.failedCount(), result.errorCount(), duration);
        }
    }

    /**
     * 매일 새벽 1시에 정합성 체크 (안전장치)
     * 혹시 놓친 펀딩이 있는지 확인
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void dailyStatusCheck() {
        int closed = fundingStatusService.checkAndCloseMissedFundings();

        if (closed > 0) {
            log.warn("[스케줄러] 정합성 체크 - 누락 펀딩 {}건 처리 완료", closed);
        }
    }
}