package net.whgkswo.lo8pinggye.auth.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.lo8pinggye.auth.dto.VerifyDto;
import net.whgkswo.lo8pinggye.auth.service.AuthService;
import net.whgkswo.lo8pinggye.entities.members.MemberController;
import net.whgkswo.lo8pinggye.entities.members.MemberDto;
import net.whgkswo.lo8pinggye.entities.members.MemberService;
import net.whgkswo.lo8pinggye.responses.Response;
import net.whgkswo.lo8pinggye.responses.UriHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(AuthController.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final AuthService authService;

    public static final String BASE_PATH = "/api/v1/auth";

    // 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<?> handleVerifyRequest(@RequestBody VerifyDto dto){
        authService.verifyCode(dto.email(), dto.verificationCode());

        return ResponseEntity.ok(
                Response.simpleString("인증 코드 검증이 완료되었습니다.")
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
