package net.whgkswo.excuse_bundle.auth.jwt.token.verification;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.CustomAuthorityUtils;
import net.whgkswo.excuse_bundle.auth.jwt.principal.CustomPrincipal;
import net.whgkswo.excuse_bundle.auth.jwt.token.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.posts.post_core.controller.PostController;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 토큰 유효성 검증 필터
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        try{
            if (authorization != null && authorization.startsWith("Bearer ")) {
                // 기존 JWT 처리 로직
                Map<String, Object> claims = verifyJws(request);
                setAuthenticationToContext(claims);
            } else {
                // 토큰이 없으면 익명 Authentication 생성
                setAnonymousAuthenticationToContext();
            }
        }catch (Exception e){
            // 예외 발생 시 SecurifyContext에 Authentication이 저장되지 않음 -> AuthenticationException 발생
            // AuthenticationEntryPoint가 AuthenticationException를 캐치해서 처리하는데
            // 여기서 attribute를 설정해 둔 구체적인 예외 정보를 뽑아서 사용함
            request.setAttribute("exception", e);
        }
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // true면 필터 예외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 특정 요청은 토큰 없으면 익명 인증 객체 생성
        if(method.equals("GET") && path.startsWith(PostController.BASE_URL)){
            return false;
        }

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

    // 임시 인증 정보 설정 (요청 끝나면 삭제)
    // JWT토큰인데 컨텍스트에 왜 저장하냐? -> 로그인 정보를 저장하지 않는다는 거고, 이건 일반 요청마다 임시로 생성하는 컨텍스트 정보임
    // 요청 수행 이후 필터 체인이 자동으로 컨텍스트를 초기화
    // 그럼 비JWT 방식에서 로그인 인증 시에 컨텍스트에 저장하는 Authentication은 왜 초기화되지 않는가? -> 컨텍스트를 거쳐 세션 저장소로 이동하기 때문
    private void setAuthenticationToContext(Map<String, Object> claims) {
        // claim 추출
        String username = (String) claims.get("username");
        long memberId = Long.parseLong(claims.get("memberId").toString());

        List<Member.Role> roles = ((List<String>) claims.get("roles")).stream()
                .map(role -> Member.Role.valueOf(role))
                .collect(Collectors.toList());

        List<GrantedAuthority> authorities = authorityUtils.createAuthorities(roles);

        CustomPrincipal principal = new CustomPrincipal(username, memberId);

        // 인증 정보 생성 (이미 JWT토큰으로 인증 되었기 때문에 credentials은 굳이 저장할 필요 없음 -> null)
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        // SecurityContext에 인증정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 익명 인증정보 생성
    private void setAnonymousAuthenticationToContext() {
        UsernamePasswordAuthenticationToken anonymousAuth = new UsernamePasswordAuthenticationToken(
                "anonymous",
                null,
                List.of(new SimpleGrantedAuthority("ANONYMOUS"))
        );
        anonymousAuth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
    }
}
