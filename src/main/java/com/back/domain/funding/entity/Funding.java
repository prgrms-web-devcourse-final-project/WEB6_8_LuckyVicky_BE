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
public class Funding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

//    private FundingCategory category;  카테고리 추후 구현

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FundingStatus status = FundingStatus.DRAFT;

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<FundingOption> options = new ArrayList<>();

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
}
