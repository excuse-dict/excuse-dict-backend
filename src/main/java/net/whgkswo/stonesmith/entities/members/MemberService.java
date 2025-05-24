package net.whgkswo.stonesmith.entities.members;

import net.whgkswo.stonesmith.entities.members.rank.Rank;
import net.whgkswo.stonesmith.entities.members.rank.RankRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private MemberMapper memberMapper;
    private MemberRepository memberRepository;
    private RankRepository rankRepository;

    public MemberService(MemberMapper memberMapper, MemberRepository memberRepository, RankRepository rankRepository){
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.rankRepository = rankRepository;
    }

    public Member createUser(MemberDto dto){
        Member member = memberMapper.dtoToUser(dto);

        // TODO: 암호화 적용
        member.setPassword(dto.rawPassword());

        Rank rank = new Rank(Rank.Tier.BRONZE, 10);
        member.setRank(rank);

        return memberRepository.save(member);
    }
}
