package com.back.domain.user.controller;

import com.back.domain.user.dto.request.UpdateUserInfoRequest;
import com.back.domain.user.dto.response.UserProfileResponse;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<RsData<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("프로필 조회 - userId: {}", userDetails.getUserId());

        UserProfileResponse response = userService.getUserProfile(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "프로필 조회 성공", response)
        );
    }

    /**
     * 내 정보 수정
     */
    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보(프로필 이미지/닉네임/전화번호/주소/비밀번호)를 수정합니다.")
    public ResponseEntity<RsData<UserProfileResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserInfoRequest request) {

        log.info("사용자 정보 수정 - userId: {}, nickname: {}, hasPassword: {}",
                userDetails.getUserId(), request.name(), request.password() != null);

        UserProfileResponse response = userService.updateUserInfo(userDetails.getUserId(), request);

        return ResponseEntity.ok(
                RsData.of("200", "사용자 정보 수정 성공", response)
        );
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 회원 탈퇴를 진행합니다. 계정 상태가 DELETED로 변경됩니다.")
    public ResponseEntity<RsData<Void>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("회원 탈퇴 요청 - userId: {}", userDetails.getUserId());

        userService.deleteUser(userDetails.getUserId());

        return ResponseEntity.ok(
                RsData.of("200", "회원 탈퇴 성공")
        );
    }

    /**
     * 특정 사용자 프로필 조회 (공개 프로필)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 공개 프로필 조회", description = "특정 사용자의 공개 프로필 정보를 조회합니다.")
    public ResponseEntity<RsData<UserProfileResponse>> getUserPublicProfile(
            @PathVariable Long userId
    ) {
        log.info("공개 프로필 조회 - userId: {}", userId);

        UserProfileResponse response = userService.getUserPublicProfile(userId);

        return ResponseEntity.ok(
                RsData.of("200", "공개 프로필 조회 성공", response)
        );
    }

}
