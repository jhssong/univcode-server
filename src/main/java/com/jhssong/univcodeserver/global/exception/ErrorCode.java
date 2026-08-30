package com.jhssong.univcodeserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Member
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // ApiKey
    API_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "API 키를 찾을 수 없습니다."),
    API_KEY_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 API 키입니다."),
    API_KEY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "API 키는 1개만 발급받을 수 있습니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "일일 API 호출 한도(100회)를 초과했습니다."),

    // Verification
    RESEND_COOLDOWN(HttpStatus.TOO_MANY_REQUESTS, "재발송 대기 시간이 지나지 않았습니다."),
    SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인증 코드 발송에 실패했습니다."),

    // Mail
    MANUAL_MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 발송에 실패했습니다."),

    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다.");

    private final HttpStatus status;
    private final String message;
}
