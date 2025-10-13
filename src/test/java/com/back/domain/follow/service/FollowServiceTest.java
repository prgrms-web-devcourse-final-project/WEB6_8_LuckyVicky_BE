package com.back.domain.follow.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.follow.dto.response.FollowResponse;
import com.back.domain.follow.dto.response.FollowerListResponse;
import com.back.domain.follow.dto.response.FollowingListResponse;
import com.back.domain.follow.entity.Follow;
import com.back.domain.follow.repository.FollowRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
public class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @InjectMocks
    private FollowService followService;

    private User testUser;
    private User artistUser;
    private ArtistProfile testArtist;
    private Follow testFollow;

    @BeforeEach
    void setUp() {
        // 일반 사용자 생성
        testUser = User.createLocalUser("user@test.com", "password", "테스트유저", "010-1111-1111");
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 작가 계정 생성
        artistUser = User.createLocalUser("artist@test.com", "password", "작가유저", "010-2222-2222");
        ReflectionTestUtils.setField(artistUser, "id", 2L);
        artistUser.becomeArtist();

        // 작가 프로필 생성
        testArtist = ArtistProfile.builder()
                .user(artistUser)
                .artistApplication(null)
                .artistName("김작가")
                .mainProducts("도자기")
                .build();
        ReflectionTestUtils.setField(testArtist, "id", 1L);

        testArtist.increaseFollowerCount();

        // 팔로우 관계 생성
        testFollow = Follow.builder()
                .follower(testUser)
                .followingArtist(testArtist)
                .build();
        ReflectionTestUtils.setField(testFollow, "id", 1L);
    }

    @Nested
    @DisplayName("작가 팔로우 테스트")
    class FollowArtistTest {

        @Test
        @DisplayName("작가 팔로우 성공")
        void followArtist_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistProfileRepository.findById(1L)).willReturn(Optional.of(testArtist));
            given(followRepository.existsByFollowerIdAndFollowingArtistId(1L, 1L)).willReturn(false);
            given(followRepository.save(any(Follow.class))).willReturn(testFollow);

            Integer initialFollowerCount = testArtist.getFollowerCount();

            // when
            FollowResponse response = followService.followArtist(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.artistId()).isEqualTo(1L);
            assertThat(response.artistName()).isEqualTo("김작가");
            assertThat(response.isFollowing()).isTrue();
            assertThat(testArtist.getFollowerCount()).isEqualTo(initialFollowerCount + 1);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("작가 팔로우 실패 - 사용자 없음")
        void followArtist_UserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.followArtist(999L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("작가 팔로우 실패 - 작가 없음")
        void followArtist_ArtistNotFound() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistProfileRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.followArtist(1L, 999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("작가를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("작가 팔로우 실패 - 이미 팔로우 중")
        void followArtist_AlreadyFollowing() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistProfileRepository.findById(1L)).willReturn(Optional.of(testArtist));
            given(followRepository.existsByFollowerIdAndFollowingArtistId(1L, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> followService.followArtist(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 팔로우 중입니다.");
                    });
        }

        @Test
        @DisplayName("작가 팔로우 실패 - 자기 자신 팔로우")
        void followArtist_SelfFollow() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(artistUser));
            given(artistProfileRepository.findById(1L)).willReturn(Optional.of(testArtist));
            given(followRepository.existsByFollowerIdAndFollowingArtistId(2L, 1L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> followService.followArtist(2L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("자기 자신을 팔로우할 수 없습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("작가 언팔로우 테스트")
    class UnfollowArtistTest {

        @Test
        @DisplayName("작가 언팔로우 성공")
        void unfollowArtist_Success() {
            // given
            given(followRepository.findByFollowerIdAndFollowingArtistId(1L, 1L))
                    .willReturn(Optional.of(testFollow));
            given(artistProfileRepository.findById(1L)).willReturn(Optional.of(testArtist));

            Integer initialFollowerCount = testArtist.getFollowerCount();

            // when
            followService.unfollowArtist(1L, 1L);

            // then
            assertThat(testArtist.getFollowerCount()).isEqualTo(initialFollowerCount - 1);
            verify(followRepository).delete(testFollow);
        }

        @Test
        @DisplayName("작가 언팔로우 실패 - 팔로우 관계 없음")
        void unfollowArtist_NotFollowing() {
            // given
            given(followRepository.findByFollowerIdAndFollowingArtistId(1L, 1L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.unfollowArtist(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("팔로우 관계를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("작가 언팔로우 실패 - 권한 없음")
        void unfollowArtist_Forbidden() {
            // given
            given(followRepository.findByFollowerIdAndFollowingArtistId(999L, 1L))
                    .willReturn(Optional.of(testFollow));

            // when & then
            assertThatThrownBy(() -> followService.unfollowArtist(999L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("403");
                        assertThat(serviceEx.getMsg()).isEqualTo("본인이 팔로우한 관계만 삭제할 수 있습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("팔로우 상태 조회 테스트")
    class IsFollowingTest {

        @Test
        @DisplayName("팔로우 상태 확인 - 팔로우 중")
        void isFollowing_True() {
            // given
            given(followRepository.existsByFollowerIdAndFollowingArtistId(1L, 1L))
                    .willReturn(true);

            // when
            boolean isFollowing = followService.isFollowing(1L, 1L);

            // then
            assertThat(isFollowing).isTrue();
        }

        @Test
        @DisplayName("팔로우 상태 확인 - 팔로우 안 함")
        void isFollowing_False() {
            // given
            given(followRepository.existsByFollowerIdAndFollowingArtistId(1L, 1L))
                    .willReturn(false);

            // when
            boolean isFollowing = followService.isFollowing(1L, 1L);

            // then
            assertThat(isFollowing).isFalse();
        }
    }

    @Nested
    @DisplayName("팔로잉 목록 조회 테스트")
    class GetFollowingListTest {

        @Test
        @DisplayName("팔로잉 목록 조회 성공")
        void getFollowingList_Success() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);
            given(followRepository.findFollowingsByFollowerId(1L))
                    .willReturn(List.of(testFollow));

            // when
            List<FollowingListResponse> response = followService.getMyFollowingList(1L);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).artistName()).isEqualTo("김작가");
            verify(followRepository).findFollowingsByFollowerId(1L);
        }

        @Test
        @DisplayName("팔로잉 목록 조회 - 빈 목록")
        void getFollowingList_Empty() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);
            given(followRepository.findFollowingsByFollowerId(1L))
                    .willReturn(List.of());

            // when
            List<FollowingListResponse> response = followService.getMyFollowingList(1L);

            // then
            assertThat(response).isEmpty();
        }

        @Test
        @DisplayName("팔로잉 목록 조회 실패 - 사용자 없음")
        void getFollowingList_UserNotFound() {
            // given
            given(userRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> followService.getMyFollowingList(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                    });
        }
    }

    @Nested
    @DisplayName("팔로워 목록 조회 테스트")
    class GetFollowerListTest {

        @Test
        @DisplayName("팔로워 목록 조회 성공")
        void getFollowerList_Success() {
            // given
            given(artistProfileRepository.findById(1L)).willReturn(Optional.of(testArtist));
            given(followRepository.findFollowersByArtistId(1L))
                    .willReturn(List.of(testFollow));

            // when
            List<FollowerListResponse> response = followService.getMyFollowerList(1L);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).userName()).isEqualTo("테스트유저");
            verify(followRepository).findFollowersByArtistId(1L);
        }

        @Test
        @DisplayName("팔로워 목록 조회 실패 - 작가 없음")
        void getFollowerList_ArtistNotFound() {
            // given
            given(artistProfileRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.getMyFollowerList(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                    });
        }
    }

    @Nested
    @DisplayName("팔로우 카운트 테스트")
    class GetCountTest {

        @Test
        @DisplayName("팔로잉 수 조회")
        void getFollowingCount() {
            // given
            given(followRepository.countByFollowerId(1L)).willReturn(5L);

            // when
            long count = followService.getFollowingCount(1L);

            // then
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("팔로워 수 조회")
        void getFollowerCount() {
            // given
            given(followRepository.countByFollowingArtistId(1L)).willReturn(10L);

            // when
            long count = followService.getFollowerCount(1L);

            // then
            assertThat(count).isEqualTo(10L);
        }
    }
}