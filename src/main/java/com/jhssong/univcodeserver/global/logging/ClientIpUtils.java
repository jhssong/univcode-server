package com.jhssong.univcodeserver.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class ClientIpUtils {

    private ClientIpUtils() {}

    public static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int comma = xff.indexOf(',');
            String first = (comma > 0 ? xff.substring(0, comma) : xff).trim();
            if (!first.isEmpty()) return first;
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) return realIp;
        return request.getRemoteAddr();
    }
}
