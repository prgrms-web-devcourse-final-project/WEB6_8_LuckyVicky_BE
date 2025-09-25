package com.back.domain.user.user;

import com.back.domain.user.entity.Grade;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발용 임시 클래스 (추후 삭제 예정)
 * TODO: 작가 신청/승인 API 완성 후 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "test"}) // 개발/테스트 환경에서만 사용
public class DevUserService {

    private final UserRepository userRepository;
    private final Rq rq;

    /**
     * 현재 로그인된 사용자의 역할을 변경합니다.
     * 사용법: devUserService.changeMyRole(Role.ARTIST);
     */
    @Transactional
    public void changeMyRole(Role newRole) {
        Long userId = rq.getUserId();
        if (userId == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changeRole(newRole);
        log.info("사용자 {}의 역할이 {}로 변경되었습니다.", user.getName(), newRole);
    }

    /**
     * 현재 로그인된 사용자의 등급을 변경합니다.
     * 사용법: devUserService.changeMyGrade(Grade.GOLD);
     */
    @Transactional
    public void changeMyGrade(Grade newGrade) {
        Long userId = rq.getUserId();
        if (userId == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changeGrade(newGrade);
        log.info("사용자 {}의 등급이 {}로 변경되었습니다.", user.getName(), newGrade);
    }

    /**
     * 현재 로그인된 사용자의 돈을 설정합니다.
     * 사용법: devUserService.setMyMoney(100000);
     */
    @Transactional
    public void setMyMoney(int money) {
        Long userId = rq.getUserId();
        if (userId == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setMoney(money);
        log.info("사용자 {}의 보유 금액이 {}원으로 설정되었습니다.", user.getName(), money);
    }

    /**
     * 특정 사용자의 역할을 변경합니다. (관리자용)
     * 사용법: devUserService.changeUserRole(userId, Role.ARTIST);
     */
    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changeRole(newRole);
        log.info("사용자 {}의 역할이 {}로 변경되었습니다.", user.getName(), newRole);
    }

    /**
     * 이메일로 사용자 찾아서 역할 변경
     * 사용법: devUserService.changeUserRoleByEmail("user1@dev.com", Role.ARTIST);
     */
    @Transactional
    public void changeUserRoleByEmail(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        user.changeRole(newRole);
        log.info("사용자 {}({})의 역할이 {}로 변경되었습니다.", user.getName(), email, newRole);
    }
}