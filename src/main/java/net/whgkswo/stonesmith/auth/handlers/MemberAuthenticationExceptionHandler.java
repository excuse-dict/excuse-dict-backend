package net.whgkswo.stonesmith.auth.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.whgkswo.stonesmith.exception.ErrorResponseDto;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SignatureException;

// 인증 과정에서 Authentication 조회에 실패 (AuthenticationException 발생) 시 케이스 처리
// 요청 필터 단계에서 처리하기 때문에 DispatcherServlet <-> Controller 사이에서 동작하는 GlobalExceptionHandler와는 별개의 영역
// EntryPoint 이름만 보면 인증 요청 진입점 같지만 그냥 에러 핸들러
@Component
public class MemberAuthenticationExceptionHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // JwtVerificationFilter에서 attribute에 넣어 둔 예외 추출
        Exception exception = (Exception) request.getAttribute("exception");

        ErrorResponseDto dto;
        if(exception instanceof ExpiredJwtException){
            dto = new ErrorResponseDto(401, "토큰이 만료되었습니다.");
        } else if (exception instanceof SignatureException) {
            dto = new ErrorResponseDto(401, "유효하지 않은 토큰입니다.");
        } else {
            dto = new ErrorResponseDto(401, "인증이 필요합니다.");
        }
        sendErrorResponse(response, dto);
    }

    // 에러 응답 전송
    private void sendErrorResponse(HttpServletResponse response, ErrorResponseDto dto) throws IOException{
        response.setStatus(dto.status());
        response.setContentType("application/json;charset=UTF-8");

        String json = objectMapper.writeValueAsString(dto);

        response.getWriter().write(json);
    }
}
