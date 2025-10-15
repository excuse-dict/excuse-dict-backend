package net.whgkswo.excuse_bundle.auth.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.jwt.principal.CustomPrincipal;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisKeyMapper;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.email.config.AdminEmailConfig;
import net.whgkswo.excuse_bundle.entities.members.email.dto.EmailVerificationStateDto;
import net.whgkswo.excuse_bundle.entities.members.email.dto.VerificationPurpose;
import net.whgkswo.excuse_bundle.entities.members.email.service.EmailService;
import net.whgkswo.excuse_bundle.exceptions.BadRequestException;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RedisService redisService;
    private final RedisKeyMapper redisKeyMapper;
    private final AdminEmailConfig adminEmailConfig;
    private final EmailService emailService;

    // 메일 인증정보 만료 시간
    private static final int EMAIL_VERIFICATION_DURATION_SEC = 3600;

    // 메일 인증 임시차단 유지 시간
    private static final int VERIFICATION_BLOCK_DURATION_SEC = 1800;


    // 인증 코드 검증
    public void verifyCode(String email, String code, VerificationPurpose purpose){

        // 이메일 차단 여부 확인
        emailService.checkIsEmailBlocked(email);

        // redis 키 준비
        RedisKey.Prefix prefix = redisKeyMapper.getVerificationCodePrefix(purpose);
        RedisKey key = new RedisKey(prefix, email);

        // 인증 코드와 남은 시도 횟수 조회
        Optional<EmailVerificationStateDto> optionalState = redisService.get(key, EmailVerificationStateDto.class);

        // 인증 코드가 없거나 만료됨
        if(optionalState.isEmpty()) throw new BusinessLogicException(ExceptionType.VERIFICATION_CODE_EXPIRED);

        EmailVerificationStateDto verificationState = optionalState.get();

        // 코드가 일치하지 않음
        if(!code.equals(verificationState.code())) handleCodeViolation(verificationState, email, purpose);

        // 일치하면 레디스에서 인증코드 삭제
        redisService.remove(key);

        // 이메일 인증 완료 정보 저장
        RedisKey.Prefix completePrefix = redisKeyMapper.getVerificationCompletePrefix(purpose);
        addVerificationToRedis(email, completePrefix);
    }

    // 인증 코드 틀렸을 때 실행
    private void handleCodeViolation(EmailVerificationStateDto prevState, String email, VerificationPurpose purpose){
        // 라스트 찬스였으면
        if(prevState.failedAttempts() >= EmailVerificationStateDto.MAX_VERIFICATION_ATTEMPTS - 1){
            // 30분 간 해당 이메일 접근 차단
            RedisKey key = new RedisKey(RedisKey.Prefix.VERIFICATION_BLOCK, email);
            redisService.put(key, true, VERIFICATION_BLOCK_DURATION_SEC);

            // 기존 인증정보 삭제
            redisService.remove(new RedisKey(redisKeyMapper.getVerificationCodePrefix(purpose), email));
            redisService.remove(new RedisKey(redisKeyMapper.getVerificationCompletePrefix(purpose), email));

            throw new BusinessLogicException(ExceptionType.VERIFICATION_ATTEMPTS_RAN_OUT);
        }

        EmailVerificationStateDto newState = prevState.plusFailedAttempt();

        // 실패 횟수 증가시키고 ttl 갱신한 뒤 다시 저장
        RedisKey.Prefix prefix = redisKeyMapper.getVerificationCodePrefix(purpose);
        redisService.put(new RedisKey(prefix, email), newState, EmailService.CODE_DURATION_SEC);

        // 클라이언트에 응답
        throw new BusinessLogicException(ExceptionType.wrongVerificationCode(newState.getRemainingAttempts()));
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

    // 토큰에서 id 회원 id 추출
    public long getMemberIdFromAuthentication(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated())
            throw new BadRequestException(ExceptionType.AUTHENTICATION_FAILED);

        // Postman으로 요청한 경우 String Principal이기 때문에 ClassCastException 발생 가능
        // 실제 어플리케이션에서는 정상 동작함
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return principal.memberId();
    }

    // 토큰에서 id 회원 id 추출 (옵셔널)
    public Optional<Long> getOptionalMemberIdFromAuthentication(Authentication authentication){
        if(authentication == null) return Optional.empty();

        return Optional.of(getMemberIdFromAuthentication(authentication));
    }

    public boolean isValidUser(Authentication authentication){

        if(authentication == null || !authentication.isAuthenticated()) return false;

        return authentication.getPrincipal() instanceof CustomPrincipal;
    }
}
