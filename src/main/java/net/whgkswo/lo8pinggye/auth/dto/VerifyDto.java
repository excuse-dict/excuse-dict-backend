package net.whgkswo.lo8pinggye.auth.dto;

import net.whgkswo.lo8pinggye.responses.dtos.Dto;

public record VerifyDto(
        String email,
        String verificationCode
) implements Dto {
}
