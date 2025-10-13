package com.back.domain.follow.controller;

import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.follow.entity.Follow;
import com.back.domain.follow.repository.FollowRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("팔로우 컨트롤러 테스트")
public class FollowControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtistProfileRepository artistProfileRepository;

    @Autowired
    private ArtistApplicationRepository artistApplicationRepository;

    @Autowired
    private FollowRepository followRepository;

    private ArtistProfile testArtistProfile;

    @BeforeEach
    void setUp() {
        User artist = userRepository.findByEmail("artist1@artist.com").orElseThrow();

        testArtistProfile = artistProfileRepository.findByUserId(artist.getId())
                .orElseGet(() -> {
                    // 1. 더미 신청서 생성
                    ArtistApplication dummyApplication = ArtistApplication.builder()
                            .user(artist)
                            .ownerName("테스트작가")
                            .email(artist.getEmail())
                            .phone(artist.getPhone())
                            .artistName("테스트작가")
                            .build();
                    ArtistApplication savedApplication = artistApplicationRepository.save(dummyApplication);

                    // 2. 프로필 생성
                    ArtistProfile profile = ArtistProfile.builder()
                            .user(artist)
                            .artistApplication(savedApplication)  // ← 연결
                            .artistName("테스트작가")
                            .mainProducts("도자기")
                            .build();
                    return artistProfileRepository.save(profile);
                });
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("1. 작가 팔로우 - 성공")
    void followArtist_Success() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(post("/api/follows/artists/" + testArtistProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("팔로우 성공"));

        // DB 확인
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        boolean exists = followRepository.existsByFollowerIdAndFollowingArtistId(
                user.getId(), testArtistProfile.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("2. 작가 팔로우 - 실패 (미인증)")
    void followArtist_Unauthorized() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(post("/api/follows/artists/" + testArtistProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("3. 작가 언팔로우 - 성공")
    void unfollowArtist_Success() throws Exception {
        // given - 먼저 팔로우
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        Follow follow = Follow.create(user, testArtistProfile);
        followRepository.save(follow);
        testArtistProfile.increaseFollowerCount();

        // when
        ResultActions resultActions = mvc.perform(delete("/api/follows/artists/" + testArtistProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("언팔로우 성공"));

        // DB 확인
        boolean exists = followRepository.existsByFollowerIdAndFollowingArtistId(
                user.getId(), testArtistProfile.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("4. 작가 언팔로우 - 실패 (팔로우하지 않음)")
    void unfollowArtist_NotFollowing() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/follows/artists/" + testArtistProfile.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("5. 팔로우 상태 확인 - 팔로우 중")
    void isFollowing_True() throws Exception {
        // given
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        Follow follow = Follow.create(user, testArtistProfile);
        followRepository.save(follow);

        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/follows/artists/" + testArtistProfile.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("6. 팔로우 상태 확인 - 팔로우 안 함")
    void isFollowing_False() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/follows/artists/" + testArtistProfile.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("7. 팔로잉 목록 조회 - 성공")
    void getFollowingList_Success() throws Exception {
        // given
        User user = userRepository.findByEmail("user2@user.com").orElseThrow();
        Follow follow = Follow.create(user, testArtistProfile);
        followRepository.save(follow);

        // when
        ResultActions resultActions = mvc.perform(get("/api/follows/following")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("8. 팔로잉 목록 조회 - 빈 목록")
    void getFollowingList_Empty() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/follows/following")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithUserDetails("artist1@artist.com")
    @DisplayName("9. 팔로워 목록 조회 - 성공 (작가)")
    void getFollowerList_Success() throws Exception {
        // given
        User user2 = userRepository.findByEmail("user2@user.com").orElseThrow();
        Follow follow = Follow.create(user2, testArtistProfile);
        followRepository.save(follow);

        // when - user1(작가) 본인이 조회
        ResultActions resultActions = mvc.perform(get("/api/follows/followers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @WithUserDetails("user2@user.com")  // ← user2는 일반 사용자
    @DisplayName("10. 팔로워 목록 조회 - 실패 (작가 아님)")
    void getFollowerList_NotArtist() throws Exception {
        // when - 일반 사용자가 조회 시도
        ResultActions resultActions = mvc.perform(get("/api/follows/followers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    @Test
    @DisplayName("11. 팔로워 수 조회 - 성공 (공개 API)")
    void getFollowerCount_Success() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                        get("/api/follows/artists/" + testArtistProfile.getId() + "/followers/count")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @WithUserDetails("user2@user.com")
    @DisplayName("12. 팔로잉 수 조회 - 성공")
    void getFollowingCount_Success() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/follows/following/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isNumber());
    }
}