package com.jhssong.univcodeserver.application.admin;

import com.jhssong.univcodeserver.domain.apikey.entity.ApiKey;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminMemberRow> findAllMembers() {
        Map<Long, ApiKey> keysByMemberId = apiKeyRepository.findAllWithMember().stream()
                .collect(Collectors.toMap(k -> k.getMember().getId(), Function.identity()));
        return memberRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(m -> {
                    ApiKey key = keysByMemberId.get(m.getId());
                    return new AdminMemberRow(
                            m.getId(),
                            m.getEmail(),
                            m.getAffiliation(),
                            m.getCreatedAt(),
                            key != null ? key.getId() : null,
                            key != null ? key.getStatus().name() : null,
                            key != null ? key.getDailyCallCount() : 0,
                            key != null ? key.getIssuanceRequestedAt() : null);
                })
                .toList();
    }

    @Transactional
    public void createMember(String email, String affiliation, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        memberRepository.save(Member.builder()
                .email(email)
                .affiliation(affiliation)
                .password(passwordEncoder.encode(password))
                .build());
    }

    @Transactional
    public void deleteMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        apiKeyRepository.deleteAllByMemberId(memberId);
        memberRepository.deleteById(memberId);
    }

    @Transactional
    public void createApiKey(Long memberId, String purpose) {
        if (apiKeyRepository.countByMemberId(memberId) >= 1) {
            throw new CustomException(ErrorCode.API_KEY_LIMIT_EXCEEDED);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        String keyValue = generateKeyValue();
        apiKeyRepository.save(ApiKey.builder()
                .member(member)
                .keyValue(keyValue)
                .purpose(purpose)
                .status(ApiKeyStatus.ACTIVE)
                .build());
    }

    @Transactional
    public void approveApiKey(Long id) {
        apiKeyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.API_KEY_NOT_FOUND))
                .approve();
    }

    @Transactional
    public void revokeApiKey(Long id) {
        apiKeyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.API_KEY_NOT_FOUND))
                .revoke();
    }

    @Transactional
    public void deleteApiKey(Long id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new CustomException(ErrorCode.API_KEY_NOT_FOUND);
        }
        apiKeyRepository.deleteById(id);
    }

    private String generateKeyValue() {
        return "uc_" + UUID.randomUUID().toString().replace("-", "");
    }
}
