package net.whgkswo.excuse_bundle.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.whgkswo.excuse_bundle.exceptions.ErrorResponseDto;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

// 로그인 시도 실패 시 호출 (아이디 / 비밀번호 틀림)
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        sendErrorResponse(response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException{
        ObjectMapper mapper = new ObjectMapper();

        // 응답 구성
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ExceptionType.AUTHENTICATION_FAILED);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);

        // 응답 전송
        response.getWriter().write(mapper.writeValueAsString(errorResponseDto));
    }
}
