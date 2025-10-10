package com.back.domain.faq.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * FAQ 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "faqs")
public class Faq extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FaqCategory category;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Builder
    public Faq(String question, String answer, FaqCategory category) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.viewCount = 0L;
    }

    /**
     * FAQ 수정
     */
    public void update(String question, String answer, FaqCategory category) {
        if (question != null) this.question = question;
        if (answer != null) this.answer = answer;
        if (category != null) this.category = category;
    }

    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
}
