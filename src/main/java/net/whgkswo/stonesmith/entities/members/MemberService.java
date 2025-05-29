package net.whgkswo.stonesmith.entities.members;

import lombok.RequiredArgsConstructor;
import net.whgkswo.stonesmith.auth.service.AuthService;
import net.whgkswo.stonesmith.entities.members.email.EmailService;
import net.whgkswo.stonesmith.entities.members.nicknames.NicknameService;
import net.whgkswo.stonesmith.entities.members.rank.Rank;
import net.whgkswo.stonesmith.exception.BusinessLogicException;
import net.whgkswo.stonesmith.exception.ExceptionType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameService nicknameService;
    private final EmailService emailService;
    private final AuthService authService;

    // 멤버 가입
    public long createMember(MemberDto dto){
        // 이메일 유효성 검사
        emailService.validateEmail(dto.email());
        // 이메일 인증여부 검사
        authService.checkEmailVerified(dto.email());

        // 닉네임 유효성 검증
        nicknameService.validateNickname(dto.nickname());

        Member member = memberMapper.dtoToUser(dto);

        // 비밀번호 암호화 적용
        String encryptedPassword = passwordEncoder.encode(dto.rawPassword());
        member.setPassword(encryptedPassword);

        Rank rank = new Rank(Rank.Tier.BRONZE, 10);
        member.setRank(rank);

        // 권한 설정
        authService.giveRoles(member);

        memberRepository.save(member);
        return member.getId();
    }
}
