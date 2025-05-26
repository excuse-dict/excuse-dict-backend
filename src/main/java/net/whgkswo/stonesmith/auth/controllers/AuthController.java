package net.whgkswo.stonesmith.auth.controllers;

import jakarta.validation.Valid;
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

    public static final String BASE_PATH = "/api/v1/auth";

    // 생성자 주입
    public AuthController(MemberService memberService){
        this.memberService = memberService;
    }

    // 이메일 중복 검증
    @GetMapping("/check-email")
    public ResponseEntity<?> handleEmailDuplicationCheckRequest(@RequestParam String email){
        boolean isEmailDuplicated = memberService.isEmailDuplicated(email);

        return ResponseEntity.ok(
                Response.simpleBoolean(isEmailDuplicated)
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
