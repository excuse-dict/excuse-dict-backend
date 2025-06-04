package net.whgkswo.excuse_bundle.auth.jwt.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.whgkswo.excuse_bundle.auth.dto.LoginDto;
import net.whgkswo.excuse_bundle.auth.jwt.service.JwtTokenService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

// 클라이언트 로그인 요청을 수신하는 엔트리포인트
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    // 로그인 요청을 받으면 여기로 들어오고 authenticationManager에게 처리 위임
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response){
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.email(), loginDto.password()
        );

        return authenticationManager.authenticate(authenticationToken);
    }

    // authenticationManager가 처리를 마치고 여기로 돌려줌 -> 토큰 발행해서 응답 나감
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain,
                                            Authentication authResult
                                            ) throws ServletException, IOException {
        Member member = (Member) authResult.getPrincipal();

        String accessToken = jwtTokenService.generateAccessToken(member);
        String refreshToken = jwtTokenService.generateRefreshToken(member);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        // AuthenticationSuccessHandler 추가 호출
        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}
