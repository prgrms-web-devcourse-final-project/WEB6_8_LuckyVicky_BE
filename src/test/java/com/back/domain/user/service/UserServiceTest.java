package com.back.domain.user.service;


import com.back.domain.user.entity.Grade;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.Status;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.createLocalUser(
                "test@example.com",
                "encodedPassword",
                "테스트유저",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class GetUserTest {

        @Test
        @DisplayName("ID로 사용자 조회 성공")
        void getUserById_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            User result = userService.getUserById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 실패")
        void getUserById_NotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("이메일로 사용자 조회 성공")
        void getUserByEmail_Success() {
            // given
            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));

            // when
            User result = userService.getUserByEmail("test@example.com");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 실패")
        void getUserByEmail_NotFound() {
            // given
            given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("작가 자격 관리 테스트")
    class ArtistRoleTest {

        @Test
        @DisplayName("작가 자격 획득 성공")
        void becomeToArtist_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.becomeToArtist(1L);

            // then
            assertThat(testUser.getRole()).isEqualTo(Role.ARTIST);
            assertThat(testUser.getIsArtistVerified()).isTrue();
            assertThat(testUser.getArtistVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 작가인 사용자 작가 자격 획득 실패")
        void becomeToArtist_AlreadyArtist() {
            // given
            testUser.becomeArtist(); // 이미 작가로 설정
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.becomeToArtist(1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 작가 권한을 가진 사용자입니다.");
                    });
        }

        @Test
        @DisplayName("작가 자격 해제 성공")
        void revokeArtistRole_Success() {
            // given
            testUser.becomeArtist(); // 먼저 작가로 만들기
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.revokeArtistRole(1L, 100L);

            // then
            assertThat(testUser.getRole()).isEqualTo(Role.USER);
            assertThat(testUser.getIsArtistVerified()).isFalse();
            assertThat(testUser.getArtistVerifiedAt()).isNull();
        }

        @Test
        @DisplayName("작가가 아닌 사용자 작가 자격 해제 실패")
        void revokeArtistRole_NotArtist() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.revokeArtistRole(1L, 100L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("작가 권한이 없는 사용자입니다.");
                    });
        }
    }

    @Nested
    @DisplayName("머니 관리 테스트")
    class MoneyManagementTest {

        @Test
        @DisplayName("머니 증가 성공")
        void addMoney_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            int initialMoney = testUser.getMoney();

            // when
            userService.addMoney(1L, 10000, "테스트 충전");

            // then
            assertThat(testUser.getMoney()).isEqualTo(initialMoney + 10000);
        }

        @Test
        @DisplayName("0 이하 금액 증가 실패")
        void addMoney_InvalidAmount() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.addMoney(1L, 0, "테스트"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("증가할 금액은 0보다 커야 합니다.");
                    });
        }

        @Test
        @DisplayName("머니 차감 성공")
        void deductMoney_Success() {
            // given
            testUser.addMoney(50000); // 먼저 머니 추가
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            int initialMoney = testUser.getMoney();

            // when
            userService.deductMoney(1L, 10000, "테스트 사용");

            // then
            assertThat(testUser.getMoney()).isEqualTo(initialMoney - 10000);
        }

        @Test
        @DisplayName("보유 금액 부족으로 차감 실패")
        void deductMoney_InsufficientBalance() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.deductMoney(1L, 100000, "테스트"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("보유 금액이 부족합니다.");
                    });
        }
    }

    @Nested
    @DisplayName("포인트 관리 테스트")
    class PointManagementTest {

        @Test
        @DisplayName("포인트 증가 성공")
        void addPoint_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            int initialPoint = testUser.getPoint();

            // when
            userService.addPoint(1L, 500, "리뷰 작성 보상");

            // then
            assertThat(testUser.getPoint()).isEqualTo(initialPoint + 500);
        }

        @Test
        @DisplayName("0 이하 포인트 증가 실패")
        void addPoint_InvalidAmount() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.addPoint(1L, -100, "테스트"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("증가할 포인트는 0보다 커야 합니다.");
                    });
        }

        @Test
        @DisplayName("포인트 차감 성공")
        void deductPoint_Success() {
            // given
            testUser.addPoint(1000); // 먼저 포인트 추가
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            int initialPoint = testUser.getPoint();

            // when
            userService.deductPoint(1L, 300, "쿠폰 사용");

            // then
            assertThat(testUser.getPoint()).isEqualTo(initialPoint - 300);
        }

        @Test
        @DisplayName("보유 포인트 부족으로 차감 실패")
        void deductPoint_InsufficientBalance() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.deductPoint(1L, 1000, "테스트"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("보유 포인트가 부족합니다.");
                    });
        }
    }

    @Nested
    @DisplayName("등급 관리 테스트")
    class GradeManagementTest {

        @Test
        @DisplayName("등급 업그레이드 성공")
        void upgradeGrade_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            Grade newGrade = Grade.TREE;

            // when
            userService.upgradeGrade(1L, newGrade);

            // then
            assertThat(testUser.getGrade()).isEqualTo(Grade.TREE);
        }
    }

    @Nested
    @DisplayName("계정 상태 관리 테스트")
    class StatusManagementTest {

        @Test
        @DisplayName("계정 상태 변경 성공")
        void changeStatus_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.changeStatus(1L, Status.BLOCKED, 100L);

            // then
            assertThat(testUser.getStatus()).isEqualTo(Status.BLOCKED);
        }

        @Test
        @DisplayName("계정 차단 성공")
        void blockUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.blockUser(1L, 100L, "스팸 행위");

            // then
            assertThat(testUser.getStatus()).isEqualTo(Status.BLOCKED);
        }

        @Test
        @DisplayName("이미 차단된 계정 재차단 실패")
        void blockUser_AlreadyBlocked() {
            // given
            testUser.changeStatus(Status.BLOCKED);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.blockUser(1L, 100L, "중복 차단"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 차단된 사용자입니다.");
                    });
        }

        @Test
        @DisplayName("계정 차단 해제 성공")
        void unblockUser_Success() {
            // given
            testUser.changeStatus(Status.BLOCKED);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.unblockUser(1L, 100L);

            // then
            assertThat(testUser.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("차단되지 않은 계정 차단 해제 실패")
        void unblockUser_NotBlocked() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.unblockUser(1L, 100L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("차단되지 않은 사용자입니다.");
                    });
        }

        @Test
        @DisplayName("계정 삭제 성공")
        void deleteUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userService.deleteUser(1L);

            // then
            assertThat(testUser.getStatus()).isEqualTo(Status.DELETED);
            assertThat(testUser.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 삭제된 계정 재삭제 실패")
        void deleteUser_AlreadyDeleted() {
            // given
            testUser.changeStatus(Status.DELETED);
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 삭제된 계정입니다.");
                    });
        }
    }
}