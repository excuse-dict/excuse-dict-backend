package net.whgkswo.stonesmith.entities.members;

import net.whgkswo.stonesmith.responses.dtos.Dto;

public record MemberDto(
        String username,
        String email,
        String rawPassword
) implements Dto {
}
