package net.whgkswo.stonesmith.entities.members;

import net.whgkswo.stonesmith.entities.members.nicknames.NicknameService;
import net.whgkswo.stonesmith.responses.dtos.SimpleStringDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(MemberController.BASE_PATH)
public class MemberController {
    private MemberService memberService;
    private NicknameService nicknameService;

    public static final String BASE_PATH = "/api/v1/users";

    public MemberController(MemberService memberService, NicknameService nicknameService){
        this.memberService = memberService;
        this.nicknameService = nicknameService;
    }

    // 닉네임 중복 검사
    @PostMapping("/nicknames/check-availability")
    public ResponseEntity<?> handleNicknameAvailabilityCheck(SimpleStringDto dto){
        // 닉네임 유효성 검사 (걸리면 예외 반환)
        nicknameService.validateNickname(dto.text());

        return ResponseEntity.ok("사용할 수 있는 닉네임입니다.");
    }

    // 회원가입
    public ResponseEntity<?> handleRegisterRequest(MemberDto){

    }
}
