package com.back.domain.follow.controller;

import com.back.domain.follow.dto.response.FollowResponse;
import com.back.domain.follow.dto.response.FollowerListResponse;
import com.back.domain.follow.dto.response.FollowingListResponse;
import com.back.domain.follow.service.FollowService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "팔로우", description = "작가 팔로우 관련 API")
public class FollowController {

    private final FollowService followService;

    /**
     * 작가 팔로우
     */
    @PostMapping("/artists/{artistId}")
    @Operation(
            summary = "작가 팔로우",
            description = "특정 작가를 팔로우합니다. 이미 팔로우 중인 경우 오류를 반환합니다."
    )
    public ResponseEntity<RsData<FollowResponse>> followArtist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "작가 프로필 ID", example = "1", required = true)
            @PathVariable Long artistId) {

        FollowResponse response = followService.followArtist(userDetails.getUserId(), artistId);

        return ResponseEntity.ok(
                RsData.of("200", "팔로우 성공", response)
        );
    }

    /**
     * 작가 언팔로우
     */
    @DeleteMapping("/artists/{artistId}")
    @Operation(
            summary = "작가 언팔로우",
            description = "특정 작가를 언팔로우합니다."
    )
    public ResponseEntity<RsData<Void>> unfollowArtist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "작가 프로필 ID", example = "1", required = true)
            @PathVariable Long artistId) {

        followService.unfollowArtist(userDetails.getUserId(), artistId);

        return ResponseEntity.ok(
                RsData.of("200", "언팔로우 성공")
        );
    }

    /**
     * 팔로우 상태 확인
     */
    @GetMapping("/artists/{artistId}/status")
    @Operation(
            summary = "팔로우 상태 확인",
            description = "현재 로그인한 사용자가 특정 작가를 팔로우 중인지 확인합니다."
    )
    public ResponseEntity<RsData<Boolean>> isFollowing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "작가 프로필 ID", example = "1", required = true)
            @PathVariable Long artistId) {

        boolean isFollowing = followService.isFollowing(userDetails.getUserId(), artistId);

        return ResponseEntity.ok(
                RsData.of("200", "팔로우 상태 조회 성공", isFollowing)
        );
    }

    /**
     * 내가 팔로우하는 작가 목록 조회
     */
    @GetMapping("/following")
    @Operation(
            summary = "내가 팔로우하는 작가 목록",
            description = "현재 로그인한 사용자가 팔로우하는 작가 목록을 조회합니다."
    )
    public ResponseEntity<RsData<List<FollowingListResponse>>> getFollowingList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FollowingListResponse> response = followService.getMyFollowingList(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "팔로잉 목록 조회 성공", response)
        );
    }

    /**
     * 내 팔로워 목록 조회 (작가 전용)
     */
    @GetMapping("/followers")
    @Operation(
            summary = "내 팔로워 목록 조회 (작가 전용)",
            description = "작가 본인의 팔로워 목록을 조회합니다. 작가가 아니거나 다른 작가의 팔로워는 조회할 수 없습니다."
    )
    public ResponseEntity<RsData<List<FollowerListResponse>>> getMyFollowerList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<FollowerListResponse> response = followService.getMyFollowerList(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "팔로워 목록 조회 성공", response)
        );
    }

    /**
     * 내가 팔로우하는 작가 수 조회
     */
    @GetMapping("/following/count")
    @Operation(
            summary = "팔로잉 수 조회",
            description = "현재 로그인한 사용자가 팔로우하는 작가의 수를 조회합니다."
    )
    public ResponseEntity<RsData<Long>> getFollowingCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        long count = followService.getFollowingCount(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "팔로잉 수 조회 성공", count)
        );
    }

    /**
     * 특정 작가의 팔로워 수 조회 (공개)
     */
    @GetMapping("/artists/{artistId}/followers/count")
    @Operation(
            summary = "작가의 팔로워 수 조회",
            description = "특정 작가의 팔로워 수를 조회합니다. (공개 API)"
    )
    public ResponseEntity<RsData<Long>> getFollowerCount(
            @Parameter(description = "작가 프로필 ID", example = "1", required = true)
            @PathVariable Long artistId) {

        long count = followService.getFollowerCount(artistId);

        return ResponseEntity.ok(
                RsData.of("200", "팔로워 수 조회 성공", count)
        );
    }
}
