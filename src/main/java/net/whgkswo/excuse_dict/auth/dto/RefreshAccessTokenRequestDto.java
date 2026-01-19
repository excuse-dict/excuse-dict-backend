package net.whgkswo.excuse_dict.auth.dto;

import jakarta.validation.constraints.NotBlank;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record RefreshAccessTokenRequestDto(
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) implements Dto {
}
