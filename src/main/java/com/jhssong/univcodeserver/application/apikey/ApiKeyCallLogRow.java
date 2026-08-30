package com.jhssong.univcodeserver.application.apikey;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyCallLog;
import java.time.LocalDateTime;

public record ApiKeyCallLogRow(LocalDateTime calledAt, String method, String path) {
    public static ApiKeyCallLogRow from(ApiKeyCallLog log) {
        return new ApiKeyCallLogRow(log.getCreatedAt(), log.getMethod(), log.getPath());
    }
}
