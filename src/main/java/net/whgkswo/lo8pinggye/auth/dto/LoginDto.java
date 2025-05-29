package net.whgkswo.lo8pinggye.auth.dto;

import net.whgkswo.lo8pinggye.responses.dtos.Dto;

public record LoginDto(
        String email,
        String password
) implements Dto {

}
