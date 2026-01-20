package net.whgkswo.excuse_dict.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SchedulerLoggingAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object logScheduler(ProceedingJoinPoint joinPoint) throws Throwable{
        String methodName = joinPoint.getSignature().getName();

        log.info("=============== Scheduled: {} START ===============", methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("=============== Scheduled: {} END (Success) ===============", methodName);

            return result;
        } catch (Exception e) {
            log.error("=============== Scheduled: {} END (Failed) ===============", methodName, e);

            return null;
        }
    }
}
