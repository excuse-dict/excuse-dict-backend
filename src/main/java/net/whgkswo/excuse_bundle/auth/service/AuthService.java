package net.whgkswo.excuse_bundle.auth.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisKeyMapper;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.entities.members.Member;
import net.whgkswo.excuse_bundle.entities.members.email.VerificationPurpose;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RedisService redisService;
    private final RedisKeyMapper redisKeyMapper;

    @Value("${secret.admin.emails}")
    private List<String> adminEmails;

    // 메일 인증정보 만료 시간
    private static final int EMAIL_VERIFICATION_DURATION_SEC = 3600;

    // 인증 코드 검증
    public void verifyCode(String email, String code, VerificationPurpose purpose){
        RedisKey.Prefix prefix = redisKeyMapper.fromVerificationPurpose(purpose);
        RedisKey key = new RedisKey(prefix, code);

        Optional<String> storedCode = redisService.get(key);

        // 인증 코드가 없거나 만료됨
        if(storedCode.isEmpty()) throw new BusinessLogicException(ExceptionType.VERIFICATION_CODE_EXPIRED);

        // 코드가 일치하지 않음
        if(!code.equals(storedCode.get())) throw new BusinessLogicException(ExceptionType.WRONG_VERIFICATION_CODE);

        // 일치하면 레디스에서 인증코드 삭제
        redisService.remove(key);
        // 이메일 인증 완료 정보 저장
        addVerificationToRedis(email, prefix);
    }

    // 이메일 인증 완료 정보 저장
    private void addVerificationToRedis(String email, RedisKey.Prefix prefix){
        RedisKey key = new RedisKey(prefix, email);

        // 레디스에 저장 (value는 의미 없음. 키만 확인하면 됨)
        redisService.put(key, "true", EMAIL_VERIFICATION_DURATION_SEC);
    }

    // 이메일 인증 여부 검사 (회원가입)
    public void checkEmailVerified(String email){
        RedisKey key = new RedisKey(RedisKey.Prefix.VERIFICATION_COMPLETE_REGISTRATION, email);

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

    // 비밀번호 재설정용 토큰 발급
    public String generatePasswordResetToken(String email){
        // 토큰 생성 (32바이트 랜덤 문자열)
        String token = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");

        // 레디스에 저장 (30분 후 만료)
        RedisKey key = new RedisKey(RedisKey.Prefix.PASSWORD_RESET_TOKEN, email);
        redisService.put(key, token, 1800);

        return token;
    }
}
