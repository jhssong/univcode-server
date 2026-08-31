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
        LocalDateTime createdAt,
        LocalDateTime issuanceRequestedAt
) {
    public static ApiKeyResponse from(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getPurpose(),
                apiKey.getKeyValue(),
                apiKey.getStatus().name(),
                apiKey.getDailyCallCount(),
                RateLimitService.DAILY_LIMIT,
                apiKey.getCreatedAt(),
                apiKey.getIssuanceRequestedAt()
        );
    }
}
