package com.jhssong.univcodeserver.presentation.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationSendRequest(@NotBlank @Email String email) {}
