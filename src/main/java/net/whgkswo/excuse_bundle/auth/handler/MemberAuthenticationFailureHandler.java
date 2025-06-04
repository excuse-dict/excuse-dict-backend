package net.whgkswo.excuse_bundle.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.whgkswo.excuse_bundle.exceptions.ErrorResponseDto;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

// 로그인 인증 실패 시 추가 로직 실행 가능
public class MemberAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        sendErrorResponse(response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException{
        ObjectMapper mapper = new ObjectMapper();

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(401, "로그인에 실패하였습니다.");

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);

        response.getWriter().write(mapper.writeValueAsString(errorResponseDto));
    }
}
