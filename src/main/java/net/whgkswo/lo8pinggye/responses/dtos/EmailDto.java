package net.whgkswo.lo8pinggye.responses.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDto(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank
        String email
) implements Dto{

}
