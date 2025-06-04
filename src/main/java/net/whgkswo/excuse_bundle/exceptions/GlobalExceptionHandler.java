package net.whgkswo.excuse_bundle.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessLoginException(BusinessLogicException e){
        ErrorResponseDto response = ErrorResponseDto.of(e.getExceptionType());

        return ResponseEntity
                .status(e.getExceptionType().status())
                .body(response);
    }

    // DTO 파싱 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDto(HttpMessageNotReadableException e){
        if(e.getCause() instanceof InvalidFormatException ie){
            String fieldName = ie.getPath().get(0).getFieldName();
            Object rejectedValue = ie.getValue();

            // DTO 필드에 명시한 메시지는 사용 불가 (밑에 있음)
            // JSON 파싱 단계에서 오류가 나면 Validation 단계까지 도달 X
            // JSON 필드 타입 오류
            ErrorResponseDto response = ErrorResponseDto.of(ExceptionType.jsonFieldInvalid(fieldName, rejectedValue.toString()));

            return ResponseEntity.badRequest().body(response);
        }
        // JSON 포맷 오류
        return ResponseEntity.badRequest().body(
                ErrorResponseDto.of(ExceptionType.JSON_FORMAT_INVALID)
        );
    }

    // DTO 필드 Validation 에러 처리는 여기서
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                ErrorResponseDto.of(ExceptionType.dtoValidationFailed(message))
        );
    }
}
