package com.jhssong.univcodeserver.presentation.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank String code
) {}
