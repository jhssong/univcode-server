package com.jhssong.univcodeserver.application.apikey;

import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyCallLogRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiKeyUsageService {

    private static final int RECENT_LOG_LIMIT = 20;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MM/dd");

    private final ApiKeyCallLogRepository apiKeyCallLogRepository;

    @Transactional(readOnly = true)
    public List<UsagePoint> dailyUsage(Long apiKeyId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : apiKeyCallLogRepository.countDailySince(apiKeyId, start.atStartOfDay())) {
            counts.put(toLocalDate(row[0]), (Long) row[1]);
        }
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<UsagePoint> points = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(today); day = day.plusDays(1)) {
            long count = counts.getOrDefault(day, 0L);
            points.add(new UsagePoint(day.format(DAY_LABEL), count, heightPercent(count, max)));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<UsagePoint> monthlyUsage(Long apiKeyId, int months) {
        YearMonth current = YearMonth.now();
        YearMonth start = current.minusMonths(months - 1L);
        Map<YearMonth, Long> counts = new HashMap<>();
        for (Object[] row : apiKeyCallLogRepository.countMonthlySince(apiKeyId, start.atDay(1).atStartOfDay())) {
            counts.put(YearMonth.parse((String) row[0]), (Long) row[1]);
        }
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<UsagePoint> points = new ArrayList<>();
        for (YearMonth month = start; !month.isAfter(current); month = month.plusMonths(1)) {
            long count = counts.getOrDefault(month, 0L);
            points.add(new UsagePoint(month.toString(), count, heightPercent(count, max)));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<ApiKeyCallLogRow> recentLogs(Long apiKeyId) {
        return apiKeyCallLogRepository
                .findByApiKeyIdOrderByCreatedAtDesc(apiKeyId, PageRequest.of(0, RECENT_LOG_LIMIT))
                .stream()
                .map(ApiKeyCallLogRow::from)
                .toList();
    }

    private static int heightPercent(long count, long max) {
        if (max == 0 || count == 0) {
            return 0;
        }
        return Math.max((int) Math.round(count * 100.0 / max), 4);
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        return LocalDate.parse(value.toString());
    }
}
