package net.whgkswo.excuse_bundle.dummy.member;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberRegistrationDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.lib.random.RandomCodeGenerator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DummyMemberGenerator {

    private final MemberService memberService;
    private final RandomCodeGenerator randomCodeGenerator;

    public Member createDummyMember(){

        String nickname = "dummy_" + System.currentTimeMillis() + randomCodeGenerator.generateRandomCode(4);
        String email = nickname + "@gmail.com";
        String password = randomCodeGenerator.generateRandomCode(16);

        return memberService.createMember(new MemberRegistrationDto(email, password, nickname));
    }
}
