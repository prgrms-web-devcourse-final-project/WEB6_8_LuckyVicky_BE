package com.back.domain.funding.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "fundings")
public class Funding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "category_id", nullable = false)
//    private Category category; // parent == null 인 상위 카테고리만 사용

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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



    @Builder.Default
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<FundingOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)// 펀딩이 저장/삭제되면 모든 펀딩이미지도 저장/삭제됨, 펀딩에서 이미지를 제거하면 DB에서 해당 이미지도 삭제됨.
    private List<FundingImage> images = new ArrayList<>();; // 해당 펀딩의 이미지들(대표/추가/썸네일 이미지)

    public void attachOption(FundingOption option) {
        option.attachTo(this);
        this.options.add(option);
    }

    public void increaseCollectedAmount(long delta) {
        this.collectedAmount += delta;
    }

    public void increaseParticipantCount(int delta) {
        this.participantCount = this.participantCount + delta;
    }

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
        // --- 도메인 규칙 검증 ---
        if (user == null) throw new IllegalArgumentException("작성자는 필수입니다.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("제목은 필수입니다.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("설명은 필수입니다.");
//        if (category == null) throw new IllegalArgumentException("카테고리는 필수입니다.");
        if (targetAmount <= 0) throw new IllegalArgumentException("목표 금액은 0보다 커야 합니다.");
        if (startDate == null || endDate == null) throw new IllegalArgumentException("시작/종료일은 필수입니다.");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");

        Funding f = Funding.builder()
                .user(user)
                .title(title)
                .description(description)
//                .category(category)
                .imageUrl(imageUrl)
                .targetAmount(targetAmount)
                .startDate(startDate)
                .endDate(endDate)
                .status(initialStatus != null ? initialStatus : FundingStatus.OPEN)
                .build();

        if (options != null) {
            for (FundingOption o : options) {
                f.attachOption(o); // 양방향 세팅
            }
        }
        return f;
    }
}
