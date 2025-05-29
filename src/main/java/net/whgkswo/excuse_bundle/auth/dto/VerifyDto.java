package net.whgkswo.excuse_bundle.auth.dto;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record VerifyDto(
        String email,
        String verificationCode
) implements Dto {
}
