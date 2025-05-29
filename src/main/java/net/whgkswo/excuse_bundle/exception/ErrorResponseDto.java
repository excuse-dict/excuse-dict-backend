package net.whgkswo.excuse_bundle.exception;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record ErrorResponseDto(
        int status,
        String message
        ) implements Dto {
}
