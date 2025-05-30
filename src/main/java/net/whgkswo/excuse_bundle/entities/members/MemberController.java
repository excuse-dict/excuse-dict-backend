package net.whgkswo.excuse_bundle.entities.members;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.nicknames.NicknameService;
import net.whgkswo.excuse_bundle.responses.Response;
import net.whgkswo.excuse_bundle.responses.dtos.EmailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(MemberController.BASE_PATH)
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final NicknameService nicknameService;

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
    public ResponseEntity<?> handleRegisterRequest(@RequestBody MemberDto dto){
        long memberId = memberService.createMember(dto);
        URI uri = URI.create(BASE_PATH + "/" + memberId);

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
}
