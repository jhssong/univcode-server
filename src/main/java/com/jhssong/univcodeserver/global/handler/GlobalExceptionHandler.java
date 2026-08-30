package com.jhssong.univcodeserver.global.handler;

import com.jhssong.errorping.ErrorpingService;
import com.jhssong.univcodeserver.global.exception.ApiResponse;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorpingService errorpingService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.info("[400] Validation Failed method={} uri={} message={}", method(request), uri(request), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requiredType = (e.getRequiredType() != null) ? e.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("field '%s' expected type '%s'", e.getName(), requiredType);
        log.info("[400] Type Mismatch method={} uri={} field={} expectedType={}",
                method(request), uri(request), e.getName(), requiredType);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[400] Illegal Argument method={} uri={} message={}", method(request), uri(request), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    protected ResponseEntity<ApiResponse<?>> handleServletRequestBindingException(
            ServletRequestBindingException e, HttpServletRequest request) {
        log.debug("[400] Invalid Access method={} uri={} message={}", method(request), uri(request), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.debug("[405] Method Not Allowed method={} uri={}", method(request), uri(request));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(
            NoResourceFoundException e, HttpServletRequest request) {
        log.debug("[404] No Resource Found method={} uri={}", method(request), uri(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus();
        if (status.is4xxClientError()) {
            log.warn("[{}] CustomException({}) method={} uri={} message={}",
                    status.value(), errorCode.name(), method(request), uri(request), errorCode.getMessage());
        } else if (status.is5xxServerError()) {
            errorpingService.sendErrorToDiscord(e, status, request, errorCode.getMessage());
            log.error("[{}] CustomException({}) method={} uri={} message={}",
                    status.value(), errorCode.name(), method(request), uri(request), errorCode.getMessage(), e);
        }
        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception e, HttpServletRequest request) {
        log.error("[500] Internal Server Error method={} uri={}", method(request), uri(request), e);
        errorpingService.sendErrorToDiscord(e, HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private static String method(HttpServletRequest request) {
        return request != null ? request.getMethod() : "?";
    }

    private static String uri(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "?";
    }
}
