package net.whgkswo.excuse_bundle.entities.members.email.controller;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.members.email.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EmailController.BASE_PATH)
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final MemberService memberService;

    public static final String BASE_PATH = "/api/v1/emails";
    public static final String BASE_PATH_ANY = "/api/*/emails";

    // 이메일 중복 검증
    @GetMapping("/check-availability")
    public ResponseEntity<?> handleEmailDuplicationCheckRequest(@RequestParam String email){
        emailService.validateEmail(email);

        return ResponseEntity.ok().build();
    }
}
