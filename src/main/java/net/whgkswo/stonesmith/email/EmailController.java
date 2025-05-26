package net.whgkswo.stonesmith.email;

import jakarta.validation.Valid;
import net.whgkswo.stonesmith.responses.Response;
import net.whgkswo.stonesmith.responses.dtos.EmailDto;
import net.whgkswo.stonesmith.responses.dtos.VerificationCodeResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
        // 코드 생성하고 메일 보낸 후 만료시간 받아오기
        LocalDateTime expiryTime = emailService.sendVerificationEmail(dto.email());

        return ResponseEntity.ok(
                Response.of(new VerificationCodeResponseDto(dto.email(), expiryTime))
        );
    }
}
