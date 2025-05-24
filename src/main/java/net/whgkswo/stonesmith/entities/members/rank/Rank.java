package net.whgkswo.stonesmith.entities.members.rank;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.stonesmith.entities.BaseEntity;
import net.whgkswo.stonesmith.entities.members.Member;


@Getter
@Setter
@Entity
public class Rank extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private int point;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    private int division;   // 브론즈~다이아까진 1~10급, 전설은 순위 저장

    public static int LOWEST_DIVISION = 10;

    public Rank(Tier tier, int division){
        this.tier = tier;
        this.division = division;
    }

    public void setMember(Member member){
        this.member = member;
        if(member.getRank() == null) member.setRank(this);
    }

    public enum Tier{
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND,
        LEGEND
    }
}
