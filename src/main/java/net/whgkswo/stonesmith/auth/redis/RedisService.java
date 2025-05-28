package net.whgkswo.stonesmith.auth.redis;

import net.whgkswo.stonesmith.exception.BusinessLogicException;
import net.whgkswo.stonesmith.exception.ExceptionType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "verification-code";
    private static final String VERIFICATION_COMPLETE_PREFIX = "verification-complete";

    public RedisService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

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
    public String get(String key){
        try{
            return redisTemplate.opsForValue().get(key);
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
