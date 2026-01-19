package net.whgkswo.excuse_dict.dummy.member;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.entities.members.core.dto.MemberRegistrationDto;
import net.whgkswo.excuse_dict.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_dict.entities.members.core.service.MemberService;
import net.whgkswo.excuse_dict.lib.random.RandomCodeGenerator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyMemberGenerator {

    private final MemberService memberService;
    private final RandomCodeGenerator randomCodeGenerator;

    public Member createDummyMember(){

        String randomCode = randomCodeGenerator.generateRandomCode(8);
        String nickname = "더미#" + randomCode;
        String email = "dummy_" + randomCode + "@gmail.com";
        String password = randomCodeGenerator.generateRandomCode(16);

        return memberService.createMemberWithoutValidation(new MemberRegistrationDto(email, nickname, password));
    }
}
