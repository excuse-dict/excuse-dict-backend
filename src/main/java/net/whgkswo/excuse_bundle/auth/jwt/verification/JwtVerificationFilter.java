package net.whgkswo.excuse_bundle.auth.jwt.verification;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.CustomAuthorityUtils;
import net.whgkswo.excuse_bundle.auth.jwt.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_bundle.entities.members.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 토큰 유효성 검증 필터
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        }catch (Exception e){
            // 예외 발생 시 SecurifyContext에 Authentication이 저장되지 않음 -> AuthenticationException 발생
            // AuthenticationEntryPoint가 AuthenticationException를 캐치해서 처리하는데
            // 여기서 attribute를 설정해 둔 정보를 뽑아서 사용함
            request.setAttribute("exception", e);
        }

        // 토큰 검증 및 claim 추출
        Map<String, Object> claims = verifyJws(request);
        // 인증정보 구성
        setAuthenticationToContext(claims);
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // true면 필터 예외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
        String authorization = request.getHeader("Authorization");
        // Authorization 헤더가 없거나 토큰이 Bearer로 시작하지 않으면 스킵
        return authorization == null || !authorization.startsWith("Bearer");
    }

    // 토큰 검증
    private Map<String, Object> verifyJws(HttpServletRequest request) {
        // Authorization 헤더에서 Bearer를 제거하여 순수한 JWT 토큰 추출
        String jws = request.getHeader("Authorization").replace("Bearer ", "");
        // 서명을 검증할 시크릿 키 생성
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        // 서명 검증 후 claim 반환
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        return claims;
    }

    // 인증 정보 설정
    private void setAuthenticationToContext(Map<String, Object> claims) {
        // claim에서 이메일과 권한 추출
        String username = (String) claims.get("username");
        @SuppressWarnings("unchecked")
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((Set<Member.Role>) claims.get("roles"));
        // 인증 정보 생성 (이미 JWT토큰으로 인증 되었기 때문에 credentials은 굳이 저장할 필요 없음 -> null)
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        // SecurityContext에 인증정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
