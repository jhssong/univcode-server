package com.jhssong.univcodeserver.presentation.verification.controller;

import com.jhssong.univcodeserver.application.verification.VerificationService;
import com.jhssong.univcodeserver.global.exception.ApiResponse;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationSendRequest;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationVerifyRequest;
import com.jhssong.univcodeserver.presentation.verification.dto.VerificationVerifyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> send(@Valid @RequestBody VerificationSendRequest request) {
        verificationService.send(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerificationVerifyResponse>> verify(
            @Valid @RequestBody VerificationVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(verificationService.verify(request)));
    }
}
