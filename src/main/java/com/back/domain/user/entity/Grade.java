package com.back.domain.user.entity;

import com.back.global.exception.ServiceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Grade {
    SPROUT("새싹"),
    GRASS("풀"),
    TREE("나무"),
    FOREST("숲"),
    GUARDIAN("나무지기");

    private final String koreanName;

    // 한글 이름으로 Grade 찾기
    @JsonCreator
    public static Grade fromKoreanName(String koreanName) {
        if (koreanName == null || koreanName.isBlank()) {
            throw new ServiceException("400", "등급 정보가 필요합니다.");
        }

        for (Grade grade : Grade.values()) {
            if (grade.getKoreanName().equals(koreanName)) {
                return grade;
            }
        }
        throw new ServiceException("400", "유효하지 않은 등급입니다: " + koreanName);
    }

    // 영문 이름으로 Grade 찾기
    public static Grade fromName(String name) {
        if (name == null || name.isBlank()) {
            throw new ServiceException("400", "등급 정보가 필요합니다.");
        }

        try {
            return Grade.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("400", "유효하지 않은 등급입니다: " + name);
        }
    }

    // JSON 직렬화 시 한글 이름 반환 - ex) API 응답에서 "SPROUT" 대신 "새싹"으로 표시
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
