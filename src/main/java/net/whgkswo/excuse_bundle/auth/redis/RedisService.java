package net.whgkswo.excuse_bundle.auth.redis;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    // 저장
    public void put(RedisKey key, String value, int durationOfSec){
        try{
            redisTemplate.opsForValue().set(
                    key.toString(),
                    value,
                    Duration.ofSeconds(durationOfSec)
            );
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 조회
    public Optional<String> get(RedisKey key){
        try{
            String value = redisTemplate.opsForValue().get(key.toString());
            return Optional.ofNullable(value);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 삭제
    public void remove(RedisKey key){
        try{
            redisTemplate.delete(key.toString());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 키 조회
    public boolean containsKey(RedisKey key){
        try{
            return redisTemplate.hasKey(key.toString());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }
}
