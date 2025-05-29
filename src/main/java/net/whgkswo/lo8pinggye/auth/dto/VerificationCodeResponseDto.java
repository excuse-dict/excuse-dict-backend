package net.whgkswo.lo8pinggye.auth.dto;

import net.whgkswo.lo8pinggye.responses.dtos.Dto;

import java.time.LocalDateTime;

public record VerificationCodeResponseDto(
        String email,
        LocalDateTime expiryTime
) implements Dto {
}
