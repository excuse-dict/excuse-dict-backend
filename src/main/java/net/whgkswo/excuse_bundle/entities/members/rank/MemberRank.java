package net.whgkswo.excuse_bundle.entities.members.rank;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.BaseEntity;
import net.whgkswo.excuse_bundle.entities.members.Member;


@Getter
@Setter
@Entity
@NoArgsConstructor
public class MemberRank extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private int currentPoint;

    @Enumerated(EnumType.STRING)
    private Type type;

    public MemberRank(Type type){
        this.type = type;
    }

    public void setMember(Member member){
        this.member = member;
        if(member.getMemberRank() == null) member.setMemberRank(this);
    }

    public enum Type {
        TADPOLE("올챙이"),
        LOACH("미꾸라지"),
        SNAKE("능구렁이")
        ;
        private String name;

        Type(String name){
            this.name = name;
        }
    }
}
