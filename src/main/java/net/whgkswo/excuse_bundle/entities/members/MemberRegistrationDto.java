package net.whgkswo.excuse_bundle.entities.members;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record MemberRegistrationDto(
        @Email(message = "이메일 형식이 바르지 않습니다.")
        @Size(max = 30, message = "이메일은 30자 이내여야 합니다.")
        String email,

        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
        String nickname,

        // TODO: 허용되지 않은 문자 거르기 -> 커스텀 Validator 고려
        @Size(min = 8, max = 128, message = "비밀번호는 8~128자 사이여야 합니다.")
        @Pattern(regexp = "^(?=.*[!@#$%&_\\-=;:,.<>]).*$", message = "비밀번호에 필수 특수문자가 누락되었습니다.")
        String rawPassword
) implements Dto {
}
