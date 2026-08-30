package com.jhssong.univcodeserver.application.admin;

import java.time.LocalDateTime;

public record AdminMemberRow(
        Long id,
        String email,
        String affiliation,
        LocalDateTime createdAt,
        Long apiKeyId,
        String apiKeyStatus,
        int dailyCallCount
) {
    public boolean hasApiKey() {
        return apiKeyId != null;
    }
}
