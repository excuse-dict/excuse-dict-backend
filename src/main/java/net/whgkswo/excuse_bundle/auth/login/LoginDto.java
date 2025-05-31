package net.whgkswo.excuse_bundle.auth.login;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record LoginDto(
        String email,
        String password
) implements Dto {

}
