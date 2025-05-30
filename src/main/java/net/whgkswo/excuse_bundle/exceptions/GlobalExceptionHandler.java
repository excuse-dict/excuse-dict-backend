package net.whgkswo.excuse_bundle.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import net.whgkswo.excuse_bundle.responses.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLoginException(BusinessLogicException e){
        ErrorResponse response = ErrorResponse.create(e, HttpStatus.valueOf(e.getStatus()), e.getMessage());

        return ResponseEntity
                .status(e.getStatus())
                .body(response);
    }

    // DTO 파싱 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDto(HttpMessageNotReadableException e){
        if(e.getCause() instanceof InvalidFormatException ie){
            String fieldName = ie.getPath().get(0).getFieldName();
            Object rejectedValue = ie.getValue();

            // DTO 필드에 명시한 메시지는 사용 불가 (밑에 있음)
            // JSON 파싱 단계에서 오류가 나면 Validation 단계까지 도달 X
            String message = String.format("%s 필드의 값이 유효하지 않습니다: %s", fieldName, rejectedValue);

            ErrorResponse response = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, message);

            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.badRequest().body(
                ErrorResponse.create(e, HttpStatus.BAD_REQUEST, "잘못된 요청 형식입니다.")
        );
    }

    // DTO 필드 Validation 에러 처리는 여기서
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                ErrorResponse.create(e, HttpStatus.BAD_REQUEST, message)
        );
    }
}
