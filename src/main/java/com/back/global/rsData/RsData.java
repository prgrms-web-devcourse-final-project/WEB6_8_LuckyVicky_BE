package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RsData<T>(
        String resultCode,
        @JsonIgnore
        int statusCode,
        String msg,
        T data
) {
    public RsData(String resultCode, String msg) { this(resultCode, msg, null); }

    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }

    public static <T> RsData<T> of(String resultCode, String msg, T data) {
        return new RsData<>(resultCode, msg, data);
    }

    public static <T> RsData<T> of(String resultCode, String msg) {
        return new RsData<>(resultCode, msg, null);
    }

    public static <T> RsData<T> ok(String msg, T data) {
        return new RsData<>("200-OK", msg, data);
    }
}