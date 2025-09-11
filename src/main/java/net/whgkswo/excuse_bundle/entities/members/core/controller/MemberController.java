package net.whgkswo.excuse_bundle.entities.members.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.recaptcha.RecaptchaService;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberRegistrationDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.members.nicknames.NicknameService;
import net.whgkswo.excuse_bundle.entities.members.passwords.ResetPasswordDto;
import net.whgkswo.excuse_bundle.general.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(MemberController.BASE_PATH)
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final NicknameService nicknameService;
    private final RecaptchaService recaptchaService;

    public static final String BASE_PATH = "/api/v1/members";
    public static final String BASE_PATH_ANY = "/api/*/members";

    // 닉네임 중복 검사
    @GetMapping("/nicknames/check-availability")
    public ResponseEntity<?> handleNicknameAvailabilityCheck(@RequestParam String nickname){
        // 닉네임 유효성 검사 (걸리면 예외 반환)
        nicknameService.validateNickname(nickname);

        return ResponseEntity.ok("사용할 수 있는 닉네임입니다.");
    }

    // 회원가입
    @PostMapping
    public ResponseEntity<?> handleRegisterRequest(@Valid @RequestBody MemberRegistrationDto dto){
        Member member = memberService.createMember(dto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(member.getId()) // id 다음에 이걸 넣었기 때문에 명시 안해도 순서대로 매칭
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    // 이메일로 가입 여부 검사
    @GetMapping("/emails/is-registered")
    public ResponseEntity<?> handleEmailCheckRequest(@RequestParam String email){
        boolean isRegistered = memberService.isEmailRegistered(email);

        return ResponseEntity.ok(
                Response.simpleBoolean(isRegistered)
        );
    }

    // 비밀번호 재설정 (분실시)
    @PatchMapping("/passwords/reset")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody ResetPasswordDto dto){
        // 리캡챠 토큰 검증
        recaptchaService.verifyRecaptcha(dto.recaptchaToken());

        // 비밀번호 재설정
        memberService.resetPassword(dto.email(), dto.newPassword());

        return ResponseEntity.noContent().build();
    }
}
