package net.whgkswo.lo8pinggye.exception;

import net.whgkswo.lo8pinggye.responses.dtos.Dto;

public record ErrorResponseDto(
        int status,
        String message
        ) implements Dto {
}
