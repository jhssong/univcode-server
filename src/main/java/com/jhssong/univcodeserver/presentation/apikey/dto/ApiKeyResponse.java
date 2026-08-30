package com.jhssong.univcodeserver.presentation.apikey.dto;

import com.jhssong.univcodeserver.application.apikey.RateLimitService;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import java.time.LocalDateTime;

public record ApiKeyResponse(
        Long id,
        String purpose,
        String keyValue,
        String status,
        int dailyCallCount,
        int dailyLimit,
        LocalDateTime createdAt
) {
    /** 발급/재발급 직후 원문 키를 단 한 번 노출할 때 사용 */
    public static ApiKeyResponse issued(ApiKey apiKey) {
        return of(apiKey, apiKey.getKeyValue());
    }

    /** 그 외 조회에서는 키 원문을 노출하지 않는다 */
    public static ApiKeyResponse from(ApiKey apiKey) {
        return of(apiKey, null);
    }

    private static ApiKeyResponse of(ApiKey apiKey, String keyValue) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getPurpose(),
                keyValue,
                apiKey.getStatus().name(),
                apiKey.getDailyCallCount(),
                RateLimitService.DAILY_LIMIT,
                apiKey.getCreatedAt()
        );
    }
}
