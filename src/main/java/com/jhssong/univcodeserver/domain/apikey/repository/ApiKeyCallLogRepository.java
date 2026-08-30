package com.jhssong.univcodeserver.domain.apikey.repository;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyCallLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyCallLogRepository extends JpaRepository<ApiKeyCallLog, Long> {

    List<ApiKeyCallLog> findByApiKeyIdOrderByCreatedAtDesc(Long apiKeyId, Pageable pageable);

    @Query("SELECT FUNCTION('DATE', l.createdAt), COUNT(l) FROM ApiKeyCallLog l "
            + "WHERE l.apiKey.id = :apiKeyId AND l.createdAt >= :since "
            + "GROUP BY FUNCTION('DATE', l.createdAt)")
    List<Object[]> countDailySince(@Param("apiKeyId") Long apiKeyId, @Param("since") LocalDateTime since);

    @Query("SELECT FUNCTION('DATE_FORMAT', l.createdAt, '%Y-%m'), COUNT(l) FROM ApiKeyCallLog l "
            + "WHERE l.apiKey.id = :apiKeyId AND l.createdAt >= :since "
            + "GROUP BY FUNCTION('DATE_FORMAT', l.createdAt, '%Y-%m')")
    List<Object[]> countMonthlySince(@Param("apiKeyId") Long apiKeyId, @Param("since") LocalDateTime since);
}
