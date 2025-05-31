package net.whgkswo.excuse_bundle.entities.members.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record EmailVerificationRequestDto(
        @NotBlank(message = "이메일은 공백이 아니어야 합니다.")
        @Email(message = "이메일을 올바른 형식으로 입력해야 합니다.")
        String email,
        @NotNull(message = "인증 코드를 요구하는 목적을 명시해야 합니다.")
        VerificationPurpose purpose,
        @NotBlank(message = "보안 검증에 실패했습니다. 잠시 후 다시 시도해주세요.")
        String recaptchaToken
) implements Dto {
}
