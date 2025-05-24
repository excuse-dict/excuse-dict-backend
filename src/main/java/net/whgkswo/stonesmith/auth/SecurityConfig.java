package net.whgkswo.stonesmith.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // 누가 접근할 수 있는 요청인지 확인
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AuthController.BASE_PATH + "/**").permitAll() // 회원가입은 인증 예외
                        .requestMatchers("/h2/**").permitAll() // h2 볼때는 인증 예외
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
}
