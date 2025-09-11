package net.whgkswo.excuse_bundle.entities.members.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record EmailDto(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank
        String email
) implements Dto {

}
