package com.jhssong.univcodeserver.application.member;

import com.jhssong.univcodeserver.application.apikey.ApiKeyService;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import com.jhssong.univcodeserver.presentation.member.dto.MemberSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyService apiKeyService;

    @Transactional
    public SignupResult signup(MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Member member = memberRepository.save(Member.builder()
                .email(request.email())
                .affiliation(request.affiliation())
                .password(passwordEncoder.encode(request.password()))
                .build());
        String apiKey = apiKeyService.issueDefault(member);
        return new SignupResult(member.getId(), apiKey);
    }
}
