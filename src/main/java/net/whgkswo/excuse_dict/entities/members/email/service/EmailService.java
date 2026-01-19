package net.whgkswo.excuse_dict.entities.members.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisKeyMapper;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.entities.members.core.repositoriy.MemberRepository;
import net.whgkswo.excuse_dict.entities.members.email.dto.EmailVerificationStateDto;
import net.whgkswo.excuse_dict.entities.members.email.dto.VerificationPurpose;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import net.whgkswo.excuse_dict.lib.random.RandomCodeGenerator;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final Environment environment;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final RedisKeyMapper redisKeyMapper;
    private final RandomCodeGenerator randomCodeGenerator;

    public static final int CODE_DURATION_SEC = 300; // 코드 유효기간
    private static final int CODE_RECREATION_COOLDOWN = 30; // 코드 재발급 최소 대기시간
    private static final int MIN_TTL_FOR_CODE_RECREATION = CODE_DURATION_SEC - CODE_RECREATION_COOLDOWN;
    private static final int VERIFICATION_CODE_LENGTH = 6;

    // 이메일 유효성 검사
    public void validateEmail(String email){

        // 이메일 차단 여부 확인
        checkIsEmailBlocked(email);

        // 이메일 중복 검사
        if(memberRepository.existsByEmail(email)) {
            throw new BusinessLogicException(ExceptionType.DUPLICATED_EMAIL);
        }
    }

    public LocalDateTime sendVerificationEmail(String email, VerificationPurpose purpose){

        // 이메일 차단 여부 확인
        checkIsEmailBlocked(email);

        // redis 키 생성
        RedisKey.Prefix prefix = redisKeyMapper.getVerificationCodePrefix(purpose);
        RedisKey key = new RedisKey(prefix, email);

        // 일정 시간 이내에 다시 요청할 수 없음
        Optional<Long> ttl = redisService.getTtlOfSecOptional(key);
        if(ttl.isPresent() && ttl.get() >= MIN_TTL_FOR_CODE_RECREATION){
            long timeToWait = ttl.get() - MIN_TTL_FOR_CODE_RECREATION;
            throw new BusinessLogicException(ExceptionType.tooManyVerificationCodeRequest(timeToWait));
        }

        // 인증 코드 생성
        String code = randomCodeGenerator.generateRandomCode(VERIFICATION_CODE_LENGTH);

        // 코드 만료시간 계산
        LocalDateTime expiryTime = getCodeExpiryTime();

        // redis에 코드 저장(5분 후 만료)
        Optional<EmailVerificationStateDto> optionalState = redisService.get(key, EmailVerificationStateDto.class); // 기존 코드 있으면
        int failedAttempts = optionalState.isPresent() ? optionalState.get().failedAttempts() : 0; // 실패 횟수 유지
        EmailVerificationStateDto state = new EmailVerificationStateDto(code, failedAttempts);

        redisService.put(key, state, CODE_DURATION_SEC);

        // 메일 발송
        sendEmail(email, code, expiryTime);

        return expiryTime;
    }

    // 이메일 인증 접근 차단 여부 확인
    public void checkIsEmailBlocked(String email){
        RedisKey blockKey = new RedisKey(RedisKey.Prefix.VERIFICATION_BLOCK, email);
        Optional<Long> optionalTtl = redisService.getTtlOfSecOptional(blockKey);

        if(optionalTtl.isPresent()){
            int remainingTime = optionalTtl.get().intValue();
            throw new BusinessLogicException(ExceptionType.verificationBlocked(remainingTime));
        }
    }

    // 관리자 메일 가져오기
    private String getAdminEmail(){
        return environment.getProperty("spring.mail.username");
    }

    // 메일 발송
    private void sendEmail(String email, String code, LocalDateTime expiryTime){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[핑계사전] 메일 인증 코드");
            helper.setText(createMail(code, expiryTime), true);
            helper.setFrom(getAdminEmail());

            // 메일 전송
            mailSender.send(message);

        }catch (MessagingException e){
            throw new BusinessLogicException(ExceptionType.FAILED_TO_SEND_MAIL);
        }
    }

    // 코드 만료시간 계산
    private LocalDateTime getCodeExpiryTime(){
        return LocalDateTime.now().plusSeconds(EmailService.CODE_DURATION_SEC);
    }

    // 메일 생성
    private String createMail(String code, LocalDateTime expiryTime){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String fExpiryTime = expiryTime.format(formatter);

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f5;padding:20px">
                <div style="background:white;padding:30px;border-radius:8px;box-shadow:0 2px 8px rgba(177, 178, 209, 0.2)">
                    <h2 style="color:#333;text-align:center;margin-bottom:20px">이메일 인증</h2>
                    <p style="color:#666;font-size:18px;margin-bottom:30px">아래 인증 코드를 복사하여 입력해주세요.</p>
                    <div style="background:#79788b;color:white;font-size:32px;font-weight:bold;text-align:center;padding:20px;border-radius:4px;letter-spacing:5px;margin:30px 0">%s</div>
                    <p style="color:#999;font-size:14px;text-align:center">이 코드는 %s에 만료됩니다.</p>
                </div>
            </div>
            """.formatted(code, fExpiryTime);
    }
}
