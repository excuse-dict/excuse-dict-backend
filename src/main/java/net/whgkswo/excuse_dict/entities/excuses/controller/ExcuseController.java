package net.whgkswo.excuse_dict.entities.excuses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.cooldown.Cooldown;
import net.whgkswo.excuse_dict.entities.excuses.dto.GenerateExcuseDto;
import net.whgkswo.excuse_dict.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_dict.gemini.dto.GenerateExcuseResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ExcuseController.BASE_URL)
@RequiredArgsConstructor
public class ExcuseController {

    private final ExcuseService excuseService;

    public static final String BASE_URL = "/api/v1/excuses";
    public static final String BASE_URL_ANY = "/api/*/excuses";

    // 비회원용 (회원도 가능 - 회원용과 쿨타임을 공유)
    @PostMapping("/generate/guests")
    @Cooldown(cooldownSeconds = 60)
    public Mono<GenerateExcuseResponseDto> generateExcuse(@RequestBody @Valid GenerateExcuseDto dto,
                                                          @CookieValue(value = "guestToken") String guestToken){
        return excuseService.generateExcuse(dto.situation(), dto.recaptchaToken());
    }

    // 회원용
    // 회원이 비회원용 엔드포인트로 요청을 넣어도 되지만 저건 시큐리티 설정에서 .permitAll()이기 때문에 토큰 만료 시 예외가 발생하지 않음
    // 결과적으로 액세스 토큰이 적절하게 재발급되지 않고 anonymousUser로 들어가기 때문에 회원임에도 비회원으로 취급되어 쿨타임이 길게 적용됨
    // 따라서 회원은 반드시 회원용 엔드포인트로 요청을 보내야 함
    @PostMapping("/generate/members")
    @Cooldown(cooldownSeconds = 5)
    public GenerateExcuseResponseDto generateExcuse(@RequestBody @Valid GenerateExcuseDto dto,
                                       Authentication authentication){

        // 비동기 요청시 스레드 간 Security Context가 유실되는 문제가 발생하여 동기로 전환
        GenerateExcuseResponseDto result = excuseService.generateExcuseInSynchronous(dto.situation(), dto.recaptchaToken());
        return result;
    }
}
