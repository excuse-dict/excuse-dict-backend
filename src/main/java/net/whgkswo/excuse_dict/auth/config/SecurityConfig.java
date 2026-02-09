package net.whgkswo.excuse_dict.auth.config;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.admin.AdminController;
import net.whgkswo.excuse_dict.auth.CustomAuthorityUtils;
import net.whgkswo.excuse_dict.auth.controller.AuthController;
import net.whgkswo.excuse_dict.auth.handler.MemberAuthenticationExceptionHandler;
import net.whgkswo.excuse_dict.auth.handler.MemberAuthenticationFailureHandler;
import net.whgkswo.excuse_dict.auth.handler.MemberAuthenticationSuccessHandler;
import net.whgkswo.excuse_dict.auth.jwt.entrypoint.JwtAuthenticationFilter;
import net.whgkswo.excuse_dict.auth.jwt.service.JwtTokenService;
import net.whgkswo.excuse_dict.auth.jwt.token.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_dict.auth.jwt.token.verification.JwtVerificationFilter;
import net.whgkswo.excuse_dict.entities.excuses.controller.ExcuseController;
import net.whgkswo.excuse_dict.entities.members.email.controller.EmailController;
import net.whgkswo.excuse_dict.entities.members.core.controller.MemberController;
import net.whgkswo.excuse_dict.entities.posts.post_core.controller.PostController;
import net.whgkswo.excuse_dict.entities.posts.tags.controller.TagController;
import net.whgkswo.excuse_dict.guest.controller.GuestController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenizer jwtTokenizer;
    private final JwtTokenService jwtTokenService;
    private final CustomAuthorityUtils authorityUtils;
    private final MemberAuthenticationExceptionHandler memberAuthenticationExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // 악의적인 요청인지 확인
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // API요청들은 CSRF 비활성화 (JWT토큰 사용)
                        .ignoringRequestMatchers("/h2/**") // h2도 예외
                        .ignoringRequestMatchers("/admin/**")
                )
                // CORS 활성화
                .cors(Customizer.withDefaults())
                // HTTP를 무상태로 관리 (세션 사용 안함 - JWT를 쓰면 세션이 아예 필요 없음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증되지 않은 요청 처리 진입점 등록
                .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(memberAuthenticationExceptionHandler)
                )
                // 누가 접근할 수 있는 요청인지 확인
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트는 모두 허용
                        .requestMatchers("/error").permitAll() // 에러 페이지 허용
                        .requestMatchers(HttpMethod.POST, MemberController.BASE_URL_ANY).permitAll() // 회원가입은 예외
                        .requestMatchers(MemberController.BASE_URL_ANY + "/emails/is-registered").permitAll() // 이메일 가입여부도 예외
                        .requestMatchers(MemberController.BASE_URL_ANY + "/nicknames/**").permitAll() // 닉네임 검증은 예외
                        .requestMatchers(HttpMethod.PATCH,MemberController.BASE_URL_ANY + "/passwords/reset").permitAll() // 비밀번호 변경도 허용
                        .requestMatchers(EmailController.BASE_URL_ANY + "/**").permitAll() // 이메일 관련 API는 예외
                        .requestMatchers(AuthController.BASE_URL_ANY + "/**").permitAll() // auth 전체 허용
                        .requestMatchers(HttpMethod.POST, ExcuseController.BASE_URL_ANY + "/generate/guests").permitAll() // 핑계 생성기(비회원용)
                        .requestMatchers("/h2/**").permitAll() // h2 볼때는 예외
                        // 회원/비회원 모두 호출할 수 있지만 permitAll()로 하면 아예 인증 절차 자체를 스킵 -> authentication이 null로 들어오는 문제가 있어서 hasAnyRole()로 바꿈
                        // 토큰 없이 요청 시 JwtVerificationFilter에서 ROLE_ANONYMOUS를 가진 익명 인증 객체 생성
                        // 이렇게 생성된 익명 인증정보가 실제로 핸들러까진 도달하지 못하지만(null 전달 - 이유 불명) 토큰의 유무를 판별하는 소기의 목적은 달성
                        .requestMatchers(HttpMethod.GET, PostController.BASE_URL_ANY + "/**").hasAnyRole("USER", "ADMIN", "ANONYMOUS") // 비회원도 조회는 허용
                        .requestMatchers(HttpMethod.POST, PostController.BASE_URL_ANY + "/search").hasAnyRole("USER", "ADMIN", "ANONYMOUS") // 검색 조건 복잡해서 POST
                        .requestMatchers(GuestController.BASE_URL_ANY + "/**").permitAll() // 비회원용 컨트롤러
                        .requestMatchers(HttpMethod.POST, TagController.BASE_URL_ANY).permitAll() // 태그 조회는 누구나 가능, GET이 아니고 POST인 이유는 검색 조건이 복잡해서...
                        .requestMatchers(AdminController.BASE_URL + "/**").permitAll() // 서버 내부 curl 요청으로 트리거, IP는 컨트롤러에서 체크
                        .anyRequest().authenticated() // 위에 명시하지 않은 요청은 전부 인증 필요
                )
                // 같은 도메인에서 iframe 허용 (h2가 iframe 사용)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                // CustomFilterConfigurer 적용
                .with(new CustomFilterConfigurer(), Customizer.withDefaults())
        ;
        return http.build();
    }

    // CORS 관련 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // 로컬 개발용
                "http://127.0.0.1:3000",      // 로컬 개발용
                "http://218.146.27.34:3000",  // 집피 테스트용
                "http://172.18.48.1:3000",    // 가상 어댑터 IP (혹시 몰라서)
                "http://172.19.144.1:3000",    // WSL IP (혹시 몰라서)
                "http://152.69.235.140",      // 프로덕션 인스턴스 (80번 포트)
                "https://152.69.235.140",     // 프로덕션 인스턴스 (443번 포트)
                "http://152.69.235.140:3000", // 개발 테스트용
                "https://152.69.235.140:3000", // 개발 테스트용
                "http://exdict.site",         // 프로덕션 도메인 (80번 포트)
                "https://exdict.site",        // 프로덕션 도메인 (443번 포트)
                "http://exdict.site:3000",    // 개발 테스트용
                "https://exdict.site:3000"    // 개발 테스트용
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        // 클라이언트가 토큰에 접근할 수 있도록 허용
        // 그럼 브라우저에선 이거 없어도 왜 보이냐? -> Network 탭에서 보이는 건 HTTP 프로토콜 레벨에서 접근하는 것이기 때문에 안 숨겨짐
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Refresh"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // 추가 필터를 시큐리티 필터 체인에 일괄 등록
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity>{
        @Override
        public void configure(HttpSecurity builder) {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenService);
            // 로그인 요청 엔드포인트 설정
            jwtAuthenticationFilter.setFilterProcessesUrl(AuthController.BASE_URL + "/login");
            // 로그인 요청 후처리 로직 적용
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler());

            // JWT 토큰 유효성 검증 필터 적용
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            builder
                    .addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }
}
