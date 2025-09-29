package com.back.domain.user.service;

import com.back.domain.user.entity.Grade;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.user.DevUserService;
import com.back.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("DevUserService 통합 테스트")
@Transactional // 테스트 후 롤백
class DevUserServiceTest {

    @Autowired
    private DevUserService devUserService;

    @Autowired
    private UserRepository userRepository;

    @MockBean // Rq는 Mock으로 처리 (SecurityContext 복잡함을 피하기 위해)
    private Rq rq;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // TestInitData에서 생성된 사용자들을 조회
        testUser1 = userRepository.findByEmail("user1@user.com")
                .orElseThrow(() -> new IllegalStateException("TestInitData가 실행되지 않았습니다."));
        testUser2 = userRepository.findByEmail("user2@user.com")
                .orElseThrow(() -> new IllegalStateException("TestInitData가 실행되지 않았습니다."));
        testUser3 = userRepository.findByEmail("user3@user.com")
                .orElseThrow(() -> new IllegalStateException("TestInitData가 실행되지 않았습니다."));
    }

    @Test
    @DisplayName("성공: 현재 로그인된 사용자(user1)의 역할을 ARTIST로 변경")
    void changeMyRole_Success() {
        // given
        given(rq.getUserId()).willReturn(testUser1.getId());

        // when
        devUserService.changeMyRole(Role.ARTIST);

        // then - DB에서 다시 조회해서 실제로 변경되었는지 확인
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ARTIST);
        assertThat(updatedUser.getIsArtistVerified()).isTrue();
        assertThat(updatedUser.getArtistVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("성공: 현재 로그인된 사용자(user2)의 등급을 TREE로 변경")
    void changeMyGrade_Success() {
        // given
        given(rq.getUserId()).willReturn(testUser2.getId());

        // when
        devUserService.changeMyGrade(Grade.TREE);

        // then - DB에서 다시 조회해서 실제로 변경되었는지 확인
        User updatedUser = userRepository.findById(testUser2.getId()).orElseThrow();
        assertThat(updatedUser.getGrade()).isEqualTo(Grade.TREE);
    }

    @Test
    @DisplayName("성공: 현재 로그인된 사용자(user3)의 돈을 500000원으로 설정")
    void setMyMoney_Success() {
        // given
        given(rq.getUserId()).willReturn(testUser3.getId());
        int newMoney = 500000;

        // when
        devUserService.setMyMoney(newMoney);

        // then - DB에서 다시 조회해서 실제로 변경되었는지 확인
        User updatedUser = userRepository.findById(testUser3.getId()).orElseThrow();
        assertThat(updatedUser.getMoney()).isEqualTo(newMoney);
    }

    @Test
    @DisplayName("성공: 이메일로 user1을 찾아서 역할을 ADMIN으로 변경")
    void changeUserRoleByEmail_Success() {
        // when
        devUserService.changeUserRoleByEmail("user1@user.com", Role.ADMIN);

        // then - DB에서 다시 조회해서 실제로 변경되었는지 확인
        User updatedUser = userRepository.findByEmail("user1@user.com").orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("성공: user2의 ID로 역할을 ROOT로 변경")
    void changeUserRole_Success() {
        // when
        devUserService.changeUserRole(testUser2.getId(), Role.ROOT);

        // then - DB에서 다시 조회해서 실제로 변경되었는지 확인
        User updatedUser = userRepository.findById(testUser2.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.ROOT);
    }

    @Test
    @DisplayName("성공: ARTIST에서 USER로 역할 변경 시 아티스트 인증 해제 확인")
    void changeRole_ArtistToUser_Success() {
        // given - 먼저 user3을 ARTIST로 만들고
        devUserService.changeUserRole(testUser3.getId(), Role.ARTIST);
        User artistUser = userRepository.findById(testUser3.getId()).orElseThrow();
        assertThat(artistUser.getRole()).isEqualTo(Role.ARTIST);
        assertThat(artistUser.getIsArtistVerified()).isTrue();

        // when - USER로 다시 변경
        devUserService.changeUserRole(testUser3.getId(), Role.USER);

        // then - 아티스트 인증이 해제되었는지 확인
        User updatedUser = userRepository.findById(testUser3.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(Role.USER);
        assertThat(updatedUser.getIsArtistVerified()).isFalse();
        assertThat(updatedUser.getArtistVerifiedAt()).isNull();
    }

    @Test
    @DisplayName("실패: 로그인되지 않은 상태에서 역할 변경 시도")
    void changeMyRole_Fail_NotLoggedIn() {
        // given
        given(rq.getUserId()).willReturn(null); // 로그인되지 않음

        // when & then
        assertThatThrownBy(() -> devUserService.changeMyRole(Role.ARTIST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그인된 사용자가 없습니다.");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자ID로 역할 변경 시도")
    void changeMyRole_Fail_UserNotFound() {
        // given
        Long nonExistentUserId = 99999L;
        given(rq.getUserId()).willReturn(nonExistentUserId);

        // when & then
        assertThatThrownBy(() -> devUserService.changeMyRole(Role.ARTIST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 역할 변경 시도")
    void changeUserRoleByEmail_Fail_UserNotFound() {
        // given
        String nonExistentEmail = "nonexistent@example.com";

        // when & then
        assertThatThrownBy(() -> devUserService.changeUserRoleByEmail(nonExistentEmail, Role.ARTIST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다: " + nonExistentEmail);
    }

    @Test
    @DisplayName("실패: 로그인되지 않은 상태에서 등급 변경 시도")
    void changeMyGrade_Fail_NotLoggedIn() {
        // given
        given(rq.getUserId()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> devUserService.changeMyGrade(Grade.TREE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그인된 사용자가 없습니다.");
    }

    @Test
    @DisplayName("실패: 로그인되지 않은 상태에서 돈 설정 시도")
    void setMyMoney_Fail_NotLoggedIn() {
        // given
        given(rq.getUserId()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> devUserService.setMyMoney(100000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그인된 사용자가 없습니다.");
    }

    @Test
    @DisplayName("데이터 확인: TestInitData로 생성된 사용자들이 올바르게 존재하는지 검증")
    void verifyTestInitData() {
        // TestInitData가 제대로 실행되었는지 확인
        assertThat(testUser1.getEmail()).isEqualTo("user1@user.com");
        assertThat(testUser1.getName()).isEqualTo("유저1");

        assertThat(testUser2.getEmail()).isEqualTo("user2@user.com");
        assertThat(testUser2.getName()).isEqualTo("유저2");

        assertThat(testUser3.getEmail()).isEqualTo("user3@user.com");
        assertThat(testUser3.getName()).isEqualTo("유저3");

        // 총 3명의 사용자가 생성되었는지 확인
        long userCount = userRepository.count();
        assertThat(userCount).isGreaterThanOrEqualTo(3);
    }
}