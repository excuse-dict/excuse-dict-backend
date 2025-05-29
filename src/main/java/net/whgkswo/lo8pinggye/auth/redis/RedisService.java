package net.whgkswo.lo8pinggye.auth.redis;

import lombok.RequiredArgsConstructor;
import net.whgkswo.lo8pinggye.exception.BusinessLogicException;
import net.whgkswo.lo8pinggye.exception.ExceptionType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "verification-code";
    private static final String VERIFICATION_COMPLETE_PREFIX = "verification-complete";

    // 저장
    public void put(String key, String value, int durationOfSec){
        try{
            redisTemplate.opsForValue().set(
                    key,
                    value,
                    Duration.ofSeconds(durationOfSec)
            );
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 조회
    public Optional<String> get(String key){
        try{
            String value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 삭제
    public void remove(String key){
        try{
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 키 조회
    public boolean containsKey(String key){
        try{
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 레디스 키 생성
    private String generateRedisKey(String prefix, String identifier){
        return prefix + ":" + identifier;
    }

    // 인증 코드 레디스 키 생성
    public String getKeyForVerificationCode(String email){
        return generateRedisKey(VERIFICATION_CODE_PREFIX, email);
    }

    // 메일 인증 완료 레디스 키 생성
    public String getKeyForVerificationComplete(String email){
        return generateRedisKey(VERIFICATION_COMPLETE_PREFIX, email);
    }
}
