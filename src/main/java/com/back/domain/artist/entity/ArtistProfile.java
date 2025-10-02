package com.back.domain.artist.entity;

import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작가 프로필 엔티티
 * - 작가 신청 승인 시 생성됨
 * - User와 1:1 매핑
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "artist_profiles")
public class ArtistProfile extends BaseEntity {

    // ==== 연관 관계 매핑 ==== //

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_application_id", nullable = false)
    private ArtistApplication artistApplication;

    // ===== 작가 기본 정보 ===== //
    @Column(nullable = false, length = 20)
    private String artistName; // 작가명

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String mainProducts; // 주력 상품

    @Column(length = 100)
    private String snsAccount; // SNS 계정

    // ==== 사업자 정보 ==== //

    @Column(length = 200)
    private String businessAddress; // 사업장 주소

    @Column(length = 200)
    private String businessAddressDetail; // 사업장 상세주소(동/호수)

    @Column(length = 10)
    private String businessZipCode; // 사업장 우편번호

    @Column(length = 20)
    private String managerPhone; // 담당자 연락처

    // ==== 은행 정보 ==== //

    @Column(length = 50)
    private String bankName; // 은행명

    @Column(length = 50)
    private String bankAccount; // 계좌번호

    @Column(length = 20)
    private String accountName; // 예금주명

    // ==== 작가 프로필 전용 정보 ==== //

    @Column(length = 2000)
    private String description; // 작가 소개

    @Column(length = 500)
    private String profileImageUrl; // 프로필 이미지 URL

    // ==== 통계 정보 ==== //

    @Column(nullable = false)
    private Integer followerCount = 0; // 팔로워 수

    @Column(nullable = false)
    private Long totalSales = 0L; // 총 판매액

    @Column(nullable = false)
    private Integer productCount = 0; // 등록 상품 수

    @Builder
    public ArtistProfile(User user, ArtistApplication artistApplication,
                         String artistName, String email, String phone,
                         String mainProducts, String snsAccount,
                         String businessAddress, String businessAddressDetail, String businessZipCode,
                         String managerPhone,
                         String bankName, String bankAccount, String accountName,
                         String description, String profileImageUrl) {
        this.user = user;
        this.artistApplication = artistApplication;
        this.artistName = artistName;
        this.email = email;
        this.phone = phone;
        this.mainProducts = mainProducts;
        this.snsAccount = snsAccount;
        this.businessAddress = businessAddress;
        this.businessAddressDetail = businessAddressDetail;
        this.businessZipCode = businessZipCode;
        this.managerPhone = managerPhone;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.accountName = accountName;
        this.description = description;
        this.profileImageUrl = profileImageUrl;

        // 통계 정보 초기화
        this.followerCount = 0;
        this.totalSales = 0L;
        this.productCount = 0;
    }



    // ===== 정적 팩토리 메서드 ===== //

    /**
     * ArtistApplication으로부터 ArtistProfile 생성
     */
    public static ArtistProfile fromApplication(User user, ArtistApplication application) {
        return ArtistProfile.builder()
                .user(user)
                .artistApplication(application)
                .artistName(application.getArtistName())
                .email(application.getEmail())
                .phone(application.getPhone())
                .mainProducts(application.getMainProducts())
                .snsAccount(application.getSnsAccount())
                .businessAddress(application.getBusinessAddress())
                .businessAddressDetail(application.getBusinessAddressDetail())
                .businessZipCode(application.getBusinessZipCode())
                .managerPhone(application.getManagerPhone())
                .bankName(application.getBankName())
                .bankAccount(application.getBankAccount())
                .accountName(application.getAccountName())
                .build();
    }

    // ==== 프로필 정보 업데이트 메서드 ==== //

    public void updateProfile(String profileImageUrl, String artistName, String snsAccount,
                              String description, String businessAddress, String businessAddressDetail,
                              String businessZipCode, String accountName, String bankName,
                              String bankAccount, String managerPhone) {
        // 프로필 이미지
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }

        // 작가명
        if (artistName != null && !artistName.isBlank()) {
            this.artistName = artistName;
        }

        // SNS 계정
        if (snsAccount != null && !snsAccount.isBlank()) {
            this.snsAccount = snsAccount;
        }

        // 작가 소개
        if (description != null && !description.isBlank()) {
            this.description = description;
        }

        // 사업장 주소
        if (businessAddress != null && !businessAddress.isBlank()) {
            this.businessAddress = businessAddress;
        }

        if (businessAddressDetail != null && !businessAddressDetail.isBlank()) {
            this.businessAddressDetail = businessAddressDetail;
        }
        if (businessZipCode != null && !businessZipCode.isBlank()) {
            this.businessZipCode = businessZipCode;
        }

        // 예금주
        if (accountName != null && !accountName.isBlank()) {
            this.accountName = accountName;
        }

        // 은행명
        if (bankName != null && !bankName.isBlank()) {
            this.bankName = bankName;
        }

        // 계좌번호
        if (bankAccount != null && !bankAccount.isBlank()) {
            this.bankAccount = bankAccount;
        }

        // 연락처
        if (managerPhone != null && !managerPhone.isBlank()) {
            this.managerPhone = managerPhone;
        }
    }

    // ==== 통계 정보 업데이트 메서드 ==== //

    /**
     * 팔로워 수 증가
     */
    public void increaseFollowerCount() {
        this.followerCount++;
    }

    /**
     * 판매액 증가
     */
    public void addSales(Long amount) {
        if (amount <= 0) {
            throw new ServiceException("400", "판매액은 0보다 커야 합니다.");
        }
        this.totalSales += amount;
    }

    /**
     * 팔로워 수 감소
     */
    public void decreaseFollowerCount() {
        if (this.followerCount > 0) {
            this.followerCount--;
        }
    }

    /**
     * 상품 수 증가
     */
    public void increaseProductCount() {
        this.productCount++;
    }

    /**
     * 상품 수 감소
     */
    public void decreaseProductCount() {
        if (this.productCount > 0) {
            this.productCount--;
        }
    }

    // ==== 검증 메서드 ==== //

    /**
     * 본인 프로필인지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 본인 프로필이 아닌 경우 예외 발생
     */
    public void validateOwnership(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new ServiceException("403", "본인의 프로필만 수정할 수 있습니다.");
        }
    }

}
