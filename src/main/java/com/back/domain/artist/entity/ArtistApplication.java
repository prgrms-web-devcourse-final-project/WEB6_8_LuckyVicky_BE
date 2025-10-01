package com.back.domain.artist.entity;

import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 작가 신청서 엔티티
 */
@Getter
@Entity
@Table(name = "artist_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String ownerName; // 대표자명(실명)

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private String artistName; // 작가활동명

    private String businessNumber; // 사업자번호

    private String businessName; // 상호명

    private String businessAddress; // 사업장 기본주소(도로명/지번)

    private String businessAddressDetail; // 사업장 상세주소(동/호수)

    private String businessZipCode; // 사업장 우편번호

    private String telecomSalesNumber; // 통신판매번호

    private String snsAccount;

    private String mainProducts; // 주력 상품

    private String managerPhone; // 담당자 연락처

    private String bankName; // 은행명

    private String bankAccount; // 계좌번호

    private String accountName; // 예금주명

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING; // 신청 상태

    private String rejectionReason; // 거부 사유

    private Long reviewedById; // 검토자 ID

    private String reviewedByName; // 검토자 이름

    private LocalDateTime reviewedAt; // 검토 일시

    @OneToMany(mappedBy = "artistApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArtistDocument> documents = new ArrayList<>();

    @Builder
    public ArtistApplication(User user, String ownerName, String email, String phone, String artistName,
                             String businessNumber, String businessName, String businessAddress,
                             String businessAddressDetail, String businessZipCode, String telecomSalesNumber,
                             String snsAccount, String mainProducts, String managerPhone,
                             String bankName, String bankAccount, String accountName) {
        this.user = user;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.artistName = artistName;
        this.businessNumber = businessNumber;
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.businessAddressDetail = businessAddressDetail;
        this.businessZipCode = businessZipCode;
        this.telecomSalesNumber = telecomSalesNumber;
        this.snsAccount = snsAccount;
        this.mainProducts = mainProducts;
        this.managerPhone = managerPhone;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.accountName = accountName;
    }


    // ==== 상태 변경 메서드들 ==== //

    /**
     * 신청서 승인
     * @param reviewerId 검토한 관리자 ID
     * @param reviewerName 검토한 관리자 이름
     */
    public void approve(Long reviewerId, String reviewerName) {
        validateCanChangeStatus();
        this.status = ApplicationStatus.APPROVED;
        this.rejectionReason = null;
        this.reviewedById = reviewerId;
        this.reviewedByName = reviewerName;
        this.reviewedAt = LocalDateTime.now();
    }

    /**
     * 신청서 거부
     * @param reviewerId 검토한 관리자 ID
     * @param reviewerName 검토한 관리자 이름
     * @param rejectionReason 거절 사유(필수)
     */
    public void reject(Long reviewerId, String reviewerName, String rejectionReason) {
        validateCanChangeStatus();
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new ServiceException("500", "거부 사유는 필수입니다.");
        }

        this.status = ApplicationStatus.REJECTED;
        this.reviewedById = reviewerId;
        this.reviewedByName = reviewerName;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }


    // ==== 상태 검증 메서드들 ==== //

    public boolean isPending() {
        return ApplicationStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return ApplicationStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return ApplicationStatus.REJECTED.equals(this.status);
    }

    private void validateCanChangeStatus() {
        if (!isPending()) throw new ServiceException("500", "대기중 상태의 신청서만 승인/거절할 수 있습니다.");

    }

}
