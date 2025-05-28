package net.whgkswo.stonesmith.auth.dto;

import lombok.Getter;
import net.whgkswo.stonesmith.responses.dtos.Dto;

public record LoginDto(
        String email,
        String password
) implements Dto {

}
