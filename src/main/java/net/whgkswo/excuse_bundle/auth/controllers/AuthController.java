package net.whgkswo.excuse_bundle.auth.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.verify.VerificationCodeResponseDto;
import net.whgkswo.excuse_bundle.auth.verify.VerifyDto;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.members.MemberController;
import net.whgkswo.excuse_bundle.entities.members.MemberDto;
import net.whgkswo.excuse_bundle.entities.members.MemberService;
import net.whgkswo.excuse_bundle.entities.members.email.EmailService;
import net.whgkswo.excuse_bundle.entities.members.email.EmailVerificationRequestDto;
import net.whgkswo.excuse_bundle.entities.members.email.VerificationPurpose;
import net.whgkswo.excuse_bundle.responses.Response;
import net.whgkswo.excuse_bundle.responses.UriHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping(AuthController.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final AuthService authService;
    private final EmailService emailService;

    public static final String BASE_PATH = "/api/v1/auth";
    public static final String BASE_PATH_ANY = "/api/*/auth";

    @PostMapping("/verify/codes")
    public ResponseEntity<?> handleVerificationCodeRequest(@Valid @RequestBody EmailVerificationRequestDto dto){
        // 코드 생성하고 메일 보낸 후 만료시간 받아오기
        LocalDateTime expiryTime = emailService.sendVerificationEmail(dto.email(), dto.purpose());

        return ResponseEntity.ok(
                Response.of(new VerificationCodeResponseDto(dto.email(), expiryTime))
        );
    }

    // 회원가입용 인증 코드 검증
    @PostMapping("/verify/signup")
    public ResponseEntity<?> verifySignupCode(@RequestBody VerifyDto dto){
        authService.verifyCode(dto.email(), dto.verificationCode(), VerificationPurpose.REGISTRATION);

        return ResponseEntity.ok(
                Response.simpleString("인증 코드 검증이 완료되었습니다.")
        );
    }

    // 비밀번호 재설정용 인증 코드 검증
    @PostMapping("/verify/reset-password")
    public ResponseEntity<?> verifyResetPasswordCode(@RequestBody VerifyDto dto){
        authService.verifyCode(dto.email(), dto.verificationCode(), VerificationPurpose.RESET_PASSWORD);

        // 비밀번호 재설정 페이지 접속용 토큰 발급
        String token = authService.generatePasswordResetToken(dto.email());

        return ResponseEntity.ok(
                Response.simpleString(token)
        );
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> handleSignupRequest(@Valid @RequestBody MemberDto dto){
        long memberId = memberService.createMember(dto);
        // 회원가입은 AuthController가 처리하지만 URI는 UserController 기준으로
        URI uri = UriHelper.createURI(MemberController.BASE_PATH, memberId);

        return ResponseEntity.created(uri).build();
    }
}
