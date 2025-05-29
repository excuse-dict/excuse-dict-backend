package net.whgkswo.lo8pinggye.entities.members.email;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.lo8pinggye.entities.members.MemberService;
import net.whgkswo.lo8pinggye.responses.Response;
import net.whgkswo.lo8pinggye.responses.dtos.EmailDto;
import net.whgkswo.lo8pinggye.auth.dto.VerificationCodeResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(EmailController.BASE_PATH)
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final MemberService memberService;

    public static final String BASE_PATH = "/api/v1/email";

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
