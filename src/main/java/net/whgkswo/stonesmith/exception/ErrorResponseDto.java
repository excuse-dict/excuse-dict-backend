package net.whgkswo.stonesmith.exception;

import net.whgkswo.stonesmith.responses.dtos.Dto;

public record ErrorResponseDto(
        int status,
        String message
        ) implements Dto {
}
