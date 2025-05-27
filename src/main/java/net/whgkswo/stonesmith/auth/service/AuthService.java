package net.whgkswo.stonesmith.auth.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    RedisTemplate<String, String> redisTemplate;

    public AuthService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    // 인증 코드 레디스 키 생성
    public static String getRedisKeyForVerificationCode(String email){
        return "verification:" + email;
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code){
        String key = getRedisKeyForVerificationCode(email);
        String redisCode = redisTemplate.opsForValue().get(key);

        // 일치하면
        if(code.equals(redisCode)){ // nullable한 값이 우측 -> null체크도 됨
            // 레디스에서 키 삭제
            redisTemplate.delete(key);
            return true;
        }
        // 일치하는 값이 없으면(틀렸거나 만료)
        return false;
    }
}
