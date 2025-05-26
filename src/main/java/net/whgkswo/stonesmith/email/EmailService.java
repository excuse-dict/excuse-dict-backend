package net.whgkswo.stonesmith.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.whgkswo.stonesmith.exception.BusinessLogicException;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class EmailService {
    private JavaMailSender mailSender;
    private RedisTemplate<String, String> redisTemplate;
    private Environment environment;

    private static final long CODE_DURATION_SEC = 300;

    public EmailService(JavaMailSender mailSender, RedisTemplate<String, String> redisTemplate, Environment environment){
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }

    public void sendVerificationEmail(String email){
        // 인증 코드 생성
        String code = generateVerificationCode();

        // redis에 코드 저장(5분 후 만료)
        try{
            redisTemplate.opsForValue().set(
                    getRedisKey(email),
                    code,
                    Duration.ofSeconds(CODE_DURATION_SEC)
            );
        } catch (Exception e) {
            throw new BusinessLogicException("Redis 서버 연결 불가");
        }

        // 메일 발송
        sendEmail(email, code);
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code){
        String key = getRedisKey(email);
        String redisCode = redisTemplate.opsForValue().get(key);

        // 일치하면
        if(code.equals(redisCode)){ // nullable한 값이 우측 -> null체크도 됨
            // 레디스에서 키 삭제
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    // 레디스 키 생성
    private String getRedisKey(String email){
        return "verification:" + email;
    }

    // 인증 코드 생성
    private String generateVerificationCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; i++){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    // 관리자 메일 가져오기
    private String getAdminEmail(){
        return environment.getProperty("spring.mail.username");
    }

    // 메일 발송
    private void sendEmail(String email, String code){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[돌장장이] 메일 인증 코드");
            helper.setText(createMail(code), true);
            helper.setFrom(getAdminEmail());

            // 메일 전송
            mailSender.send(message);

        }catch (MessagingException e){
            throw new BusinessLogicException("메일 전송 실패!");
        }
    }

    // 코드 만료시간 계산
    private LocalDateTime getCodeExpiryTime(){
        return LocalDateTime.now().plusSeconds(EmailService.CODE_DURATION_SEC);
    }

    // 메일 생성
    private String createMail(String code){
        // 만료시간 계산
        LocalDateTime expiryTime = getCodeExpiryTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String fExpiryTime = expiryTime.format(formatter);

        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: rgb(195, 208, 221); padding: 20px; border-radius: 10px;">
                    <h2 style="color: #333; text-align: center;">이메일 인증</h2>
                    <p style="font-size: 16px; color: #666;">
                        아래 인증 코드를 복사하여 입력해주세요.
                    </p>
                    <div style="background-color: rgb(130, 153, 167);; color: white; font-size: 24px; 
                                font-weight: bold; text-align: center; padding: 15px; 
                                border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p style="font-size: 14px; color: #999;">
                        이 코드는 %s에 만료됩니다.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(code, fExpiryTime);
    }


}
