package net.whgkswo.excuse_bundle.cooldown;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class CooldownAspect {

    private final RedisService redisService;
    private final AuthService authService;

    @Before("@annotation(annotation)")
    public void checkCooldown(Cooldown annotation){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        HttpServletRequest request = getCurrentRequest();

        // 요청에 문제가 있을 시 쿨다운 체크 스킵 (ex: 401)
        if(request.getAttribute("exception") != null){
            return;
        }

        String identifier = getIdentifier(authentication);

        RedisKey redisKey = new RedisKey(RedisKey.Prefix.GENERATOR_COOLDOWN, identifier);

        // 쿨다운 중이면 예외 처리
        if(redisService.containsKey(redisKey)){

            long remainingTime = redisService.getTtlOfSecOptional(redisKey).orElse((long) -1);

            throw new BusinessLogicException(ExceptionType.tooManyGenerate(remainingTime));
        }

        int cooldown = annotation.cooldownSeconds();

        // redis에 요청 기록
        redisService.put(redisKey, true, cooldown);
    }

    // 레디스 키로 쓰일 id 정하기
    private String getIdentifier(Authentication authentication){
        HttpServletRequest request = getCurrentRequest();

        boolean isValidUser = authService.isValidUser(authentication);

        return isValidUser ?
                authService.getMemberIdFromAuthentication(authentication) + "" // 회원이면 회원 id
                : getClientIp(request); // 비회원일 경우 ip
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        // 여러 가지 헤더명으로 시도해보며 IP 추출
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
