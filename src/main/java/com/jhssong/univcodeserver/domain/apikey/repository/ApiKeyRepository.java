package com.jhssong.univcodeserver.domain.apikey.repository;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findAllByMember(Member member);
    Optional<ApiKey> findByMemberId(Long memberId);
    Optional<ApiKey> findByKeyValueAndStatus(String keyValue, ApiKeyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM ApiKey k WHERE k.id = :id")
    Optional<ApiKey> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT k FROM ApiKey k JOIN FETCH k.member ORDER BY k.createdAt DESC")
    List<ApiKey> findAllWithMember();

    long countByMemberId(Long memberId);
    long countByStatus(ApiKeyStatus status);

    void deleteAllByMemberId(Long memberId);

    @Query("SELECT COALESCE(SUM(k.dailyCallCount), 0) FROM ApiKey k WHERE k.callCountDate = :date")
    long sumDailyCallCountByDate(@Param("date") LocalDate date);
}
