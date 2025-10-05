package com.back.domain.funding.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "fundings", indexes = {
        @Index(name = "idx_funding_status_enddate", columnList = "status, end_date")
})
public class Funding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "category_id", nullable = false)
//    private Category category; // parent == null 인 상위 카테고리만 사용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FundingStatus status = FundingStatus.OPEN;

    @Column(nullable = false)
    private long targetAmount;

    @Column(nullable = false)
    @Builder.Default
    private long collectedAmount = 0L;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private int participantCount = 0;

    // ========== 컬렉션 ==========

    @Builder.Default
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<FundingOption> options = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createDate DESC, id DESC")
    private List<FundingNews> news = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createDate DESC, id DESC")
    private List<FundingCommunity> communities = new ArrayList<>();

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)// 펀딩이 저장/삭제되면 모든 펀딩이미지도 저장/삭제됨, 펀딩에서 이미지를 제거하면 DB에서 해당 이미지도 삭제됨.
    @org.hibernate.annotations.BatchSize(size = 100)  // N+1 방지: 100개씩 배치로 조회
    private List<FundingImage> images = new ArrayList<>();; // 해당 펀딩의 이미지들(대표/추가/썸네일 이미지)

    // ========== 팩토리 메서드 ==========

    public static Funding create(
            User user,
            String title,
            String description,
//            Category category,
            String imageUrl,
            long targetAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            FundingStatus initialStatus,
            List<FundingOption> options
    ) {
        validateCreation(user, title, description, targetAmount, startDate, endDate);

        Funding funding = Funding.builder()
                .user(user)
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
//                .category(category)
                .targetAmount(targetAmount)
                .startDate(startDate)
                .endDate(endDate)
                .status(initialStatus != null ? initialStatus : FundingStatus.OPEN)
                .build();

        if (options != null) {
            options.forEach(funding::attachOption);
        }

        return funding;
    }

    // ========== 기본 정보 수정 ==========

    public void updateBasicInfo(String title, String description, String imageUrl) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null && !description.isBlank()) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateTargetAmount(long newTargetAmount) {
        validateTargetAmountUpdate(newTargetAmount);
        this.targetAmount = newTargetAmount;
    }

    public void updateEndDate(LocalDateTime newEndDate) {
        validateEndDateUpdate(newEndDate);
        this.endDate = newEndDate;
    }

    // ========== 상태 변경 ==========

    public void close() {
        if (this.status != FundingStatus.OPEN) {
            throw new IllegalStateException("진행 중인 펀딩만 종료할 수 있습니다.");
        }
        this.status = FundingStatus.CLOSED;
    }

    public void markAsSuccess() {
        if (this.status != FundingStatus.CLOSED) {
            throw new IllegalStateException("종료된 펀딩만 성공 처리할 수 있습니다.");
        }
        if (this.collectedAmount < this.targetAmount) {
            throw new IllegalStateException("목표 금액을 달성하지 못했습니다.");
        }
        this.status = FundingStatus.SUCCESS;
    }

    public void markAsFailed() {
        if (this.status != FundingStatus.CLOSED) {
            throw new IllegalStateException("종료된 펀딩만 실패 처리할 수 있습니다.");
        }
        this.status = FundingStatus.FAILED;
    }

    public void cancel() {
        if (this.status == FundingStatus.SUCCESS || this.status == FundingStatus.FAILED) {
            throw new IllegalStateException("이미 완료된 펀딩은 취소할 수 없습니다.");
        }
        this.status = FundingStatus.CANCELED;
    }

    // ========== 옵션 관리 ==========

    public void attachOption(FundingOption option) {
        option.attachTo(this);
        this.options.add(option);
    }

    public void addOption(FundingOption newOption) {
        this.attachOption(newOption);
    }

    public void updateOption(Long optionId, String name, Long price, Integer stock, Integer sortOrder) {
        FundingOption option = this.options.stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 옵션을 찾을 수 없습니다."));

        option.update(name, price, stock, sortOrder);
    }

    // ========== 금액/참여자 관리 ==========

    public void increaseCollectedAmount(long delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("증가 금액은 0보다 커야 합니다.");
        }
        this.collectedAmount += delta;
    }

    public void increaseParticipantCount(int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("증가 수는 0보다 커야 합니다.");
        }
        this.participantCount += delta;
    }

    // ========== 조회 메서드 ==========

    public boolean isEndDatePassed() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    public boolean canBeEditedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isEditable() {
        return this.status == FundingStatus.OPEN;
    }

    public List<FundingOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public List<FundingNews> getNews() {
        return Collections.unmodifiableList(news);
    }

    public List<FundingCommunity> getCommunities() {
        return Collections.unmodifiableList(communities);
    }

    public List<FundingImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    // ========== 검증 메서드 ==========

    private static void validateCreation(
            User user,
            String title,
            String description,
            long targetAmount,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (user == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("제목은 100자를 초과할 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("설명은 필수입니다.");
        }
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("목표 금액은 0보다 커야 합니다.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작/종료일은 필수입니다.");
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("시작일은 현재 시각 이후여야 합니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
    }

    private void validateTargetAmountUpdate(long newTargetAmount) {
        if (newTargetAmount <= 0) {
            throw new IllegalArgumentException("목표 금액은 0보다 커야 합니다.");
        }
        if (this.participantCount > 0) {
            throw new IllegalStateException(
                    "참여자가 있으면 목표 금액을 수정할 수 없습니다."
            );
        }
    }

    private void validateEndDateUpdate(LocalDateTime newEndDate) {
        if (newEndDate == null) {
            throw new IllegalArgumentException("종료일은 필수입니다.");
        }
        if (newEndDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("종료일은 기존 종료일보다 이후여야 합니다.");
        }
        if (newEndDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("종료일은 현재 시각 이후여야 합니다.");
        }
        if (this.status != FundingStatus.OPEN) {
            throw new IllegalStateException("진행 중인 펀딩만 종료일을 수정할 수 있습니다.");
        }
    }
}