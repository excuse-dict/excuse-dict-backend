package net.whgkswo.excuse_dict.entities.excuses.dto;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;
import org.hibernate.validator.constraints.Length;

public record GenerateExcuseDto(
        @Length(min = 5, max = 100, message = "상황은 5~100자로 입력해주세요.")
        String situation,
        String recaptchaToken
) implements Dto {
}
