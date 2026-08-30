package com.jhssong.univcodeserver.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final long SLOW_THRESHOLD_MS = 500L;
    private static final int MAX_URI_LEN = 300;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return "/actuator/health".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            logRequest(request, response, duration);
        }
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = buildUri(request);
        int status = response.getStatus();
        String ip = ClientIpUtils.clientIp(request);
        boolean slow = duration > SLOW_THRESHOLD_MS;
        String tag = slow ? "[SLOW]" : "[REQ]";

        if (status >= 500) {
            log.error("{} {} {} {} {}ms ip={}", tag, method, uri, status, duration, ip);
        } else if (status >= 400 || slow) {
            log.warn("{} {} {} {} {}ms ip={}", tag, method, uri, status, duration, ip);
        } else {
            log.info("{} {} {} {} {}ms ip={}", tag, method, uri, status, duration, ip);
        }
    }

    private String buildUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String full = StringUtils.hasText(query) ? uri + "?" + query : uri;
        return full.length() > MAX_URI_LEN ? full.substring(0, MAX_URI_LEN) + "…" : full;
    }
}
