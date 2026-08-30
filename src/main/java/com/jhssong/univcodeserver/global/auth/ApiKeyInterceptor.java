package com.jhssong.univcodeserver.global.auth;

import com.jhssong.univcodeserver.application.apikey.RateLimitService;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Api-Key";

    private final ApiKeyRepository apiKeyRepository;
    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String key = request.getHeader(HEADER);
        if (!StringUtils.hasText(key)) {
            throw new CustomException(ErrorCode.API_KEY_INVALID);
        }
        ApiKey apiKey = apiKeyRepository.findByKeyValueAndStatus(key, ApiKeyStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.API_KEY_INVALID));
        rateLimitService.checkAndIncrement(apiKey.getId(), request.getMethod(), request.getRequestURI());
        request.setAttribute("apiKeyId", apiKey.getId());
        return true;
    }
}
