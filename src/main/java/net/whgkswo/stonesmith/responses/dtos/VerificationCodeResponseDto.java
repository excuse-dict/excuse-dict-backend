package net.whgkswo.stonesmith.responses.dtos;

import java.time.LocalDateTime;

public record VerificationCodeResponseDto(
        String email,
        LocalDateTime expiryTime
) implements Dto{
}
