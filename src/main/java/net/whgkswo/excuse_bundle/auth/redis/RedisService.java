package net.whgkswo.excuse_bundle.auth.redis;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import net.whgkswo.excuse_bundle.serialize.JsonSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JsonSerializer jsonSerializer;

    // 저장
    public <T> void put(RedisKey key, T value, int durationOfSec){
        try{
            String json = jsonSerializer.serialize(value);

            redisTemplate.opsForValue().set(
                    key.toString(),
                    json,
                    Duration.ofSeconds(durationOfSec)
            );
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 저장 (만료 기한 없음)
    public <T> void put(RedisKey key, T value){
        try{
            String json = jsonSerializer.serialize(value);

            redisTemplate.opsForValue().set(key.toString(), json);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 조회 (문자열)
    public Optional<String> getRawString(RedisKey key){
        try{
            String value = redisTemplate.opsForValue().get(key.toString());
            return Optional.ofNullable(value);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 조회 (타입 지정)
    public <T> Optional<T> get(RedisKey key, Class<T> clazz){
        try{
            String json = redisTemplate.opsForValue().get(key.toString());
            if(json == null) return Optional.empty();

            T value = jsonSerializer.deserialize(json, clazz);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }

    // 갱신 (만료 시간은 그대로)
    public <T> void update(RedisKey key, T updatedValue, BusinessLogicException e){
        long ttl = getTtlOfSecOrThrow(key, e);

        if(ttl >= Integer.MAX_VALUE){ // 키는 있지만 만료 없음
            put(key, updatedValue);
        }else{
            put(key, updatedValue, (int) ttl);
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

    // 만료 시간 조회 (공개용 - 키 없을 때 예외 던지기)
    public long getTtlOfSecOrThrow(RedisKey key, BusinessLogicException keyNotFoundEx){
        long ttl = getTtlOfSec(key);

        if(ttl == -1) return Long.MAX_VALUE;
        if(ttl == -2) throw keyNotFoundEx;

        return ttl;
    }

    // 만료 시간 조회 (공개용 - optional버전)
    public Optional<Long> getTtlOfSecOptional(RedisKey key){
        long ttl = getTtlOfSec(key);

        if(ttl == -2 || ttl == 0) return Optional.empty();
        if(ttl == -1) return Optional.of(Long.MAX_VALUE);
        return Optional.of(ttl);
    }

    // 만료 시간 조회 (-1: 키는 있지만 만료 시간이 없음, -2: 키가 없음)
    private long getTtlOfSec(RedisKey key){
        try{
            return redisTemplate.getExpire(key.toString(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.REDIS_CONNECTION_LOST);
        }
    }
}
