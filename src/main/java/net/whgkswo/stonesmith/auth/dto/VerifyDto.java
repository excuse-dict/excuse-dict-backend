package net.whgkswo.stonesmith.auth.dto;

import net.whgkswo.stonesmith.responses.dtos.Dto;

public record VerifyDto(
        String email,
        String verificationCode
) implements Dto {
}
