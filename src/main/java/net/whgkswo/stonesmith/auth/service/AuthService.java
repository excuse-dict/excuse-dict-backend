package net.whgkswo.stonesmith.auth.service;

import net.whgkswo.stonesmith.auth.redis.RedisService;
import net.whgkswo.stonesmith.entities.members.Member;
import net.whgkswo.stonesmith.exception.BusinessLogicException;
import net.whgkswo.stonesmith.exception.ExceptionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private RedisService redisService;

    @Value("${secret.admin.emails}")
    private List<String> adminEmails;

    // 메일 인증정보 만료 시간
    private static final int EMAIL_VERIFICATION_DURATION_SEC = 3600;

    public AuthService(RedisService redisService){
        this.redisService = redisService;
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code){
        String key = redisService.getKeyForVerificationCode(email);
        String storedCode = redisService.get(key);

        // 일치하면
        if(code.equals(storedCode)){ // nullable한 값이 우측 -> null체크도 됨
            // 레디스에서 인증코드 삭제
            redisService.remove(key);
            // 이메일 인증 완료 정보 저장
            addVerificationToRedis(email);
            return true;
        }
        // 일치하는 값이 없으면(틀렸거나 만료)
        return false;
    }

    // 이메일 인증 완료 정보 저장
    private void addVerificationToRedis(String email){
        String key = redisService.getKeyForVerificationComplete(email);

        // 레디스에 저장 (value는 의미 없음. 키만 확인하면 됨)
        redisService.put(key, "true", EMAIL_VERIFICATION_DURATION_SEC);
    }

    // 이메일 인증 여부 검사
    public void checkEmailVerified(String email){
        String key = redisService.getKeyForVerificationComplete(email);
        // 인증 정보 없으면 예외 던지기
        if(!redisService.containsKey(key)) throw new BusinessLogicException(ExceptionType.EMAIL_NOT_VERIFIED);
    }

    // 회원 권한 부여
    public void giveRoles(Member member){
        if(adminEmails.contains(member.getEmail())){
            member.addRole(Member.Role.ADMIN);
        }
        member.addRole(Member.Role.USER);
    }
}
