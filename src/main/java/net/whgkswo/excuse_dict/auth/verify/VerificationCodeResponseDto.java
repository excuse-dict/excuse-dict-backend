package net.whgkswo.excuse_dict.auth.verify;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

import java.time.LocalDateTime;

public record VerificationCodeResponseDto(
        String email,
        LocalDateTime expiryTime
) implements Dto {
}
