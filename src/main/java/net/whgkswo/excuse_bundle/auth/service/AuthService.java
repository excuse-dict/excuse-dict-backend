package net.whgkswo.excuse_bundle.auth.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisKeyMapper;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.auth.verify.VerificationCode;
import net.whgkswo.excuse_bundle.entities.members.core.entities.Member;
import net.whgkswo.excuse_bundle.entities.members.email.AdminEmailConfig;
import net.whgkswo.excuse_bundle.entities.members.email.VerificationPurpose;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RedisService redisService;
    private final RedisKeyMapper redisKeyMapper;
    private final AdminEmailConfig adminEmailConfig;

    // 메일 인증정보 만료 시간
    private static final int EMAIL_VERIFICATION_DURATION_SEC = 3600;

    // 인증 코드 생성
    public VerificationCode generateVerificationCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; i++){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return new VerificationCode(code.toString());
    }

    // 인증 코드 검증
    public void verifyCode(String email, String code, VerificationPurpose purpose){
        RedisKey.Prefix prefix = redisKeyMapper.getVerificationCodePrefix(purpose);
        RedisKey key = new RedisKey(prefix, email);

        Optional<VerificationCode> optionalCode = redisService.get(key, VerificationCode.class);

        // 인증 코드가 없거나 만료됨
        if(optionalCode.isEmpty()) throw new BusinessLogicException(ExceptionType.VERIFICATION_CODE_EXPIRED);

        VerificationCode storedCode = optionalCode.get();

        // 코드가 일치하지 않음
        if(!code.equals(storedCode.getCode())) {
            // 남은 시도 횟수 차감
            deductRemainingAttempts(key, storedCode);
            throw new BusinessLogicException(ExceptionType.wrongVerificationCode(storedCode));
        };

        // 일치하면 레디스에서 인증코드 삭제
        redisService.remove(key);

        // 이메일 인증 완료 정보 저장
        RedisKey.Prefix completePrefix = redisKeyMapper.getVerificationCompletePrefix(purpose);
        addVerificationToRedis(email, completePrefix);
    }

    // 인증 코드 틀릴 시 남은 시도 횟수 차감
    private void deductRemainingAttempts(RedisKey redisKey, VerificationCode code){
        // 시도 횟수 소진 시 키 삭제
        if(code.getRemainingAttempts() <= 1){ // 마지막 시도
            redisService.remove(redisKey);
            throw new BusinessLogicException(ExceptionType.WRONG_VERIFICATION_CODE_LAST);
        }
        // 시도 횟수 차감
        code.deductAttempts();
        // 레디스에 다시 저장
        redisService.update(redisKey, code, new BusinessLogicException(ExceptionType.VERIFICATION_CODE_EXPIRED));
    }

    // 이메일 인증 완료 정보 저장
    public void addVerificationToRedis(String email, RedisKey.Prefix prefix){
        RedisKey key = new RedisKey(prefix, email);

        // 레디스에 저장 (value는 의미 없음. 키만 확인하면 됨)
        redisService.put(key, "true", EMAIL_VERIFICATION_DURATION_SEC);
    }

    // 이메일 인증 여부 검사 (회원가입, 비밀번호 재설정)
    public void checkEmailVerified(String email, RedisKey.Prefix prefix){
        RedisKey key = new RedisKey(prefix, email);

        if(redisService.containsKey(key)){
            // 확인 후 삭제
            redisService.remove(key);
        }else{
            // 인증 정보 없으면 예외 던지기
            throw new BusinessLogicException(ExceptionType.EMAIL_NOT_VERIFIED);
        }
    }

    // 회원 권한 부여
    public void giveRoles(Member member){
        List<String> adminEmails = adminEmailConfig.getEmails();

        if(adminEmails.contains(member.getEmail())){
            member.addRole(Member.Role.ADMIN);
        }
        member.addRole(Member.Role.USER);
    }
}
