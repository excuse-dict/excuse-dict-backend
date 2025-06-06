package net.whgkswo.excuse_bundle.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 클라이언트 요청 / 비즈니스 로직 오류 예외 처리
    @ExceptionHandler({BusinessLogicException.class, BadRequestException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequestExceptionException(CustomException e){
        ErrorResponseDto response = ErrorResponseDto.of(e.getExceptionType());

        return ResponseEntity
                .status(e.getExceptionType().status())
                .body(response);
    }


    // DTO 파싱 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDto(HttpMessageNotReadableException e){
        // DTO 필드에 명시한 메시지는 사용 불가 (밑에 있음)
        // JSON 파싱 단계에서 오류가 나면 Validation 단계까지 도달 X
        // JSON 필드 타입 오류
        if(e.getCause() instanceof InvalidFormatException ie){
            return handleInvalidFormatException(ie);

        } else if (e.getCause() instanceof JsonMappingException jme) {
            return handleJsonMappingException(jme);
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

    public ResponseEntity<ErrorResponseDto> handleInvalidFormatException(InvalidFormatException ie){
        String fieldName = ie.getPath().get(0).getFieldName();
        Object rejectedValue = ie.getValue();

        ErrorResponseDto response = ErrorResponseDto.of(ExceptionType.jsonFieldInvalid(fieldName, rejectedValue.toString()));

        return ResponseEntity.badRequest().body(response);
    }

    public ResponseEntity<ErrorResponseDto> handleJsonMappingException(JsonMappingException jme){
        // jackson이 IllegalArgumentException을 JsonMappingException으로 래핑함
        if (jme.getCause() instanceof IllegalArgumentException iae) {
            ErrorResponseDto response = ErrorResponseDto.of(
                    ExceptionType.fromException(400, iae)
            );
            return ResponseEntity.badRequest().body(response);
        }

        // 일반 JsonMappingException 처리
        ErrorResponseDto response = ErrorResponseDto.of(
                ExceptionType.fromException(400, jme)
        );
        return ResponseEntity.badRequest().body(response);
    }
}
