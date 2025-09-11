package net.whgkswo.excuse_bundle.exceptions;

import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record ErrorResponseDto(
        int status,
        String code,
        String message
        ) implements Dto {

        public static ErrorResponseDto of(ExceptionType type){
                return new ErrorResponseDto(type.status(), type.code(), type.message());
        }
}
