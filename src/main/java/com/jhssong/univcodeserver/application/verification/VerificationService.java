package com.jhssong.univcodeserver.application.verification;

import com.jhssong.univcode.UnivCodeService;
import com.jhssong.univcode.exception.ResendCooldownException;
import com.jhssong.univcode.exception.SendFailureException;
import com.jhssong.univcode.store.VerificationStore.VerifyResult;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationSendRequest;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationVerifyRequest;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UnivCodeService univCodeService;

    public void send(VerificationSendRequest request) {
        try {
            univCodeService.sendCode(request.email());
        } catch (ResendCooldownException e) {
            throw new CustomException(ErrorCode.RESEND_COOLDOWN);
        } catch (SendFailureException e) {
            throw new CustomException(ErrorCode.SEND_FAILED);
        }
    }

    public VerificationVerifyResponse verify(VerificationVerifyRequest request) {
        VerifyResult result = univCodeService.verifyCode(request.email(), request.code());
        return new VerificationVerifyResponse(result == VerifyResult.MATCHED);
    }
}
