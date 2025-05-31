package net.whgkswo.excuse_bundle.auth.recaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record RecaptchaResponseDto(
        boolean success,
        String challenge_ts,
        String hostname,
        @JsonProperty("error-codes") String[] errorCodes
) implements Dto {
}
