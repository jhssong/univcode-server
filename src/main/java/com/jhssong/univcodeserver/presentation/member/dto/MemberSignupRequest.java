package com.jhssong.univcodeserver.presentation.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
        @NotBlank @Email String email,
        @NotBlank String affiliation,
        @NotBlank @Size(min = 8) String password
) {}
