package com.back.domain.user.service;

import com.back.domain.user.entity.Grade;
import com.back.domain.user.entity.Status;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));

    }

    /**
     * 이메일로 사용자 조회
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException("404", "사용자를 찾을 수 없습니다."));
    }

    // ===== 작가 관련 ===== //

    /**
     * 작가 자격 획득
     */
    @Transactional
    public void becomeToArtist(Long userId) {
        User user = getUserById(userId);

        if (user.isArtist()) {
            throw new ServiceException("400", "이미 작가 권한을 가진 사용자입니다.");
        }

        user.becomeArtist();
        log.info("사용자 작가로 변경: userId={}", userId);
    }

    /**
     * 작가 자격 해제
     */
    @Transactional
    public void revokeArtistRole(Long userId, Long adminId) {
        User user = getUserById(userId);

        if (!user.isArtist()) {
            throw new ServiceException("400", "작가 권한이 없는 사용자입니다.");
        }

        user.revokeArtistRole();
        log.info("작가 자격 해제: userId={}, adminId={}", userId, adminId);
    }

    // ==== 머니/포인트 관리 ==== //

    /**
     * 머니 증가
     */
    @Transactional
    public void addMoney(Long userId, int amount, String reason) {
        User user = getUserById(userId);
        user.addMoney(amount);
        log.info("머니 증가: userId={}, amount={}, reason={}, newBalance={}",
                userId, amount, reason, user.getMoney());
    }

    /**
     * 머니 차감
     */
    @Transactional
    public void deductMoney(Long userId, int amount, String reason) {
        User user = getUserById(userId);
        user.deductMoney(amount);
        log.info("머니 차감: userId={}, amount={}, reason={}, newBalance={}",
                userId, amount, reason, user.getMoney());
    }

    /**
     * 포인트 증가
     */
    @Transactional
    public void addPoint(Long userId, int amount, String reason) {
        User user = getUserById(userId);
        user.addPoint(amount);
        log.info("포인트 증가: userId={}, amount={}, reason={}, newBalance={}",
                userId, amount, reason, user.getPoint());
    }

    /**
     * 포인트 차감
     */
    @Transactional
    public void deductPoint(Long userId, int amount, String reason) {
        User user = getUserById(userId);
        user.deductPoint(amount);
        log.info("포인트 차감: userId={}, amount={}, reason={}, newBalance={}",
                userId, amount, reason, user.getPoint());
    }

    // ==== 등급 관리 ==== //

    /**
     * 등급 업그레이드
     */
    @Transactional
    public void upgradeGrade(Long userId, Grade newGrade) {
        User user = getUserById(userId);
        Grade oldGrade = user.getGrade();

        user.upgradeGrade(newGrade);
        log.info("등급 변경: userId={}, {} → {}", userId, oldGrade, newGrade);
    }

    // ==== 계정 상태 관리 ==== //

    /**
     * 계정 상태 변경
     */
    @Transactional
    public void changeStatus(Long userId, Status newStatus, Long adminId) {
        User user = getUserById(userId);
        Status oldStatus = user.getStatus();

        user.changeStatus(newStatus);
        log.info("계정 상태 변경: userId={}, {} → {}, adminId={}",
                userId, oldStatus, newStatus, adminId);
    }

    /**
     * 계정 차단
     */
    @Transactional
    public void blockUser(Long userId, Long adminId, String reason) {
        User user = getUserById(userId);

        if (Status.BLOCKED.equals(user.getStatus())) {
            throw new ServiceException("400", "이미 차단된 사용자입니다.");
        }

        user.changeStatus(Status.BLOCKED);
        log.info("계정 차단: userId={}, adminId={}, reason={}", userId, adminId, reason);
    }

    /**
     * 계정 차단 해제
     */
    @Transactional
    public void unblockUser(Long userId, Long adminId) {
        User user = getUserById(userId);

        if (!Status.BLOCKED.equals(user.getStatus())) {
            throw new ServiceException("400", "차단되지 않은 사용자입니다.");
        }

        user.changeStatus(Status.ACTIVE);
        log.info("계정 차단 해제: userId={}, adminId={}", userId, adminId);
    }

    /**
     * 계정 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        if (Status.DELETED.equals(user.getStatus())) {
            throw new ServiceException("400", "이미 삭제된 계정입니다.");
        }

        user.changeStatus(Status.DELETED);
        log.info("계정 삭제: userId={}", userId);
    }

}
