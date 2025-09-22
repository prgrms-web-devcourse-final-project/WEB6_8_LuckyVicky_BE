package com.back.domain.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Users")
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

    private String agreementIp;

    // 정적 팩토리 메서드 - 로컬 회원가입
    public static User createLocalUser(String email, String password, String name, String phone,
                                       String agreementIp) {
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
        user.agreementIp = agreementIp;    // 외부에서 받음
        return user;
    }

    // TODO: 정적 팩토리 메서드 - 소셜 로그인 구현
}
