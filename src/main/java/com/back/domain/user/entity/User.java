package com.back.domain.user.entity;

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

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String phone;

    private String profileImageUrl;

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

    // 정적 팩토리 메서드 - 로컬 회원가입
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
        user.privacyRequiredAgreed = true;  // 회원가입 시 필수 동의
        user.marketingAgreed = false;       // 선택 동의 (기본값)
        user.termsAgreedAt = LocalDateTime.now();  // 현재 시간
        return user;
    }

    // 로그인 가능한 역할 목록 반환
    public List<Role> getAvailableLoginRoles() {
        return role.getAvailableRoles();
    }

    // 특정 역할로 로그인 가능한지 확인
    public boolean canLoginAs(Role targetRole) {
        return role.canLoginAs(targetRole);
    }


    /**
     * Tell, Don't Ask 메서드들
     */

    // 관리자인지 확인
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    // 아티스트인지 확인
    public boolean isArtist() {
        return Role.ARTIST.equals(this.role) && Boolean.TRUE.equals(this.isArtistVerified);
    }

    // 돈이 충분한지 확인
    public boolean hasEnoughMoney(int amount) {
        return this.money >= amount;
    }

    // 본인인지 확인 (ID 비교)
    public boolean isSameUser(Long userId) {
        return userId != null && this.getId().equals(userId);
    }

    // 본인인지 확인 (User 객체 비교)
    public boolean isSameUser(User user) {
        return user != null && this.getId().equals(user.getId());
    }


    /**
     * 개발용 임시 메서드들 (추후 삭제 예정)
     * TODO: 작가 신청/승인 API 완성 후 삭제
     */

    // 개발용: 역할 변경 (임시)
    public void changeRole(Role newRole) {
        Role previousRole = this.role;
        this.role = newRole;

        switch (newRole) {
            case ARTIST:
                this.isArtistVerified = true;
                this.artistVerifiedAt = LocalDateTime.now();
                break;

            case ADMIN:
            case ROOT:
                // 관리자 권한으로 변경 시 아티스트 인증은 유지하되,
                // 기존에 ARTIST가 아니었다면 인증 해제
                if (!Role.ARTIST.equals(previousRole)) {
                    this.isArtistVerified = false;
                    this.artistVerifiedAt = null;
                }
                break;

            case USER:
            default:
                // 일반 사용자나 기타 역할로 변경 시 아티스트 인증 해제
                this.isArtistVerified = false;
                this.artistVerifiedAt = null;
                break;
        }
    }

    // 개발용: 돈 설정 (임시)
    public void setMoney(int money) {
        this.money = Math.max(0, money);
    }


    // TODO: 정적 팩토리 메서드 - 소셜 로그인 구현
}
