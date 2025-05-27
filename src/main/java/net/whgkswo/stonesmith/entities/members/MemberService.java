package net.whgkswo.stonesmith.entities.members;

import net.whgkswo.stonesmith.entities.members.rank.Rank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private MemberMapper memberMapper;
    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper, MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member createUser(MemberDto dto){
        Member member = memberMapper.dtoToUser(dto);

        // 비밀번호 암호화 적용
        String encryptedPassword = passwordEncoder.encode(dto.rawPassword());
        member.setPassword(encryptedPassword);

        Rank rank = new Rank(Rank.Tier.BRONZE, 10);
        member.setRank(rank);

        return memberRepository.save(member);
    }

    public boolean isEmailAvailable(String email){
        List<Member> members = memberRepository.findAll();

        for(Member member : members){
            if(member.getEmail().equals(email)) return false;
        }
        return true;
    }
}
