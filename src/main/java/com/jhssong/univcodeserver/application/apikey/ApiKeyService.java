package com.jhssong.univcodeserver.application.apikey;

import com.jhssong.errorping.ErrorpingService;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import com.jhssong.univcodeserver.presentation.apikey.dto.ApiKeyResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MemberRepository memberRepository;
    private final ErrorpingService errorpingService;

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> findAll(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return apiKeyRepository.findAllByMember(member).stream()
                .map(ApiKeyResponse::from)
                .toList();
    }

    @Transactional
    public ApiKeyResponse reissue(Long memberId) {
        ApiKey apiKey = apiKeyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.API_KEY_NOT_FOUND));
        apiKey.reissue(generateKeyValue());
        return ApiKeyResponse.from(apiKey);
    }

    @Transactional
    public void requestIssuance(Long memberId, HttpServletRequest request) {
        Optional<ApiKey> existing = apiKeyRepository.findByMemberId(memberId);
        ApiKey apiKey;
        if (existing.isPresent()) {
            apiKey = existing.get();
            if (apiKey.isActive() || apiKey.getIssuanceRequestedAt() != null) {
                return;
            }
        } else {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            apiKey = apiKeyRepository.save(ApiKey.builder()
                    .member(member)
                    .keyValue(generateKeyValue())
                    .purpose("회원 요청")
                    .status(ApiKeyStatus.INACTIVE)
                    .build());
        }
        apiKey.markIssuanceRequested();
        String message = "API 키 발급 요청 — " + apiKey.getMember().getEmail()
                + " (" + apiKey.getMember().getAffiliation() + ")";
        errorpingService.sendErrorToDiscord(new IllegalStateException(message), HttpStatus.OK, request, message);
    }

    private String generateKeyValue() {
        return "uc_" + UUID.randomUUID().toString().replace("-", "");
    }
}
