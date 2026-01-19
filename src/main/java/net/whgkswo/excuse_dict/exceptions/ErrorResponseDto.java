package net.whgkswo.excuse_dict.exceptions;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record ErrorResponseDto(
        int status,
        String code,
        String message
        ) implements Dto {

        public static ErrorResponseDto of(ExceptionType type){
                return new ErrorResponseDto(type.status(), type.code(), type.message());
        }
}
