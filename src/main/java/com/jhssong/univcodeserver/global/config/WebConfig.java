package com.jhssong.univcodeserver.global.config;

import com.jhssong.univcodeserver.global.auth.AdminAuthInterceptor;
import com.jhssong.univcodeserver.global.auth.ApiKeyInterceptor;
import com.jhssong.univcodeserver.global.auth.MemberWebInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    private final ApiKeyInterceptor apiKeyInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final MemberWebInterceptor memberWebInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API 키 인증 — 외부 클라이언트의 인증 코드 발송/검증 요청
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/v1/verification/**");

        // 세션 인증 — 서비스 관리자 웹 페이지
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        // 세션 인증 — 멤버 웹 페이지
        registry.addInterceptor(memberWebInterceptor)
                .addPathPatterns("/my/**");
    }
}
