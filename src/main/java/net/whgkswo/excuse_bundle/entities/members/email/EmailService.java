package net.whgkswo.excuse_bundle.entities.members.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisKeyMapper;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.auth.verify.VerificationCode;
import net.whgkswo.excuse_bundle.entities.members.Member;
import net.whgkswo.excuse_bundle.entities.members.MemberRepository;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final Environment environment;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final RedisKeyMapper redisKeyMapper;

    private static final int CODE_DURATION_SEC = 300;

    // 이메일 유효성 검사
    public void validateEmail(String email){
        List<Member> members = memberRepository.findAll();
        // 이메일 중복 검사
        for(Member member : members){
            if(member.getEmail().equals(email)) throw new BusinessLogicException(ExceptionType.DUPLICATED_EMAIL);
        }
    }

    public LocalDateTime sendVerificationEmail(String email, VerificationPurpose purpose){
        // 인증 코드 생성
        VerificationCode code = generateVerificationCode();

        // 코드 만료시간 계산
        LocalDateTime expiryTime = getCodeExpiryTime();

        // redis 키 생성
        RedisKey.Prefix prefix = redisKeyMapper.getVerificationCodePrefix(purpose);
        RedisKey key = new RedisKey(prefix, email);
        // redis에 코드 저장(5분 후 만료)
        redisService.put(key, code, CODE_DURATION_SEC);

        // 메일 발송
        sendEmail(email, code, expiryTime);

        return expiryTime;
    }

    // 인증 코드 생성
    private VerificationCode generateVerificationCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; i++){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return new VerificationCode(code.toString());
    }

    // 관리자 메일 가져오기
    private String getAdminEmail(){
        return environment.getProperty("spring.mail.username");
    }

    // 메일 발송
    private void sendEmail(String email, VerificationCode code, LocalDateTime expiryTime){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String codeValue = code.getCode();

            helper.setTo(email);
            helper.setSubject("[핑계사전] 메일 인증 코드");
            helper.setText(createMail(codeValue, expiryTime), true);
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
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: rgb(177, 178, 209); padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">이메일 인증</h2>
                    <p style="font-size: 16px; color: rgb(121, 120, 139);">
                        아래 인증 코드를 복사하여 입력해주세요.
                    </p>
                    <div style="background-color: rgb(121, 120, 139);; color: white; font-size: 24px; 
                                font-weight: bold; text-align: center; padding: 15px; 
                                border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p style="font-size: 14px; color: rgb(121, 120, 139);">
                        이 코드는 %s에 만료됩니다.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(code, fExpiryTime);
    }
}
