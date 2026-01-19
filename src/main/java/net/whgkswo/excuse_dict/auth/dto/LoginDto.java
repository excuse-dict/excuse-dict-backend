package net.whgkswo.excuse_dict.auth.dto;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record LoginDto(
        String email,
        String password
) implements Dto {

}
