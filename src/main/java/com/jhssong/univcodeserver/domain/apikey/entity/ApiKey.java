package com.jhssong.univcodeserver.domain.apikey.entity;

import com.jhssong.univcodeserver.domain.common.BaseTimeEntity;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "api_key")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(unique = true)
    private String keyValue;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiKeyStatus status;

    @Column(nullable = false)
    private int dailyCallCount = 0;

    @Column
    private LocalDate callCountDate;

    @Builder
    public ApiKey(Member member, String keyValue, String purpose, ApiKeyStatus status) {
        this.member = member;
        this.keyValue = keyValue;
        this.purpose = purpose;
        this.status = status != null ? status : ApiKeyStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == ApiKeyStatus.ACTIVE;
    }

    public void activate() {
        this.status = ApiKeyStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ApiKeyStatus.INACTIVE;
    }

    public void reissue(String keyValue) {
        this.keyValue = keyValue;
        this.status = ApiKeyStatus.ACTIVE;
        this.dailyCallCount = 0;
        this.callCountDate = null;
    }

    public void resetDailyCount(LocalDate date) {
        this.dailyCallCount = 0;
        this.callCountDate = date;
    }

    public void incrementDailyCount() {
        this.dailyCallCount++;
    }
}
