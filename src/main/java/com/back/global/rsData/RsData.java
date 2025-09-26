package com.back.global.rsData;

public record RsData<T>(
        String resultCode,
        String msg,
        T data
) {
    public RsData(String resultCode, String msg) { 
        this(resultCode, msg, null); 
    }

    public static <T> RsData<T> of(String resultCode, String msg, T data) {
        return new RsData<>(resultCode, msg, data);
    }

    public static <T> RsData<T> of(String resultCode, String msg) {
        return new RsData<>(resultCode, msg, null);
    }

    public static <T> RsData<T> ok(String msg, T data) {
        return new RsData<>("200", msg, data);
    }

    public static <T> RsData<T> ok(String msg) {
        return new RsData<>("200", msg, null);
    }
}
