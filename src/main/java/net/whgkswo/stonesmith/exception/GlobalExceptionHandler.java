package net.whgkswo.stonesmith.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessLoginException(BusinessLogicException e){
        ErrorResponseDto dto = new ErrorResponseDto(e.getStatus(), e.getMessage());

        return ResponseEntity
                .status(dto.status())
                .body(dto);
    }
}
