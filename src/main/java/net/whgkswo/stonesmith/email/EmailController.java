package net.whgkswo.stonesmith.email;

import jakarta.validation.Valid;
import net.whgkswo.stonesmith.responses.dtos.EmailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EmailController.BASE_PATH)
public class EmailController {
    private EmailService emailService;

    public static final String BASE_PATH = "/api/v1/email";

    public EmailController(EmailService emailService){
        this.emailService = emailService;
    }

    @PostMapping("/verification-code")
    public ResponseEntity<?> handleVerificationCodeRequest(@Valid @RequestBody EmailDto dto){
        emailService.sendVerificationEmail(dto.email());

        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }
}
