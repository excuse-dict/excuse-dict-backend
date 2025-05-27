package net.whgkswo.stonesmith.auth.config;

import net.whgkswo.stonesmith.auth.controllers.AuthController;
import net.whgkswo.stonesmith.entities.members.email.EmailController;
import net.whgkswo.stonesmith.entities.members.MemberController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // CORS 활성화
                .cors(Customizer.withDefaults())
                // 누가 접근할 수 있는 요청인지 확인
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트는 모두 허용
                        .requestMatchers("/error").permitAll() // 에러 페이지 허용
                        .requestMatchers(HttpMethod.POST, MemberController.BASE_PATH).permitAll() // 회원가입은 예외
                        .requestMatchers(EmailController.BASE_PATH + "/**").permitAll() // 이메일 관련 API는 예외
                        .requestMatchers(AuthController.BASE_PATH + "/verify").permitAll() // 코드 인증은 예외
                        .requestMatchers(MemberController.BASE_PATH + "/nicknames/**").permitAll() // 닉네임 검증은 예외
                        .requestMatchers("/h2/**").permitAll() // h2 볼때는 예외
                        //.requestMatchers(HttpMethod.GET, PostController.BASE_PATH + "/**").permitAll() // 비회원도 조회는 허용
                        .anyRequest().authenticated() // 위에 명시하지 않은 요청은 전부 인증 필요
                )
                // 악의적인 요청인지 확인
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // API요청들은 CSRF 비활성화 (JWT토큰 사용)
                        .ignoringRequestMatchers("/h2/**") // h2도 예외
                )
                // 같은 도메인에서 iframe 허용 (h2가 iframe 사용)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
        ;
        return http.build();
    }

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
}
