package net.whgkswo.excuse_dict.cooldown;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.auth.service.AuthService;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import net.whgkswo.excuse_dict.guest.service.GuestService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
public class CooldownAspect {

    private final RedisService redisService;
    private final AuthService authService;
    private final GuestService guestService;

    @Before("@annotation(cooldown)")
    public void checkCooldown(JoinPoint joinPoint, Cooldown cooldown){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        HttpServletRequest request = getCurrentRequest();

        // 요청에 문제가 있을 시 쿨다운 체크 스킵 (ex: 401)
        if(request.getAttribute("exception") != null){
            return;
        }

        // 레디스에 넣을 키
        String identifier = getIdentifier(authentication, joinPoint);

        RedisKey redisKey = new RedisKey(RedisKey.Prefix.GENERATOR_COOLDOWN, identifier);

        // 쿨다운 중이면 예외 처리
        if(redisService.containsKey(redisKey)){

            long remainingTime = redisService.getTtlOfSecOptional(redisKey).orElse((long) -1);

            throw new BusinessLogicException(ExceptionType.tooManyGenerate(remainingTime));
        }

        int cooldownSeconds = cooldown.cooldownSeconds();

        // redis에 요청 기록
        redisService.put(redisKey, true, cooldownSeconds);
    }

    // 레디스 키로 쓰일 id 정하기
    private String getIdentifier(Authentication authentication, JoinPoint joinPoint){
        HttpServletRequest request = getCurrentRequest();

        boolean isValidUser = authService.isValidUser(authentication);

        if(isValidUser){
            // 회원이면 회원 id
            return authService.getMemberIdFromAuthentication(authentication) + "";
        }else{
            // 비회원일 경우 uuid
            String guestToken = getGuestTokenFromArgs(joinPoint);
            return guestService.getUuidFromGuestToken(guestToken);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    private String getGuestTokenFromArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            CookieValue cookieValue = param.getAnnotation(CookieValue.class);

            // 쿠키에 "guestToken" 값이 있으면 반환
            if (cookieValue != null && "guestToken".equals(cookieValue.value())) {
                String guestToken = (String) args[i];
                if(guestToken == null || guestToken.isBlank()) throw new BusinessLogicException(ExceptionType.GUEST_TOKEN_REQUIRED);
                return guestToken;
            }
        }

        // 없으면 안 됨
        throw new BusinessLogicException(ExceptionType.GUEST_TOKEN_REQUIRED);
    }
}
