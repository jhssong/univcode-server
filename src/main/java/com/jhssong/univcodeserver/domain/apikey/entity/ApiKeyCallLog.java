package com.jhssong.univcodeserver.domain.apikey.entity;

import com.jhssong.univcodeserver.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "api_key_call_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiKeyCallLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApiKey apiKey;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 255)
    private String path;

    @Builder
    public ApiKeyCallLog(ApiKey apiKey, String method, String path) {
        this.apiKey = apiKey;
        this.method = method;
        this.path = path;
    }
}
