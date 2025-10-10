package com.back.domain.funding.entity;

public enum FundingStatus {
    PENDING, // 심사 중
    APPROVED, // 승인됨(펀딩 오픈 대기 중)
    REJECTED, // 심사 거절됨
    OPEN, // 펀딩 오픈, 참여 가능
    CLOSED, // 펀딩 종료(성공/실패 확정 전)
    SUCCESS, // 목표 달성 성공
    FAILED, // 목표 미달성 실패
    CANCELED // 취소됨
}
