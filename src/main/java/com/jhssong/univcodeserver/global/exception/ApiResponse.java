package com.jhssong.univcodeserver.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final ResultType result;
    private final T data;
    private final String title;
    private final String message;

    public static ApiResponse<?> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null, null);
    }

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null, null);
    }

    public static <S> ApiResponse<S> success(String message) {
        return new ApiResponse<>(ResultType.SUCCESS, null, null, message);
    }

    public static ApiResponse<?> error(ErrorCode error) {
        return new ApiResponse<>(ResultType.FAIL, null, error.name(), error.getMessage());
    }

    public static ApiResponse<?> error(ErrorCode error, String message) {
        return new ApiResponse<>(ResultType.FAIL, null, error.name(), message);
    }
}
