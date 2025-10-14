package com.back.domain.user.entity;

import com.back.global.exception.ServiceException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(unique = true, nullable = true) // OAuth Kakao 사용자를 위해 nullable 허용
    private String email;

    @Column(nullable = true) // OAuth 사용자를 위해 nullable 허용
    private String password;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = true) // OAuth 사용자를 위해 nullable 허용
    private String phone;

    private String profileImageUrl;

    private String address;

    private String detailAddress;

    private String zipCode;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    private int money;

    private int point;

    private Boolean isArtistVerified;

    private LocalDateTime artistVerifiedAt;

    private LocalDateTime deletedAt;

    private Boolean privacyRequiredAgreed;

    private Boolean marketingAgreed;

    private LocalDateTime termsAgreedAt;


    // ===== 정적 팩토리 메서드 ===== //

    /**
     * 로컬 회원가입 사용자 생성
     */
    public static User createLocalUser(String email, String password, String name, String phone) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.phone = phone;
        user.role = Role.USER;
        user.grade = Grade.SPROUT;
        user.status = Status.ACTIVE;
        user.provider = Provider.LOCAL;
        user.money = 0;
        user.point = 0;
        user.isArtistVerified = false;
        user.privacyRequiredAgreed = true;  // 회원가입 시 필수 동의
        user.marketingAgreed = false;       // 선택 동의 (기본값)
        user.termsAgreedAt = LocalDateTime.now();  // 현재 시간
        return user;
    }

    /**
     * OAuth 회원가입 사용자 생성
     */
    public static User createOAuthUser(String email, String name, Provider provider, String providerId) {
        User user = new User();
        user.email = email;

        // Provider를 포함한 고유한 이름 생성 (예: "홍길동_GOOGLE")
        user.name = name + "_" + provider.name();

        user.provider = provider;
        user.providerId = providerId;
        user.password = null;  // 소셜 로그인은 비밀번호 없음
        user.role = Role.USER;
        user.grade = Grade.SPROUT;
        user.status = Status.ACTIVE;
        user.money = 0;
        user.point = 0;
        user.isArtistVerified = false;
        user.privacyRequiredAgreed = true;  // OAuth 로그인 시 필수 동의로 간주
        user.marketingAgreed = false;
        user.termsAgreedAt = LocalDateTime.now();
        return user;
    }

    // ===== 역할 관련 메서드 ===== //

    /**
     * 로그인 가능한 역할 목록 반환
     */
    public List<Role> getAvailableLoginRoles() {
        return role.getAvailableRoles();
    }

    /**
     * 특정 역할로 로그인 가능한지 확인
     */
    public boolean canLoginAs(Role targetRole) {
        return role.canLoginAs(targetRole);
    }

    /**
     * 관리자 권한 보유 여부 확인
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * 작가 권한 보유 여부 확인 (인증 완료 필수)
     */
    public boolean isArtist() {
        return Role.ARTIST.equals(this.role) && Boolean.TRUE.equals(this.isArtistVerified);
    }

    /**
     * 작가 자격 획득 (신청 승인 시 사용)
     */
    public void becomeArtist() {
        this.role = Role.ARTIST;
        this.grade = Grade.GUARDIAN;
        this.isArtistVerified = true;
        this.artistVerifiedAt = LocalDateTime.now();
    }

    /**
     * 작가 자격 상실 (관리자 권한 필요)
     */
    public void revokeArtistRole() {
        if (!Role.ARTIST.equals(this.role)) {
            throw new ServiceException("400", "작가 권한이 없는 사용자입니다.");
        }

        this.role = Role.USER;
        this.grade = Grade.SPROUT;
        this.isArtistVerified = false;
        this.artistVerifiedAt = null;
    }

    /**
     * 관리자 권한 획득 (개발용)
     */
    public void becomeAdmin() {
        this.role = Role.ADMIN;
    }

    // ===== 검증 메서드 ===== //

    /**
     * 보유 금액이 충분한지 확인
     */
    public boolean hasEnoughMoney(int amount) {
        return this.money >= amount;
    }

    /**
     * 본인 확인 (ID 비교)
     */
    public boolean isSameUser(Long userId) {
        return userId != null && this.getId().equals(userId);
    }

    /**
     * 본인 확인 (User 객체 비교)
     */
    public boolean isSameUser(User user) {
        return user != null && this.getId().equals(user.getId());
    }

    // ===== 머니/포인트 관리 ===== //

    /**
     * 머니 증가
     * @param amount 증가할 금액
     */
    public void addMoney(int amount) {
        if (amount <= 0) {
            throw new ServiceException("400", "증가할 금액은 0보다 커야 합니다.");
        }
        this.money += amount;
    }

    /**
     * 머니 차감
     * @param amount 차감할 금액
     */
    public void deductMoney(int amount) {
        if (amount <= 0) {
            throw new ServiceException("400", "차감할 금액은 0보다 커야 합니다.");
        }
        if (this.money < amount) {
            throw new ServiceException("400", "보유 금액이 부족합니다.");
        }
        this.money -= amount;
    }

    /**
     * 포인트 증가
     * @param amount 증가할 포인트
     */
    public void addPoint(int amount) {
        if (amount <= 0) {
            throw new ServiceException("400", "증가할 포인트는 0보다 커야 합니다.");
        }
        this.point += amount;
    }

    /**
     * 포인트 차감
     * @param amount 차감할 포인트
     */
    public void deductPoint(int amount) {
        if (amount <= 0) {
            throw new ServiceException("400", "차감할 포인트는 0보다 커야 합니다.");
        }
        if (this.point < amount) {
            throw new ServiceException("400", "보유 포인트가 부족합니다.");
        }
        this.point -= amount;
    }

    // ===== 등급/계정 상태 관리 ===== //

    /**
     * 등급 업그레이드
     */
    public void upgradeGrade(Grade newGrade) {
        // TODO: 등급 업그레이드 로직 (구매 금액, 활동 등 기준)
        this.grade = newGrade;
    }

    /**
     * 계정 상태 변경
     */
    public void changeStatus(Status newStatus) {
        this.status = newStatus;

        if (Status.DELETED.equals(newStatus)) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    /**
     * 프로필 정보 업데이트
     */
    public void updateProfile(String name, String phone, String address,
                              String detailAddress, String zipCode, String profileImageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (detailAddress != null && !detailAddress.isBlank()) {
            this.detailAddress = detailAddress;
        }
        if (zipCode != null && !zipCode.isBlank()) {
            this.zipCode = zipCode;
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // ===== OAuth 관련 메서드 ===== //

    /**
     * OAuth 사용자 여부 확인
     */
    public boolean isOAuthUser() {
        return !Provider.LOCAL.equals(this.provider);
    }

    /**
     * OAuth 프로필 정보 업데이트 (재로그인 시)
     */
    public void updateOAuthProfile(String name, String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * OAuth 사용자의 추가 정보 입력 필요 여부 확인
     */
    public boolean needsAdditionalInfo() {
        return isOAuthUser() && (this.phone == null || this.phone.isBlank());
    }

}
