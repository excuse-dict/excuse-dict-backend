package net.whgkswo.stonesmith.entities.members;

import net.whgkswo.stonesmith.entities.members.email.EmailService;
import net.whgkswo.stonesmith.entities.members.nicknames.NicknameService;
import net.whgkswo.stonesmith.entities.members.rank.Rank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private MemberMapper memberMapper;
    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private NicknameService nicknameService;
    private EmailService emailService;

    public MemberService(MemberMapper memberMapper, MemberRepository memberRepository, PasswordEncoder passwordEncoder, NicknameService nicknameService, EmailService emailService){
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.nicknameService = nicknameService;
        this.emailService = emailService;
    }

    // 멤버 가입
    public long createMember(MemberDto dto){
        // 이메일 중복 검사
        emailService.validateEmail(dto.email());
        // 닉네임 유효성 검증
        nicknameService.validateNickname(dto.nickname());

        Member member = memberMapper.dtoToUser(dto);

        // 비밀번호 암호화 적용
        String encryptedPassword = passwordEncoder.encode(dto.rawPassword());
        member.setPassword(encryptedPassword);

        Rank rank = new Rank(Rank.Tier.BRONZE, 10);
        member.setRank(rank);

        memberRepository.save(member);
        return member.getId();
    }


}
