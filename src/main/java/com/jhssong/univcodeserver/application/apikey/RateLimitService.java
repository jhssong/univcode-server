package com.jhssong.univcodeserver.application.apikey;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyCallLog;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyCallLogRepository;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    public static final int DAILY_LIMIT = 100;

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyCallLogRepository apiKeyCallLogRepository;

    @Transactional
    public void checkAndIncrement(Long apiKeyId, String method, String path) {
        ApiKey key = apiKeyRepository.findByIdWithLock(apiKeyId)
                .orElseThrow(() -> new CustomException(ErrorCode.API_KEY_INVALID));
        LocalDate today = LocalDate.now();
        if (!today.equals(key.getCallCountDate())) {
            key.resetDailyCount(today);
        }
        if (key.getDailyCallCount() >= DAILY_LIMIT) {
            throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        key.incrementDailyCount();
        apiKeyCallLogRepository.save(ApiKeyCallLog.builder()
                .apiKey(key)
                .method(method)
                .path(path)
                .build());
    }
}
