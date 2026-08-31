package com.jhssong.univcodeserver.application.admin;

import com.jhssong.errorping.ErrorpingService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private final ErrorpingService errorpingService;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Attempt attempt = attempts.get(ip);
        return attempt != null && attempt.blockedUntil != null && Instant.now().isBefore(attempt.blockedUntil);
    }

    public void recordFailure(String ip, HttpServletRequest request) {
        Attempt attempt = attempts.computeIfAbsent(ip, k -> new Attempt());
        boolean justBlocked = false;
        synchronized (attempt) {
            Instant now = Instant.now();
            if (attempt.windowStart == null || now.isAfter(attempt.windowStart.plus(WINDOW))) {
                attempt.windowStart = now;
                attempt.count = 0;
            }
            attempt.count++;
            if (attempt.count >= MAX_ATTEMPTS && attempt.blockedUntil == null) {
                attempt.blockedUntil = now.plus(LOCKOUT);
                justBlocked = true;
            }
        }
        if (justBlocked) {
            errorpingService.sendErrorToDiscord(
                    new IllegalStateException("관리자 로그인 " + MAX_ATTEMPTS + "회 연속 실패"),
                    HttpStatus.TOO_MANY_REQUESTS, request, "관리자 로그인 브루트포스 의심 ip=" + ip);
        }
    }

    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    private static class Attempt {
        Instant windowStart;
        int count;
        Instant blockedUntil;
    }
}
