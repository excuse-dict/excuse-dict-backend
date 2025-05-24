package net.whgkswo.stonesmith.entities.members;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.stonesmith.entities.TimeStampedEntity;
import net.whgkswo.stonesmith.entities.cards.Card;
import net.whgkswo.stonesmith.entities.members.rank.Rank;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Member extends TimeStampedEntity {

    private String username;

    private String email;

    private String password;

    @OneToMany(mappedBy = "member")
    private final List<Card> cards = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Rank rank;

    public Member(String username,
                  String email,
                  String password
                ){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void setRank(Rank rank){
        this.rank = rank;
        if(rank.getMember() == null) rank.setMember(this);
    }
}
