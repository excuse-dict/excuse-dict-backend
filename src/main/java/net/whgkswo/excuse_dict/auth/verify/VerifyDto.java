package net.whgkswo.excuse_dict.auth.verify;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record VerifyDto(
        @NotBlank(message = "이메일은 공백이 아니어야 합니다.")
        @Email(message = "이메일 형식을 올바르게 입력해야 합니다.")
        String email,
        @NotNull(message = "인증 코드를 정확히 입력해야 합니다.")
        String verificationCode
) implements Dto {
}
