package net.whgkswo.excuse_bundle.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BadRequestException extends RuntimeException implements CustomException {
    private ExceptionType exceptionType;
}
