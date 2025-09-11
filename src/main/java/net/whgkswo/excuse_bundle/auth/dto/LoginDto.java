package net.whgkswo.excuse_bundle.auth.dto;

import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record LoginDto(
        String email,
        String password
) implements Dto {

}
