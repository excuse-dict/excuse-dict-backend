package net.whgkswo.excuse_bundle.entities.members.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import net.whgkswo.excuse_bundle.entities.members.passwords.ValidPassword;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record MemberRegistrationDto(
        @Email(message = "이메일 형식이 바르지 않습니다.")
        @Size(max = 30, message = "이메일은 30자 이내여야 합니다.")
        String email,

        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
        String nickname,

        @ValidPassword
        String rawPassword
) implements Dto {
}
