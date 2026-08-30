package com.jhssong.univcodeserver.application.apikey;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import com.jhssong.univcodeserver.presentation.apikey.dto.ApiKeyResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String issueDefault(Member member) {
        return issue(member, "회원가입 시 자동 발급").getKeyValue();
    }

    private ApiKey issue(Member member, String purpose) {
        return apiKeyRepository.save(ApiKey.builder()
                .member(member)
                .keyValue(generateKeyValue())
                .purpose(purpose)
                .status(ApiKeyStatus.ACTIVE)
                .build());
    }

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
        return ApiKeyResponse.issued(apiKey);
    }

    private String generateKeyValue() {
        return "uc_" + UUID.randomUUID().toString().replace("-", "");
    }
}
