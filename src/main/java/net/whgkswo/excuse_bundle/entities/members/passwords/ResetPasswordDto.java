package net.whgkswo.excuse_bundle.entities.members.passwords;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordDto(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @ValidPassword
        String newPassword,

        @NotBlank(message = "보안 인증에 실패하였습니다. 페이지를 새로고침하거나 잠시 후에 다시 시도해주세요.")
        String recaptchaToken
) {
}
