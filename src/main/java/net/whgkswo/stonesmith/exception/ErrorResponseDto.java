package net.whgkswo.stonesmith.exception;

import net.whgkswo.stonesmith.responses.dtos.Dto;

public record ErrorResponseDto(
        String message,
        int status
) implements Dto {
}
