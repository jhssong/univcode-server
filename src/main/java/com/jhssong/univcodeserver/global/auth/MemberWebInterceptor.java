package com.jhssong.univcodeserver.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MemberWebInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "member_web_id";
    public static final String MEMBER_ID_ATTR = "memberWebId";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object memberId = session != null ? session.getAttribute(SESSION_KEY) : null;
        if (memberId != null) {
            request.setAttribute(MEMBER_ID_ATTR, (Long) memberId);
            return true;
        }
        response.sendRedirect("/login");
        return false;
    }
}
