package com.back.domain.funding.service;

import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingStatus;
import com.back.domain.funding.repository.FundingContributionRepository;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundingStatusService {

    private final FundingRepository fundingRepository;
    private final FundingContributionRepository fundingContributionRepository;
    private final NotificationService notificationService;
    private final Rq rq;

    public Funding getByIdOrThrow(Long id) {
        return fundingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 펀딩입니다."));
    }

    private void requireOwnerOrAdmin(Funding funding) {
        Long ownerId = funding.getUser().getId();
        if (!(rq.isAdmin() || rq.isSameUser(ownerId))) {
            throw new ServiceException("403", "권한이 없습니다.");
        }
    }

    // 펀딩 승인
    @Transactional
    public void approveFunding(Long fundingId) {
        Funding funding = getByIdOrThrow(fundingId);
        rq.requireAdmin();
        try {
            funding.approve();
        } catch (IllegalStateException e) {
            throw new ServiceException("400", e.getMessage());
        }
    }

    // 펀딩 거절
    @Transactional
    public void rejectFunding(Long fundingId) {
        Funding funding = getByIdOrThrow(fundingId);
        rq.requireAdmin();
        try {
            funding.reject();
        } catch (IllegalStateException e) {
            throw new ServiceException("400", e.getMessage());
        }
    }


    // 펀딩 종료
    @Transactional
    public void closeFunding(Long fundingId, String userEmail) {
        rq.requireLogin();
        Funding funding = getByIdOrThrow(fundingId);
        requireOwnerOrAdmin(funding);
        try {
            funding.close();
        } catch (IllegalStateException e) {
            throw new ServiceException("400", e.getMessage());
        }
    }

    // 펀딩 취소
    @Transactional
    public void cancelFunding(Long fundingId, String userEmail) {
        rq.requireLogin();
        Funding funding = getByIdOrThrow(fundingId);
        requireOwnerOrAdmin(funding);
        try {
            funding.cancel();
        } catch (IllegalStateException e) {
            throw new ServiceException("400", e.getMessage());
        }
    }

    // 만료된 모든 OPEN 펀딩을 CLOSED로 변경
    @Transactional
    public int closeExpiredFundings() {
        LocalDateTime now = LocalDateTime.now();

        // COUNT로 먼저 확인
        long expiredCount = fundingRepository
                .countByStatusAndEndDateBefore(FundingStatus.OPEN, now);

        if (expiredCount == 0) {
            log.debug("[자동 종료] 종료할 펀딩 없음");
            return 0;
        }

        log.info("[자동 종료] 종료 대상 펀딩 {}건 발견", expiredCount);

        // 배치 업데이트로 한 번에 처리
        int closed = fundingRepository.bulkCloseExpiredFundings(now);

        log.info("[자동 종료] 완료 - 처리: {}건", closed);

        return closed;
    }

    // 모든 CLOSED 펀딩을 최종 처리 (SUCCESS/FAILED)
    @Transactional
    public FinalizeResult finalizeAllClosedFundings() {
        List<Funding> closedFundings = fundingRepository.findByStatus(FundingStatus.CLOSED);

        if (closedFundings.isEmpty()) {
            log.debug("[최종 처리] 처리할 펀딩 없음");
            return new FinalizeResult(0, 0, 0);
        }

        log.info("[최종 처리] 대상 펀딩 {}건", closedFundings.size());

        int successCount = 0;
        int failedCount = 0;
        int errorCount = 0;

        for (Funding funding : closedFundings) {
            try {
                FinalizeStatus status = processFinalizeLogic(funding);

                if (status == FinalizeStatus.SUCCESS) {
                    successCount++;
                } else {
                    failedCount++;
                }

            } catch (Exception e) {
                errorCount++;
                log.error("[최종 처리] 실패: ID={}", funding.getId(), e);
            }
        }

        log.info("[최종 처리] 완료 - 성공: {}건, 실패: {}건, 오류: {}건",
                successCount, failedCount, errorCount);

        return new FinalizeResult(successCount, failedCount, errorCount);
    }

    // 단일 펀딩 최종 처리
    @Transactional
    public FinalizeStatus finalizeFunding(Long fundingId) {
        Funding funding = getByIdOrThrow(fundingId);
        return processFinalizeLogic(funding);
    }

    // 누락된 펀딩 체크 및 처리 (정합성 체크)
    @Transactional
    public int checkAndCloseMissedFundings() {
        LocalDateTime now = LocalDateTime.now();

        List<Funding> missedFundings = fundingRepository
                .findByStatusAndEndDateBefore(FundingStatus.OPEN, now);

        if (missedFundings.isEmpty()) {
            log.info("[정합성 체크] 모든 펀딩 상태 정상");
            return 0;
        }

        log.warn("[정합성 체크] 누락된 펀딩 {}건 발견", missedFundings.size());

        int closed = 0;
        for (Funding funding : missedFundings) {
            try {
                funding.close();
                closed++;
                log.info("[정합성 체크] 누락 펀딩 종료: ID={}, 종료일={}",
                        funding.getId(), funding.getEndDate());
            } catch (Exception e) {
                log.error("[정합성 체크] 처리 실패: ID={}", funding.getId(), e);
            }
        }

        return closed;
    }

    // 펀딩 최종 처리 핵심 로직 (CLOSED -> SUCCESS/FAILED)
    private FinalizeStatus processFinalizeLogic(Funding funding) {
        // 상태 체크
        if (funding.getStatus() != FundingStatus.CLOSED) {
            throw new ServiceException("400", "종료된 펀딩만 최종 처리할 수 있습니다.");
        }

        // 실제 모금액 조회
        Long collectedAmount = fundingContributionRepository
                .sumContributedAmountByFundingId(funding.getId());
        long actualAmount = collectedAmount != null ? collectedAmount : 0L;

        // 참여자 수 조회
        Long participantCount = fundingContributionRepository
                .countDistinctParticipantsByFundingId(funding.getId());
        int actualParticipants = participantCount != null ? participantCount.intValue() : 0;

        // 펀딩 통계 동기화
        funding.syncStats(actualAmount, actualParticipants);

        // 목표 금액 달성 여부에 따라 상태 변경
        double achievementRate = (actualAmount * 100.0) / funding.getTargetAmount();

        if (actualAmount >= funding.getTargetAmount()) {
            funding.markAsSuccess();
            log.info("펀딩 성공: ID={}, 목표={}원, 달성={}원, 달성률={}%",
                    funding.getId(), funding.getTargetAmount(), actualAmount,
                    String.format("%.2f", achievementRate));

            // 알림 발송 - 펀딩 성공
            sendFundingSuccessNotifications(funding);

            return FinalizeStatus.SUCCESS;

        } else {
            funding.markAsFailed();
            log.info("펀딩 실패: ID={}, 목표={}원, 달성={}원, 달성률={}%",
                    funding.getId(), funding.getTargetAmount(), actualAmount,
                    String.format("%.2f", achievementRate));

            // 알림 발송 - 펀딩 실패
            sendFundingFailedNotifications(funding);

            return FinalizeStatus.FAILED;
        }
    }

    /**
     * 펀딩 성공 알림 발송
     */
    private void sendFundingSuccessNotifications(Funding funding) {
        // 작가에게 알림
        notificationService.sendNotification(
                funding.getUser(),
                NotificationType.FUNDING_SUCCESS_SELLER,
                "펀딩이 목표 금액을 달성했습니다. 펀딩: " + funding.getTitle(),
                "/artist/fundings/" + funding.getId()
        );

        // 참여자들에게 알림
        List<User> participants = fundingContributionRepository.findAllParticipantsByFundingId(funding.getId());
        for (User participant : participants) {
            notificationService.sendNotification(
                    participant,
                    NotificationType.FUNDING_SUCCESS,
                    "참여한 펀딩이 성공했습니다. 펀딩: " + funding.getTitle(),
                    "/mypage/fundings/" + funding.getId()
            );
        }
    }

    /**
     * 펀딩 실패 알림 발송
     */
    private void sendFundingFailedNotifications(Funding funding) {
        // 작가에게 알림
        notificationService.sendNotification(
                funding.getUser(),
                NotificationType.FUNDING_FAILED_SELLER,
                "펀딩이 목표 금액 미달로 종료되었습니다. 펀딩: " + funding.getTitle(),
                "/artist/fundings/" + funding.getId()
        );

        // 참여자들에게 알림
        List<User> participants = fundingContributionRepository.findAllParticipantsByFundingId(funding.getId());
        for (User participant : participants) {
            notificationService.sendNotification(
                    participant,
                    NotificationType.FUNDING_FAILED,
                    "참여한 펀딩이 목표 금액 미달로 종료되었습니다. 펀딩: " + funding.getTitle(),
                    "/mypage/fundings/" + funding.getId()
            );
        }
    }

    @Transactional
    public CompleteResult processAllFundings() {
        log.info("[통합 처리] 시작");

        // 1단계: 만료된 펀딩 종료 (OPEN -> CLOSED)
        int closedCount = closeExpiredFundings();

        // 2단계: 종료된 펀딩 최종 처리 (CLOSED -> SUCCESS/FAILED)
        FinalizeResult finalizeResult = finalizeAllClosedFundings();

        log.info("[통합 처리] 완료 - 종료: {}건, 성공: {}건, 실패: {}건",
                closedCount,
                finalizeResult.successCount(),
                finalizeResult.failedCount());

        return new CompleteResult(closedCount, finalizeResult);
    }

    public record FinalizeResult(
            int successCount,
            int failedCount,
            int errorCount
    ) {
        public int totalProcessed() {
            return successCount + failedCount;
        }
    }

    public enum FinalizeStatus {
        SUCCESS,  // 성공 처리됨
        FAILED    // 실패 처리됨
    }

    public record CompleteResult(
            int closedCount,            // OPEN -> CLOSED 처리된 수
            int successCount,           // CLOSED -> SUCCESS 처리된 수
            int failedCount,            // CLOSED -> FAILED 처리된 수
            int errorCount              // 오류 발생 수
    ) {
        public CompleteResult(int closedCount, FinalizeResult finalizeResult) {
            this(closedCount,
                    finalizeResult.successCount(),
                    finalizeResult.failedCount(),
                    finalizeResult.errorCount());
        }

        public int totalFinalized() {
            return successCount + failedCount + errorCount;
        }
    }

    // 승인된 펀딩 open 처리
    @Transactional
    public int openApprovedFundings() {
        LocalDateTime now = LocalDateTime.now();

        // COUNT로 먼저 확인
        long approvedCount = fundingRepository
                .countByStatusAndStartDateBefore(FundingStatus.APPROVED, now);

        if (approvedCount == 0) {
            log.debug("[자동 오픈] 오픈할 펀딩 없음");
            return 0;
        }

        log.info("[자동 오픈] 오픈 대상 펀딩 {}건 발견", approvedCount);

        // 배치 업데이트로 한 번에 처리
        int opened = fundingRepository.bulkOpenApprovedFundings(now);

        log.info("[자동 오픈] 완료 - 처리: {}건", opened);

        return opened;
    }

    // 누락된 펀딩 체크 및 처리 (정합성 체크)
    @Transactional
    public int checkAndOpenMissedFundings() {
        LocalDateTime now = LocalDateTime.now();

        List<Funding> missedFundings = fundingRepository
                .findByStatusAndStartDateBefore(FundingStatus.APPROVED, now);

        if (missedFundings.isEmpty()) {
            log.info("[정합성 체크] 모든 승인 펀딩 상태 정상");
            return 0;
        }

        log.warn("[정합성 체크] 누락된 승인 펀딩 {}건 발견", missedFundings.size());

        int opened = 0;
        for (Funding funding : missedFundings) {
            funding.open();
            opened++;
            log.info("[정합성 체크] 누락 펀딩 오픈: ID={}, 시작일={}",
                    funding.getId(), funding.getStartDate());
        }
        return opened;
    }
}
