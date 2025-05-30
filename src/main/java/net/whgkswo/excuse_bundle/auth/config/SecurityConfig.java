package net.whgkswo.excuse_bundle.auth.config;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.CustomAuthorityUtils;
import net.whgkswo.excuse_bundle.auth.controllers.AuthController;
import net.whgkswo.excuse_bundle.auth.handlers.MemberAuthenticationFailureHandler;
import net.whgkswo.excuse_bundle.auth.handlers.MemberAuthenticationSuccessHandler;
import net.whgkswo.excuse_bundle.auth.jwt.entrypoint.JwtAuthenticationFilter;
import net.whgkswo.excuse_bundle.auth.jwt.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_bundle.auth.jwt.verification.JwtVerificationFilter;
import net.whgkswo.excuse_bundle.entities.members.email.EmailController;
import net.whgkswo.excuse_bundle.entities.members.MemberController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // 악의적인 요청인지 확인
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // API요청들은 CSRF 비활성화 (JWT토큰 사용)
                        .ignoringRequestMatchers("/h2/**") // h2도 예외
                )
                // CORS 활성화
                .cors(Customizer.withDefaults())
                // HTTP를 무상태로 관리 (세션 사용 안함 - JWT를 쓰면 세션이 아예 필요 없음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 누가 접근할 수 있는 요청인지 확인
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트는 모두 허용
                        .requestMatchers("/error").permitAll() // 에러 페이지 허용
                        .requestMatchers(HttpMethod.POST, MemberController.BASE_PATH_ANY).permitAll() // 회원가입은 예외
                        .requestMatchers(EmailController.BASE_PATH_ANY + "/**").permitAll() // 이메일 관련 API는 예외
                        .requestMatchers(MemberController.BASE_PATH_ANY + "/emails/is-registered").permitAll() // 이메일 가입여부도 예외
                        .requestMatchers(AuthController.BASE_PATH_ANY + "/verify/**").permitAll() // 코드 인증은 예외
                        .requestMatchers(AuthController.BASE_PATH_ANY + "/login").permitAll() // 로그인도 예외
                        .requestMatchers(MemberController.BASE_PATH_ANY + "/nicknames/**").permitAll() // 닉네임 검증은 예외
                        .requestMatchers("/h2/**").permitAll() // h2 볼때는 예외
                        //.requestMatchers(HttpMethod.GET, PostController.BASE_PATH + "/**").permitAll() // 비회원도 조회는 허용
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // JWT인증 필터를 스프링 시큐리티에 등록
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity>{
        @Override
        public void configure(HttpSecurity builder) {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);
            // 로그인 요청 엔드포인트 설정
            jwtAuthenticationFilter.setFilterProcessesUrl(AuthController.BASE_PATH + "/login");
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
