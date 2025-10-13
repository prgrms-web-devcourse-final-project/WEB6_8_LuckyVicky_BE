package com.back.domain.funding.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "funding_communities")
@SQLDelete(sql = "UPDATE funding_community SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class FundingCommunity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Funding funding;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    public void attachTo(Funding funding) { this.funding = funding; }

    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public static FundingCommunity create(Funding funding, User author, String content) {
        if (funding == null) throw new IllegalStateException("펀딩이 존재하지 않습니다.");
        if (author == null) throw new IllegalStateException("작성자가 존재하지 않습니다.");
        if (content == null || content.isBlank()) throw new IllegalStateException("내용을 입력해주세요.");
        return FundingCommunity.builder()
                .funding(funding)
                .author(author)
                .content(content)
                .build();
    }

    public void delete() {
        if (this.deleted) throw new IllegalStateException("이미 삭제된 글입니다.");
        if (this.funding == null) throw new IllegalStateException("펀딩이 존재하지 않습니다.");
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
