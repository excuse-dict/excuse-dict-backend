package net.whgkswo.stonesmith.auth.controllers;

import jakarta.validation.Valid;
import net.whgkswo.stonesmith.auth.dto.VerifyDto;
import net.whgkswo.stonesmith.auth.service.AuthService;
import net.whgkswo.stonesmith.entities.members.Member;
import net.whgkswo.stonesmith.entities.members.MemberController;
import net.whgkswo.stonesmith.entities.members.MemberDto;
import net.whgkswo.stonesmith.entities.members.MemberService;
import net.whgkswo.stonesmith.responses.Response;
import net.whgkswo.stonesmith.responses.UriHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(AuthController.BASE_PATH)
public class AuthController {
    private MemberService memberService;
    private AuthService authService;

    public static final String BASE_PATH = "/api/v1/auth";


    public AuthController(MemberService memberService, AuthService authService){
        this.memberService = memberService;
        this.authService = authService;
    }

    // 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<?> handleVerifyRequest(@RequestBody VerifyDto dto){
        boolean isValid = authService.verifyCode(dto.email(), dto.verificationCode());

        return ResponseEntity.ok(
                Response.simpleBoolean(isValid)
        );
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> handleSignupRequest(@Valid @RequestBody MemberDto dto){
        Member member = memberService.createUser(dto);
        // 회원가입은 AuthController가 처리하지만 URI는 UserController 기준으로
        URI uri = UriHelper.createURI(MemberController.BASE_PATH, member.getId());

        return ResponseEntity.created(uri).build();
    }
}
