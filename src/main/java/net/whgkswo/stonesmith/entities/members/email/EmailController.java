package net.whgkswo.stonesmith.entities.members.email;

import jakarta.validation.Valid;
import net.whgkswo.stonesmith.entities.members.MemberService;
import net.whgkswo.stonesmith.responses.Response;
import net.whgkswo.stonesmith.responses.dtos.EmailDto;
import net.whgkswo.stonesmith.auth.dto.VerificationCodeResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(EmailController.BASE_PATH)
public class EmailController {
    private EmailService emailService;
    private MemberService memberService;

    public static final String BASE_PATH = "/api/v1/email";

    public EmailController(EmailService emailService, MemberService memberService){
        this.emailService = emailService;
        this.memberService = memberService;
    }

    // 이메일 중복 검증
    @GetMapping("/check-availability")
    public ResponseEntity<?> handleEmailDuplicationCheckRequest(@RequestParam String email){
        emailService.validateEmail(email);

        return ResponseEntity.ok().build();
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
