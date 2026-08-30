package com.jhssong.univcodeserver.application.resend;

import com.jhssong.univcodeserver.global.config.ResendProperties;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResendMetricsService {

    private static final String METRICS = "sent,delivered,bounced,opened,complained";

    private final ResendProperties resendProperties;
    private final RestClient restClient = RestClient.create("https://api.resend.com");

    public ResendUsage getUsage(int days) {
        if (!StringUtils.hasText(resendProperties.getApiKey())) {
            return ResendUsage.unavailable();
        }
        try {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(days - 1L);
            ResendMetricsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/emails/metrics")
                            .queryParam("start_date", start)
                            .queryParam("end_date", end)
                            .queryParam("metrics", METRICS)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendProperties.getApiKey())
                    .retrieve()
                    .body(ResendMetricsResponse.class);
            Map<String, Object> totals = response != null && response.totals() != null
                    ? response.totals()
                    : Map.of();
            return new ResendUsage(
                    true,
                    asLong(totals.get("sent")),
                    asLong(totals.get("delivered")),
                    asLong(totals.get("bounced")),
                    asLong(totals.get("opened")),
                    asLong(totals.get("complained")));
        } catch (RestClientException e) {
            log.warn("Resend 지표 조회 실패: {}", e.getMessage());
            return ResendUsage.unavailable();
        }
    }

    private static long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
