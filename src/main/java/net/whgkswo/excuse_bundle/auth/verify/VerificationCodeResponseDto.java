package net.whgkswo.excuse_bundle.auth.verify;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.time.LocalDateTime;

public record VerificationCodeResponseDto(
        String email,
        LocalDateTime expiryTime
) implements Dto {
}
