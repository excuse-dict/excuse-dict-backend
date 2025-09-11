package net.whgkswo.excuse_bundle.auth.dto;

import jakarta.validation.constraints.NotBlank;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record RefreshAccessTokenRequestDto(
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) implements Dto {
}
